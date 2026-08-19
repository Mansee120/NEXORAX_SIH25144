package com.health.stridox.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.health.stridox.MyApp
import com.health.stridox.R
import com.health.stridox.data.SensorData
import com.health.stridox.domain.Preferences
import com.health.stridox.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import org.koin.android.ext.android.get
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

class BluetoothService : Service() {

    private val TAG = "BluetoothService"
    private val binder = LocalBinder()

    // Classic Bluetooth UUID (Standard SPP UUID)
    private val BLUETOOTH_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // --- Foreground Service Constants ---
    private val NOTIFICATION_CHANNEL_ID = "BluetoothServiceChannel"
    private val NOTIFICATION_CHANNEL_NAME = "Bluetooth Connection"
    private val NOTIFICATION_ID = 101

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: BufferedReader? = null

    private var connectedDevice: BluetoothDevice? = null

    // Coroutine scope
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    // Flows exposed to ViewModel/UI
    private val _sensorDataFlow = MutableStateFlow<SensorData?>(null)
    val sensorDataFlow: StateFlow<SensorData?> get() = _sensorDataFlow

    private val _connectionFlow = MutableStateFlow(false)
    val connectionFlow: StateFlow<Boolean> get() = _connectionFlow

    private val _connectedDeviceAddress = MutableStateFlow<String?>(null)
    val connectedDeviceAddress: StateFlow<String?> get() = _connectedDeviceAddress

    // Registration status (REG_OK received from MediCrutch)
    private val _registeredFlow = MutableStateFlow(false)
    val registeredFlow: StateFlow<Boolean> get() = _registeredFlow

    // Scanning results
    private val _foundDevicesFlow = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val foundDevicesFlow: StateFlow<List<BluetoothDevice>> get() = _foundDevicesFlow

    private val foundDevicesSet = LinkedHashMap<String, BluetoothDevice>()

    // Tracks if a trigger action was activated on the device
    private val _triggerActivatedFlow = MutableSharedFlow<Boolean>()
    val triggerActivatedFlow = _triggerActivatedFlow.asSharedFlow()

    @SuppressLint("MissingPermission")
    private fun addDeviceIfNew(device: BluetoothDevice) {
        val address = device.address ?: return

        synchronized(foundDevicesSet) {
            if (foundDevicesSet.containsKey(address)) return

            foundDevicesSet[address] = device
            _foundDevicesFlow.value = foundDevicesSet.values.toList()
            Log.d(TAG, "Added device: ${device.name} ($address)")
        }
    }

