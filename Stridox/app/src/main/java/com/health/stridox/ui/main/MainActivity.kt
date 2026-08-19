@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.health.stridox.ui.main

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.health.stridox.bluetooth.BluetoothService
import com.health.stridox.data.BluetoothActions
import com.health.stridox.data.SensorData
import com.health.stridox.domain.Preferences
import com.health.stridox.navigation.Home
import com.health.stridox.navigation.Login
import com.health.stridox.navigation.Register
import com.health.stridox.ui.main.home.HomeScreen
import com.health.stridox.ui.main.login.LoginScreen
import com.health.stridox.ui.main.register.RegisterScreen
import com.health.stridox.ui.main.viewmodel.BluetoothViewModel
import com.health.stridox.ui.theme.StridexTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject


val LocalSensorData = staticCompositionLocalOf<SensorData?> { null }
val LocalConnected = staticCompositionLocalOf { false }
val LocalBluetoothActions = staticCompositionLocalOf { BluetoothActions() }
val LocalConnectedDeviceAddress = staticCompositionLocalOf<String?> { null }
val LocalTriggerActivated = staticCompositionLocalOf { false }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class MainActivity : ComponentActivity() {

    var boundService: BluetoothService? = null
        private set

    private var isBound = false

    // Jobs to cancel collectors when unbinding
    private var sensorJob: Job? = null
    private var connectionJob: Job? = null
    private var foundDevicesJob: Job? = null
    private var connectedAddressJob: Job? = null
    private var triggerJob: Job? = null

    private val viewModel: BluetoothViewModel by viewModels()

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = getSystemService(BluetoothManager::class.java)
        manager?.adapter
    }

    private val enableBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                pendingStartScan?.invoke()
            } else {
                pendingStartScan = null
            }
        }

    private var pendingStartScan: (() -> Unit)? = null

    // MutableCompose-backed holder for current connected address
    private val connectedDeviceAddressState = mutableStateOf<String?>(null)

    // Trigger state
    private val triggerActivatedState = mutableStateOf(false)

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        permissions.add(Manifest.permission.CALL_PHONE)
        permissions.add(Manifest.permission.SEND_SMS)
        permissions.add(Manifest.permission.BODY_SENSORS)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        }
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        // Bluetooth permissions for Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            permissions.add(Manifest.permission.BLUETOOTH)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        // NOTIFICATION PERMISSIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // STORAGE PERMISSIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val permissionsArray = permissions.toTypedArray()
        if (permissionsArray.any {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(this, permissionsArray, 1)
        }
    }

    private fun ensureBluetoothEnabled(onEnabled: () -> Unit) {
        val adapter = bluetoothAdapter ?: return

        if (adapter.isEnabled) {
            onEnabled()
            return
        }

        pendingStartScan = onEnabled
        val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetoothLauncher.launch(enableIntent)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as? BluetoothService.LocalBinder
            boundService = localBinder?.getService()
            isBound = true
            Log.d("MainActivity", "✓ Service connected")

            // Sensor data
            sensorJob = lifecycleScope.launch {
                boundService?.sensorDataFlow?.collect { data ->
                    data?.let { viewModel.onSensorData(it) }
                }
            }

            // Connection status
            connectionJob = lifecycleScope.launch {
                boundService?.connectionFlow?.collect { isConnected ->
                    viewModel.onConnectionChanged(isConnected)
                }
            }

            // Connected device address
            connectedAddressJob = lifecycleScope.launch {
                boundService?.connectedDeviceAddress?.collect { addr ->
                    lifecycleScope.launch(Dispatchers.Main) {
                        connectedDeviceAddressState.value = addr
                    }
                }
            }

            // Found devices
            foundDevicesJob = lifecycleScope.launch {
                boundService?.foundDevicesFlow?.collect { devices ->
                    viewModel.onFoundDevices(devices)
                }
            }

            // Trigger activated
            triggerJob = lifecycleScope.launch {
                boundService?.triggerActivatedFlow?.collect { triggered ->
                    lifecycleScope.launch(Dispatchers.Main) {
                        triggerActivatedState.value = triggered
                        if (triggered) {
                            delay(6000)
                            triggerActivatedState.value = false
                        }
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            boundService = null
            Log.d("MainActivity", "✗ Service disconnected")

            sensorJob?.cancel(); sensorJob = null
            connectionJob?.cancel(); connectionJob = null
            foundDevicesJob?.cancel(); foundDevicesJob = null
            connectedAddressJob?.cancel(); connectedAddressJob = null
            triggerJob?.cancel(); triggerJob = null

            connectedDeviceAddressState.value = null
            triggerActivatedState.value = false
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "onStart called")
        requestPermissions()
        if (!Settings.canDrawOverlays(this)) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    this,
                    "grant Permission For Background emergency calling",
                    Toast.LENGTH_SHORT
                ).show()
            }
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri()
            )
            startActivityForResult(intent, 123)
        }
        // ✅ CRITICAL: Start service in foreground explicitly
        val serviceIntent = Intent(this, BluetoothService::class.java)
        startForegroundService(serviceIntent)
        Log.d("MainActivity", "✓ startForegroundService called")

        // ✅ Bind after a short delay to ensure service is ready
        lifecycleScope.launch(Dispatchers.Main) {
            delay(500) // Wait 500ms for service to initialize
            if (!isBound) {
                bindService(serviceIntent, serviceConnection, 0)
                Log.d("MainActivity", "✓ bindService called after delay")
            } else {
                Log.d("MainActivity", "⚠️ Already bound, skipping bind")
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            sensorJob?.cancel(); sensorJob = null
            connectionJob?.cancel(); connectionJob = null
            foundDevicesJob?.cancel(); foundDevicesJob = null
            connectedAddressJob?.cancel(); connectedAddressJob = null
            triggerJob?.cancel(); triggerJob = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            try {
                unbindService(serviceConnection)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error unbinding service: ${e.message}")
            }
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            val preferences = koinInject<Preferences>()
            val themeMode by preferences.getThemeMode()
                .collectAsStateWithLifecycle(initialValue = "system")

            val isDarkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }

            StridexTheme(darkTheme = isDarkTheme) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val user = runBlocking {
                        preferences.islogin()
                    }

                    val connectedAddr = connectedDeviceAddressState.value
                    val triggerActivated = triggerActivatedState.value

                    BluetoothDataProvider(
                        viewModel = viewModel,
                        connectToDevice = { device: BluetoothDevice ->
                            boundService?.connectToDevice(device, {
                                requestPermissions()
                            }, {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Unable to pair this device",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }) {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "successfully paired",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        disconnect = {
                            boundService?.disconnect()
                        },
                        sendCommand = { cmd: String ->
                            boundService?.sendCommand(cmd)
                        },
                        startScan = {
                            ensureBluetoothEnabled {
                                boundService?.startScan {
                                    requestPermissions()
                                }
                            }
                        },
                        stopScan = {
                            boundService?.stopScan()
                        },
                        connectedDeviceAddress = connectedAddr,
                        callRequest = {
                            requestPermissions()
                        },
                        setLedMode = { mode: Int ->
                            boundService?.setLedMode(mode)
                        },
                        sendNotificationToCrutch = { title: String, body: String ->
                            boundService?.sendNotificationToCrutch(title, body)
                        },
                        requestStats = {
                            boundService?.requestStats()
                        },
                        findDevice = {
                            boundService?.findDevice()
                        },
                        requestWeight = {
                            boundService?.requestWeight()
                        },
                        triggerActivated = triggerActivated
                    ) {
                        val backStack = remember(user) {
                            mutableStateListOf(
                                if (user) Home else Login
                            )
                        }
                        NavDisplay(
                            backStack = backStack,
                            entryProvider = entryProvider {
                                entry<Home> {
                                    HomeScreen(onLoggedOut = {
                                        backStack.removeLastOrNull()
                                        backStack.add(Login)
                                    })
                                }
                                entry<Login> {
                                    LoginScreen(onLoginSuccess = {
                                        backStack.removeLastOrNull()
                                        backStack.add(Home)
                                    }, onRegisterClick = {
                                        backStack.add(Register)
                                    })
                                }
                                entry<Register> {
                                    RegisterScreen(onRegistered = {
                                        backStack.removeLastOrNull()
                                        backStack.add(Login)
                                    }) {
                                        backStack.removeLastOrNull()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BluetoothDataProvider(
    viewModel: BluetoothViewModel,
    connectToDevice: (BluetoothDevice) -> Unit,
    disconnect: () -> Unit,
    sendCommand: (String) -> Unit,
    startScan: () -> Unit,
    stopScan: () -> Unit,
    callRequest: () -> Unit,
    connectedDeviceAddress: String?,
    setLedMode: (Int) -> Unit,
    sendNotificationToCrutch: (title: String, body: String) -> Unit,
    requestStats: () -> Unit,
    findDevice: () -> Unit,
    requestWeight: () -> Unit,
    triggerActivated: Boolean = false,
    content: @Composable () -> Unit
) {
    val sensorData by viewModel.sensorData.collectAsStateWithLifecycle()
    val connected by viewModel.connected.collectAsStateWithLifecycle()

    val actions = remember {
        BluetoothActions(
            connectToDevice = connectToDevice,
            disconnect = disconnect,
            sendCommand = sendCommand,
            startScan = startScan,
            stopScan = stopScan,
            callRequest = callRequest,
            setLedMode = setLedMode,
            sendNotificationToCrutch = sendNotificationToCrutch,
            requestStats = requestStats,
            findDevice = findDevice,
            requestWeight = requestWeight
        )
    }

    CompositionLocalProvider(
        LocalSensorData provides sensorData,
        LocalConnected provides connected,
        LocalBluetoothActions provides actions,
        LocalConnectedDeviceAddress provides connectedDeviceAddress,
        LocalTriggerActivated provides triggerActivated
    ) {
        content()
    }
}