    // Communication control
    private val isConnecting = AtomicBoolean(false)
    private val isScanning = AtomicBoolean(false)
    private var receiverThread: Thread? = null

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }

    override fun onCreate() {
        super.onCreate()
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        Log.d(TAG, "BluetoothService created (Classic Bluetooth mode)")

        createNotificationChannel()

        // START FOREGROUND WITH PLACEHOLDER NOTIFICATION IMMEDIATELY
        val placeholderNotif = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Bluetooth service ready")
            .setSmallIcon(R.drawable.bluetooth_24px)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, placeholderNotif)

        val filter = android.content.IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        registerReceiver(discoveryReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "════════════════════════════════════════")
        Log.d(TAG, "✓ onBind called - Activity binding to service")
        Log.d(TAG, "════════════════════════════════════════")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "✓ onUnbind called - Activity unbound from service")
        Log.d(TAG, "⚠️ Service will continue running in background (START_STICKY)")
        return true
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Log.d(TAG, "✓ onRebind called - Activity rebound to service")
    }

    // Build phoneId for registration
    private fun buildPhoneId(): String {
        val model = Build.MODEL ?: "Android"
        val id = Build.ID ?: "ID"
        return "$model-$id"
    }

    // --- Foreground Service Helpers ---
    private fun createNotificationChannel() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = NOTIFICATION_CHANNEL_ID
        val channelName = NOTIFICATION_CHANNEL_NAME

        // Default notification sound URI (system)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // If channel already exists, decide whether to recreate it
        val existing = manager.getNotificationChannel(channelId)
        if (existing != null) {
            // If existing channel has no sound or lower importance than required, delete it and recreate.
            val needRecreate = (existing.importance < NotificationManager.IMPORTANCE_DEFAULT)
                      || (existing.sound == null)
                      // optional: compare URIs if you want an exact match
                      || (existing.sound != defaultSoundUri)

            if (!needRecreate) {
                // Channel already correct — nothing to do.
                return
            }

            // Delete the old channel so we can create a fresh one with the desired sound.
            manager.deleteNotificationChannel(channelId)
        }

        // Build audio attributes for notification sound
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT // Default importance -> plays sound
        ).apply {
            description = "Keeps the Bluetooth connection active in the background."
            setSound(defaultSoundUri, audioAttributes)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 100, 50, 100)
        }

        manager.createNotificationChannel(channel)
    }

    private fun getForegroundNotification(deviceName: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Connected to $deviceName")
            .setSmallIcon(R.drawable.bluetooth_24px)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun startForegroundService(deviceName: String) {
        val notification = getForegroundNotification(deviceName)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
        Log.d(TAG, "Updated foreground notification for $deviceName")
    }

    private val discoveryReceiver = object : android.content.BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        IntentCompat.getParcelableExtra(
                            intent,
                            BluetoothDevice.EXTRA_DEVICE,
                            BluetoothDevice::class.java
                        )

                    device?.let {
                        addDeviceIfNew(device)
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d(TAG, "Discovery finished")
                    isScanning.set(false)
                }
            }
        }
    }

    // ========== CONNECT TO DEVICE ==========
    @SuppressLint("MissingPermission")
    fun connectToDevice(
        device: BluetoothDevice,
        requestPermission: () -> Unit = {},
        showFailError: () -> Unit = {},
        onPair: () -> Unit = {}
    ) {
        scope.launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ActivityCompat.checkSelfPermission(
                        this@BluetoothService,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission()
                    return@launch
                }

                if (isConnecting.getAndSet(true)) {
                    Log.w(TAG, "Connection already in progress")
                    return@launch
                }

                disconnectInternal()

                Log.d(TAG, "Connecting to ${device.name} (${device.address})...")
                connectedDevice = device

                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(BLUETOOTH_UUID)
                    bluetoothSocket?.connect()

                    Log.d(TAG, "✓ Connected to ${device.name}")

                    outputStream = bluetoothSocket?.outputStream
                    inputStream = BufferedReader(InputStreamReader(bluetoothSocket?.inputStream))

                    scope.launch {
                        _connectionFlow.value = true
                        _connectedDeviceAddress.value = device.address
                    }

                    // UPDATE FOREGROUND SERVICE WITH CONNECTED DEVICE NAME
                    startForegroundService(device.name ?: "MediCrutch")

                    // Send registration immediately
                    val phoneId = buildPhoneId()
                    sendCommand("REG;$phoneId")
                    onPair.invoke()

                    // Start listening for messages
                    startMessageListener()

                } catch (e: Exception) {
                    Log.e(TAG, "Connection failed: ${e.message}", e)
                    scope.launch {
                        _connectionFlow.value = false
                        _connectedDeviceAddress.value = null
                        _registeredFlow.value = false
                    }
                    disconnectInternal()
                    showFailError.invoke()
                }

            } catch (e: Exception) {
                Log.e(TAG, "connectToDevice error: ${e.message}", e)
                isConnecting.set(false)
                showFailError.invoke()
            }
        }
    }

    // ========== MESSAGE LISTENER ==========
    private fun startMessageListener() {
        if (receiverThread?.isAlive == true) return

        receiverThread = Thread {
            try {
                while (bluetoothSocket?.isConnected == true && inputStream != null) {
                    val line = inputStream?.readLine() ?: break

                    scope.launch {
                        if (line.isNotEmpty()) {
                            Log.d(TAG, "Received: $line")
                            handleIncomingString(line)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Message listener ended: ${e.message}")
            } finally {
                scope.launch {
                    if (bluetoothSocket?.isConnected == false) {
                        disconnectInternal()
                    }
                }
            }
        }.apply {
            isDaemon = false
            name = "BluetoothReceiver"
        }
        receiverThread?.start()
    }

    // ========== PROTOCOL HANDLING ==========
    private fun handleIncomingString(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        // Registration OK
        if (trimmed.equals("REG_OK", ignoreCase = true)) {
            Log.d(TAG, "MediCrutch registration confirmed (REG_OK)")
            _registeredFlow.value = true
        }

        // --- Registration error: empty ID ---
        if (trimmed.equals("REG_ERR;EMPTY_ID", ignoreCase = true)) {
            Log.w(TAG, "Registration error: EMPTY_ID, regenerating phoneId...")
            _registeredFlow.value = false

            val phoneId = buildPhoneId()
            if (phoneId.isNotBlank()) {
                sendCommand("REG;$phoneId")
            }
        }

        // --- Global / error messages ---
        if (trimmed.startsWith("ERR;", ignoreCase = true)) {
            Log.w(TAG, "Error from device: $trimmed")

            if (trimmed.equals("ERR;NOT_REGISTERED", ignoreCase = true)) {
                _registeredFlow.value = false

                val phoneId = buildPhoneId()
                if (phoneId.isNotBlank()) {
                    sendCommand("REG;$phoneId")
                }
            }
        }

        // Status message: STAT;steps=...;cal=...;mov=...;bpm=...;spo2=...;bat=...;led=...;br=...;asIdx=...;model=...;var=...;num=...
        if (trimmed.startsWith("STAT", ignoreCase = true)) {
            val payload = trimmed.substringAfter("STAT", "").trimStart(';')
            if (payload.isNotEmpty()) {
                val parsed = parseStatusPayload(payload)
                if (parsed != null) {
                    _sensorDataFlow.value = parsed
                }
            }
        }

        // Trigger message
        if (trimmed.startsWith("TRIG;", ignoreCase = true)) {
            val tsString = trimmed.substringAfter("TRIG;ts=", "")
            val ts = tsString.toLongOrNull()
            scope.launch {
                _triggerActivatedFlow.emit(true)
                if (!MyApp.isInForeground) {
                    triggerEmergencyFromService()
                }
            }
        }

        // Weight message
        if (trimmed.startsWith("WEIGHT;", ignoreCase = true)) {
            val value = trimmed.substringAfter("WEIGHT;", "")
            val weight = value.toFloatOrNull()
            if (weight != null) {
                val current = _sensorDataFlow.value ?: SensorData()
                _sensorDataFlow.value = current.copy(weight = weight)
            }
        }

        // Raw sensor data
        if (trimmed.startsWith("RAW", ignoreCase = true)) {
            val payload = trimmed.substringAfter("RAW", "").trimStart(';')
            val parsed = parseRawPayload(payload)
            if (parsed != null) {
                val current = _sensorDataFlow.value ?: SensorData()
                _sensorDataFlow.value = parsed.copy(
                    steps = current.steps,
                    calories = current.calories,
                    heartRate = current.heartRate,
                    spO2 = current.spO2
                )
            }
        }
    }

    /**
     * Parse STAT payload: steps=123;cal=4.5;mov=10000;bpm=80;spo2=98;bat=90;led=2;br=60;asIdx=1;model=MediCrutch;var=Prototype A;num=MC-01
     */
    private fun parseStatusPayload(payload: String): SensorData? {
        try {
            var sensor = _sensorDataFlow.value ?: SensorData()
            val pairs = payload.split(";")

            for (pair in pairs) {
                if (pair.isBlank()) continue
                val kv = pair.split("=", limit = 2)
                if (kv.size != 2) continue

                val key = kv[0].trim().lowercase()
                val value = kv[1].trim()

                sensor = try {
                    when (key) {
                        "steps" -> sensor.copy(steps = value.toLong())
                        "cal" -> sensor.copy(calories = value.toFloat())
                        "mov" -> sensor.copy(movingTimeMs = value.toLong())
                        "bpm" -> sensor.copy(heartRate = value.toInt())
                        "spo2" -> sensor.copy(spO2 = value.toInt())
                        "bat" -> sensor.copy(batteryPercent = value.toInt())
                        "led" -> sensor.copy(ledMode = value.toInt())
                        "br" -> sensor.copy(brightnessPercent = value.toInt())
                        "asidx" -> sensor.copy(autoSleepIndex = value.toInt())
                        "model" -> sensor.copy(modelName = value)
                        "var" -> sensor.copy(variantName = value)
                        "num" -> sensor.copy(modelNumber = value)
                        // NEW STAT FIELDS
                        "cad" -> sensor.copy(cadence = value.toFloat())
                        "cov" -> sensor.copy(stepCv = value.toFloat())
                        "cons" -> sensor.copy(consistency = value.toInt())
                        "ex" -> sensor.copy(exertionIndex = value.toFloat())
                        "mob" -> sensor.copy(mobility = value.toInt())
                        else -> sensor.copy()
                    }
                } catch (_: NumberFormatException) {
                    Log.w(TAG, "Parse error for $key=$value")
                    sensor.copy()
                }
            }
            return sensor
        } catch (e: Exception) {
            Log.e(TAG, "parseStatusPayload failed: ${e.message}")
            return null
        }
    }

    /**
     * Parse RAW payload: ax=1.23;ay=4.56;az=9.81;gx=0.1;gy=0.2;gz=0.3;ir=123456;red=654321;ldr=1;w=75.5
     */
    private fun parseRawPayload(payload: String): SensorData? {
        try {
            var sensor = _sensorDataFlow.value ?: SensorData()
            val pairs = payload.split(";")

            for (pair in pairs) {
                if (pair.isBlank()) continue
                val kv = pair.split("=", limit = 2)
                if (kv.size != 2) continue

                val key = kv[0].trim().lowercase()
                val value = kv[1].trim()

                sensor = try {
                    when (key) {
                        "ax" -> sensor.copy(accelerometerX = value.toFloat())
                        "ay" -> sensor.copy(accelerometerY = value.toFloat())
                        "az" -> sensor.copy(accelerometerZ = value.toFloat())
                        "gx" -> sensor.copy(gyroscopeX = value.toFloat())
                        "gy" -> sensor.copy(gyroscopeY = value.toFloat())
                        "gz" -> sensor.copy(gyroscopeZ = value.toFloat())
                        "ir" -> sensor.copy(infraredValue = value.toLong())
                        "red" -> sensor.copy(redValue = value.toLong())
                        "ldr" -> sensor.copy(ldrValue = value.toInt())
                        "w" -> sensor.copy(weight = value.toFloat())
                        else -> sensor.copy()
                    }
                } catch (_: NumberFormatException) {
                    Log.w(TAG, "Parse error for $key=$value")
                    sensor.copy()
                }
            }
            return sensor
        } catch (e: Exception) {
            Log.e(TAG, "parseRawPayload failed: ${e.message}")
            return null
        }
    }

    // ========== SEND COMMANDS ==========
    @SuppressLint("MissingPermission")
    fun sendCommand(command: String) {
        scope.launch {
            try {
                if (bluetoothSocket?.isConnected != true || outputStream == null) {
                    Log.w(TAG, "Cannot send: not connected")
                    return@launch
                }

                val cmd = "$command\n"
                outputStream?.write(cmd.toByteArray())
                outputStream?.flush()

                Log.d(TAG, "Sent: $command")
            } catch (e: Exception) {
                Log.e(TAG, "Send error: ${e.message}")
                disconnectInternal()
            }
        }
    }

    // ========== PUBLIC API ==========
    fun requestStats() {
        sendCommand("GET_STAT\n")
    }

    fun findDevice() {
        sendCommand("FIND_DEVICE")
    }

    fun requestWeight() {
        sendCommand("REQ_WEIGHT")
    }

    fun setLedMode(modeIndex: Int) {
        sendCommand("SET_LED;$modeIndex")
    }

    fun setBrightness(percent: Int) {
        val p = percent.coerceIn(0, 100)
        sendCommand("SET_BR;$p")
    }

    fun setAutoSleep(index: Int) {
        val idx = index.coerceIn(0, 4)
        sendCommand("SET_AS;$idx")
    }

    fun sendNotificationToCrutch(title: String, body: String) {
        val safeTitle = title.replace(";", ",").replace("\n", " ")
        val safeBody = body.replace(";", ",").replace("\n", " ")
        sendCommand("NOTIF;$safeTitle;$safeBody")
    }

    fun disconnect() {
        scope.launch {
            disconnectInternal()
            ServiceCompat.stopForeground(
                this@BluetoothService,
                ServiceCompat.STOP_FOREGROUND_REMOVE
            )
            stopSelf()
        }
    }

    @SuppressLint("MissingPermission")
    private fun disconnectInternal() {
        try {
            isConnecting.set(false)
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()

            inputStream = null
            outputStream = null
            bluetoothSocket = null
            connectedDevice = null

            _connectionFlow.value = false
            _connectedDeviceAddress.value = null
            _registeredFlow.value = false

            // DON'T stop foreground here - keep service running
            Log.d(TAG, "Internal disconnect complete.")
        } catch (e: Exception) {
            Log.e(TAG, "Disconnect error: ${e.message}")
        }
    }

    // ========== CLASSIC BT SCAN ==========
    @SuppressLint("MissingPermission")
    fun startScan(requestPermission: () -> Unit = {}) {
        scope.launch {
            if (isScanning.getAndSet(true)) {
                Log.w(TAG, "Scan already in progress")
                return@launch
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ActivityCompat.checkSelfPermission(
                    this@BluetoothService,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "BLUETOOTH_SCAN permission not granted")
                isScanning.set(false)
                requestPermission()
                return@launch
            }

            val adapter = bluetoothAdapter ?: run {
                Log.e(TAG, "Bluetooth adapter not available")
                isScanning.set(false)
                return@launch
            }

            if (!adapter.isEnabled) {
                Log.e(TAG, "Bluetooth is disabled")
                isScanning.set(false)
                return@launch
            }

            try {
                synchronized(foundDevicesSet) { foundDevicesSet.clear() }
                _foundDevicesFlow.value = emptyList()

                Log.d(TAG, "✓ Starting Bluetooth scan...")

                // Get bonded devices
                val bondedDevices = adapter.bondedDevices
                bondedDevices?.forEach { device ->
                    addDeviceIfNew(device)
                }
                // Start discovery
                adapter.startDiscovery()

                // Auto-stop after 30 seconds
                scope.launch {
                    delay(30000)
                    stopScan()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Scan error: ${e.message}")
                isScanning.set(false)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        scope.launch {
            if (!isScanning.getAndSet(false)) {
                Log.d(TAG, "Scan not running")
                return@launch
            }

            try {
                bluetoothAdapter?.cancelDiscovery()
                Log.d(TAG, "✓ Scan stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Stop scan error: ${e.message}")
            }
        }
    }

    fun findDeviceCommand() {
        Log.d(TAG, "FIND_DEVICE command received")
        // Implementation for FIND_DEVICE command if needed
        // For now, just log that the command was received.
        // If you need to send a response, you can use sendCommand() here.
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(discoveryReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "Receiver already unregistered? ${e.message}")
        }

        try {
            receiverThread?.join(5000)
        } catch (e: Exception) {
            Log.w(TAG, "Error joining receiver thread: ${e.message}")
        }

        job.cancel()
        disconnectInternal()
        Log.d(TAG, "Service destroyed")
    }

    fun triggerEmergencyFromService() {
        scope.launch {
            try {
                val preferences: Preferences = get() // Koin injection in Service
                val context: Context = get()
                val fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(context)

                performEmergencyCallAndSms(
                    context = context,
                    fusedLocationClient = fusedLocationClient,
                    preferences = preferences
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error in triggerEmergencyFromService: ${e.message}", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun FusedLocationProviderClient.awaitSingleUpdate(): android.location.Location? =
        suspendCancellableCoroutine { cont ->
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000L // 1 second interval (not really used for single update)
            )
                .setWaitForAccurateLocation(true)
                .setMaxUpdates(1)
                .build()

            val callback = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                    removeLocationUpdates(this)
                    if (!cont.isCompleted) {
                        cont.resume(result.lastLocation) { cause, _, _ -> cause.printStackTrace() }
                    }
                }

                override fun onLocationAvailability(availability: com.google.android.gms.location.LocationAvailability) {
                    if (!availability.isLocationAvailable) {
                    }
                }
            }

            requestLocationUpdates(request, callback, Looper.getMainLooper())

            cont.invokeOnCancellation {
                removeLocationUpdates(callback)
            }
        }

    suspend fun performEmergencyCallAndSms(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        preferences: Preferences
    ) {
        val tag = "Emergency"

        // Permissions check – do NOT request here, just bail out if missing
        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val hasSmsPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasBackgroundLocation =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

        val hasForegroundLocation =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.FOREGROUND_SERVICE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }


        if (!hasCallPermission || !hasSmsPermission || !hasLocationPermission || !hasBackgroundLocation || !hasForegroundLocation) {
            Log.w(
                tag,
                "Missing permissions: call=$hasCallPermission sms=$hasSmsPermission loc=$hasLocationPermission locBack=$hasBackgroundLocation locF=$hasForegroundLocation"
            )
            return
        }

        val emergencyNumber = preferences.getLoginResponse()?.emergencyNumber
        if (emergencyNumber.isNullOrEmpty()) {
            Log.w(tag, "Emergency number not set")
            return
        }

        val locationMessage = getCurrentLocation(context, fusedLocationClient)
        makePhoneCall(context, emergencyNumber)
        sendEmergencySms(context, emergencyNumber, locationMessage)

    }

    fun isLocationServiceAvailable(context: Context): Boolean {
        val availability = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context)
        return availability == ConnectionResult.SUCCESS
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient
    ): String {
        if (!isLocationServiceAvailable(context)) {
            Log.e("HomeScreen", "Google Play Services Location API not available on this device")
            return "Location service is not available on this device. Cannot get my location."
        }

        return try {
            // Use last known location
            var location = fusedLocationClient.lastLocation.await()

            if (location == null) {
                Log.d("HomeScreen", "lastLocation is null, requesting single update…")
                location = fusedLocationClient.awaitSingleUpdate()
            }

            location?.let {
                // Format location as a Google Maps URL
                "My current location is: https://maps.google.com/?q=${it.latitude},${it.longitude}"
            } ?: "My current location is unknown (null location)."
        } catch (_: SecurityException) {
            "Location permission denied. My location is unknown (SecurityException)."
        } catch (e: Exception) {
            Log.e("HomeScreen", "Error getting location: ${e.message}")
            "Failed to get my location."
        }
    }

    fun makePhoneCall(context: Context, number: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = "tel:$number".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Settings.canDrawOverlays(context)) {
                Log.e("Call", "Cannot make call: SYSTEM_ALERT_WINDOW permission missing")
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        context,
                        "Grant 'Display over other apps' to allow auto-calling",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
            Log.d("Call", "Call intent executed")
        } catch (e: Exception) {
            e.printStackTrace()
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Failed to make call", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sendEmergencySms(context: Context, number: String, locationMessage: String) {
        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(android.telephony.SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                android.telephony.SmsManager.getDefault()
            }

            val message = "Emergency! I need help. $locationMessage"

            smsManager.sendTextMessage(number, null, message, null, null)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Emergency SMS sent", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Failed to send SMS", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
