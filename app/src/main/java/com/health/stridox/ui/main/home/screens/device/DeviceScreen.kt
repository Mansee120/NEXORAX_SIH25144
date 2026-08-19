@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.health.stridox.ui.main.home.screens.device

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuDefaults.outlinedTextFieldColors
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.health.stridox.R
import com.health.stridox.data.Device
import com.health.stridox.data.SensorData
import com.health.stridox.data.SensorValues
import com.health.stridox.domain.Preferences
import com.health.stridox.ui.main.LocalBluetoothActions
import com.health.stridox.ui.main.LocalConnectedDeviceAddress
import com.health.stridox.ui.main.LocalSensorData
import com.health.stridox.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import kotlin.math.ceil
import kotlin.math.roundToInt

// Sample UI colors — adapt to your theme
private val AccentOrange = Color(0xFFFF772D)

enum class CrutchVariant {
    DHRUVX, APEX, ZENX
}

data class ReminderUi(
    val id: Long,
    val title: String,
    val body: String
)

// Reference / calibration data
private const val ARMPIT_TO_WRIST_REF = 53.0f
private const val WRIST_TO_FOOT_REF = 67.0f
private const val UPPER_REF = 11.5f
private const val BOTTOM_REF = 15.5f

// Crutch physical limits
private const val UPPER_MIN = 0.0f
private const val UPPER_MAX = 16.0f
private const val BOTTOM_MIN = 0.0f
private const val BOTTOM_MAX = 21.5f

data class CrutchAdjustmentResult(
    val upperCm: Float,
    val bottomCm: Float,
    val overallLevel: Int,
    val topLevel: Int,
    val bottomLevel: Int,
    val notes: String
)

private suspend fun computeCrutchAdjustment(
    armpitToWristCm: Float,
    wristToFootCm: Float,
    weightKg: Float
): CrutchAdjustmentResult? = withContext(Dispatchers.Default) {
    if (armpitToWristCm <= 0f || wristToFootCm <= 0f) return@withContext null

    val refSum = ARMPIT_TO_WRIST_REF + WRIST_TO_FOOT_REF

    val userSum = armpitToWristCm + wristToFootCm

    // 3. sizeRatio = userSum / refSum (clamp 0.5 → 1.5)
    var sizeRatio = userSum / refSum
    sizeRatio = sizeRatio.coerceIn(0.5f, 1.5f)

    // 4. Raw cm
    val upperRaw = UPPER_REF * sizeRatio
    val bottomRaw = BOTTOM_REF * sizeRatio

    // 5. Clamp to device limits
    var upperCm = upperRaw.coerceIn(UPPER_MIN, UPPER_MAX)
    var bottomCm = bottomRaw.coerceIn(BOTTOM_MIN, BOTTOM_MAX)

    val notes = mutableListOf<String>()

    // 6. Adjust based on weight
    if (weightKg > 110f) {
        upperCm *= 0.95f
        bottomCm *= 0.95f
        notes.add("Reduced extension by 5% due to high weight (>110 kg).")
    } else if (weightKg in 0f..45f) {
        upperCm *= 1.05f
        bottomCm *= 1.05f
        notes.add("Increased extension by 5% due to low weight (<45 kg).")
    } else {
        notes.add("No weight-based adjustment applied.")
    }

    // Re-clamp to device limits
    upperCm = upperCm.coerceIn(UPPER_MIN, UPPER_MAX)
    bottomCm = bottomCm.coerceIn(BOTTOM_MIN, BOTTOM_MAX)

    if (upperCm == UPPER_MIN || upperCm == UPPER_MAX ||
        bottomCm == BOTTOM_MIN || bottomCm == BOTTOM_MAX
    ) {
        notes.add("One or more values clamped to device limits.")
    }

    // 7. overallLevel (1–10)
    val overallLevel = (sizeRatio * 10f)
        .roundToInt()
        .coerceIn(1, 10)

    // 8. topLevel (1–10)
    val rawTopLevel = ceil((upperCm / UPPER_MAX) * 10f).toInt()
    val topLevel = rawTopLevel.coerceIn(1, 10)

    // 9. bottomLevel (1–10)  ✅ UPDATED TO 1–10
    val rawBottomLevel = ceil((bottomCm / BOTTOM_MAX) * 10f).toInt()
    val bottomLevel = rawBottomLevel.coerceIn(1, 10)

    return@withContext CrutchAdjustmentResult(
        upperCm = upperCm,
        bottomCm = bottomCm,
        overallLevel = overallLevel,
        topLevel = topLevel,
        bottomLevel = bottomLevel,
        notes = notes.joinToString(" ")
    )
}
// --------------------------------------------

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun DeviceScreen(modifier: Modifier = Modifier) {
    val actions = LocalBluetoothActions.current
    val sensorData = LocalSensorData.current
    val activity = LocalActivity.current

    // Observe latest SensorData safely
    val observedSensor: SensorData by remember(sensorData) {
        sensorData?.let {
            snapshotFlow { it.copy() }
        } ?: flowOf(SensorData())
    }.collectAsStateWithLifecycle(initialValue = sensorData?.copy() ?: SensorData())

    // Devices discovered while scanning
    val discoveredDevices = remember { mutableStateListOf<Device>() }

    // --- NEW: User Data Fetching ---
    val preferences = koinInject<Preferences>()
    val loginUser by preferences.getLoginResponseFlow()
        .collectAsStateWithLifecycle(initialValue = null)
// Add this at the top of your composable for debugging
    val tag = "DeviceCollection"

// Collect service foundDevicesFlow into local list
    LaunchedEffect(Unit) {
        Log.d(tag, "LaunchedEffect started")
        val activity = activity as? MainActivity
        Log.d(tag, "Activity cast: ${activity?.javaClass?.simpleName ?: "null"}")

        if (activity == null) {
            Log.e(tag, "❌ Activity is not MainActivity!")
            return@LaunchedEffect
        }

        try {
            // Directly access the service (don't use snapshotFlow yet)
            val service = activity.boundService
            Log.d(tag, "Service: ${service?.javaClass?.simpleName ?: "null"}")

            if (service == null) {
                Log.e(tag, "❌ Service is null - binding may not be complete")
                // Try waiting for service
                var retries = 0
                while (retries < 20) {
                    delay(100)
                    retries++
                    Log.d(tag, "Waiting for service... attempt $retries")
                }
            }

            service?.let { svc ->
                Log.d(tag, "✓ Service available, collecting foundDevicesFlow")

                svc.foundDevicesFlow.collect { btList ->
                    Log.d(tag, "📱 Received ${btList.size} devices")

                    if (btList.isEmpty()) {
                        Log.d(tag, "Empty device list")
                        withContext(Dispatchers.Main) {
                            discoveredDevices.clear()
                        }
                        return@collect
                    }

                    // Check permission
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ActivityCompat.checkSelfPermission(
                                activity,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.w(tag, "Missing BLUETOOTH_CONNECT permission")
                            actions.callRequest()
                            return@collect
                        }
                    }

                    // Map devices
                    val mapped = btList.map { bt ->
                        Device(
                            id = (bt.address ?: bt.name ?: "").hashCode().toLong(),
                            name = bt.name ?: bt.address ?: "Unknown",
                            isConnected = false,
                            btDevice = bt
                        )
                    }

                    Log.d(tag, "✓ Mapped ${mapped.size} devices:")
                    mapped.forEach {
                        Log.d(tag, "  - ${it.name} (${it.id})")
                    }

                    withContext(Dispatchers.Main) {
                        discoveredDevices.clear()
                        discoveredDevices.addAll(mapped)
                        Log.d(tag, "✓ UI list updated: ${discoveredDevices.size} items")
                    }
                }
            } ?: run {
                Log.e(tag, "❌ Service is still null after waiting")
            }
        } catch (e: Exception) {
            Log.e(tag, "❌ Error in LaunchedEffect: ${e.message}", e)
        }
    }

    val connectedAddress = LocalConnectedDeviceAddress.current
    val isConnected = connectedAddress != null

    var developerModeActive by rememberSaveable { mutableStateOf(false) }
    var showScanArea by rememberSaveable { mutableStateOf(false) }

    // Reminders (for crutch display)
    var reminders by rememberSaveable { mutableStateOf(listOf<ReminderUi>()) }
    var newReminderTitle by rememberSaveable { mutableStateOf("") }
    var newReminderBody by rememberSaveable { mutableStateOf("") }

    // Detect variant based on sensor data (modelName/variantName)
    val variantNameRaw = when {
        observedSensor.variantName.isNotBlank() -> observedSensor.variantName
        observedSensor.modelName.isNotBlank() -> observedSensor.modelName
        else -> "ApeX"
    }.trim()

    val variantType = when (variantNameRaw) {
        "DhruvX" -> CrutchVariant.DHRUVX
        "ZenX" -> CrutchVariant.ZENX
        "ApeX" -> CrutchVariant.APEX
        else -> CrutchVariant.APEX // default full-feature variant
    }

    Scaffold { padding ->
        if (!isConnected) {
            // -------------------------
            // UNPAIRED / UNCONNECTED UI
            // -------------------------
            Column(
                modifier = modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Empty state / no device card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.bluetooth_24px),
                                contentDescription = "Bluetooth",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No StrideX crutch paired",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pair your MediCrutch once.\nNext time it will auto-connect.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = Color(0xFFB0B0B0)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        showScanArea = true
                        actions.startScan()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = buttonColors(containerColor = AccentOrange)
                ) {
                    Text(
                        "Pair Crutch",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // After user taps Pair, show simple scan + device list
                if (showScanArea) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { actions.startScan() }
                        ) {
                            Text("Scan Again")
                        }
                        Button(
                            onClick = {
                                actions.stopScan()
                            },
                            colors = buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Stop", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (discoveredDevices.isEmpty()) {
                        Text(
                            "Searching for crutches nearby...",
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = Color(0xFFB0B0B0),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            items(discoveredDevices, key = { it.id }) { device ->
                                DeviceCard(
                                    modifier = Modifier.animateItem(),
                                    device = device,
                                    isConnectedDevice = connectedAddress == device.btDevice?.address,
                                    onConnect = {
                                        device.btDevice?.let { bt ->
                                            actions.connectToDevice(bt)
                                        }
                                    },
                                    onDisconnect = {
                                        actions.disconnect()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // -------------------------
            // CONNECTED / PAIRED UI
            // -------------------------
            Column(
                modifier = modifier
                    .padding(padding)
                    .fillMaxSize()
                    .animateContentSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        actions.findDevice()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                ) {
                    Text(
                        "Find Device",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                // HEADER: crutch info + image
                PairedCrutchHeader(
                    model = observedSensor.modelName.ifBlank { "MediCrutch" },
                    variant = variantNameRaw,
                    modelNumber = observedSensor.modelNumber.ifBlank { "MC-01" },
                    batteryPercent = observedSensor.batteryPercent,
                    onChangeDevice = {
                        actions.disconnect()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // LED MODE SECTION (depends on variant)
                LedModeSection(
                    variantType = variantType,
                    currentModeIndex = observedSensor.ledMode,
                    onModeSelected = { modeIndex ->
                        actions.setLedMode(modeIndex)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                val coroutineScope = rememberCoroutineScope()
                //POSTURE ANALYSIS SECTION
                PostureAnalysisSection(
                    userWeightKg = loginUser?.weight?.toFloatOrNull() ?: 0f,
                    userArmpitToWristCm = loginUser?.armpitToWrist?.toFloatOrNull() ?: 0f,
                    userWristToFootCm = loginUser?.wristToFoot?.toFloatOrNull() ?: 0f,
                    onUserWristToFootCmUpdate = {
                        coroutineScope.launch {
                            loginUser?.copy(wristToFoot = it.toString())?.let { loginResponse ->
                                preferences.login(
                                    loginResponse
                                )
                            }
                        }
                    },
                    onUserArmpitToWristCmUpdate = {
                        coroutineScope.launch {
                            loginUser?.copy(armpitToWrist = it.toString())?.let { loginResponse ->
                                preferences.login(
                                    loginResponse
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // REMINDERS SECTION (display on crutch)
                ReminderSection(
                    reminders = reminders,
                    newTitle = newReminderTitle,
                    newBody = newReminderBody,
                    onTitleChange = { newReminderTitle = it },
                    onBodyChange = { newReminderBody = it },
                    onAddReminder = {
                        if (newReminderTitle.isNotBlank() || newReminderBody.isNotBlank()) {
                            val newItem = ReminderUi(
                                id = System.currentTimeMillis(),
                                title = newReminderTitle.ifBlank { "Reminder" },
                                body = newReminderBody
                            )
                            reminders = reminders + newItem
                            newReminderTitle = ""
                            newReminderBody = ""
                        }
                    },
                    onSendToCrutch = { reminder ->
                        actions.sendNotificationToCrutch(reminder.title, reminder.body)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // DEVELOPER MODE + RAW SENSOR DATA
                DeveloperModeCard(
                    isActive = developerModeActive,
                    onEnter = { developerModeActive = true },
                    onExit = { developerModeActive = false },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (developerModeActive) {
                    SensorDataView(
                        sensorValues = SensorValues(
                            accX = observedSensor.accelerometerX,
                            accY = observedSensor.accelerometerY,
                            accZ = observedSensor.accelerometerZ,
                            gyroX = observedSensor.gyroscopeX,
                            gyroY = observedSensor.gyroscopeY,
                            gyroZ = observedSensor.gyroscopeZ,
                            hr = observedSensor.heartRate,
                            spo2 = observedSensor.spO2,
                            weight = observedSensor.weight,
                            force = observedSensor.force,
                            prox = observedSensor.proximityDetected,
                            distance = observedSensor.obstacleDistance,
                            state = observedSensor.sensorState
                        ),
                        onSendCustomData = { _ -> },
                        onToggleDeveloperMode = { developerModeActive = false }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

// ------------------------
// HELPER FOR INPUT FIELDS
// ------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasurementField(
    label: String,
    valueCm: Float,
    modifier: Modifier = Modifier,
    set: (Float) -> Unit = {}
) {
    var textState by remember(valueCm) {
        mutableStateOf(if (valueCm == 0f) "0.0" else "%.1f".format(valueCm))
    }
    val focusManager = LocalFocusManager.current
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // OutlinedTextField appearance
                OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "cm",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    },
                    colors = outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Button(onClick = {
                    focusManager.clearFocus(true)
                    set(textState.toFloatOrNull() ?: 0f)
                }) {
                    Text("Set")
                }
            }
        }
    }
}

// --------------------------------------------
@Composable
fun PostureAnalysisSection(
    userWeightKg: Float,
    userArmpitToWristCm: Float,
    userWristToFootCm: Float,
    onUserArmpitToWristCmUpdate: (Float) -> Unit,
    onUserWristToFootCmUpdate: (Float) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    // New state: full computed adjustment result
    var adjustmentResult by remember { mutableStateOf<CrutchAdjustmentResult?>(null) }

    // Helper Composable for result buttons
    @Composable
    fun ResultButton(label: String, value: String) {
        FilledTonalButton(
            onClick = { /* No action on click */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.alertfilled), // Placeholder for info icon
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$label to ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            // Header: "Posture & Adjustment" and Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Adjustment",
                        style = MaterialTheme.typography.titleLarge, // Using titleLarge for better emphasis
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Detailed Suggestion of Crutch Height Adjustment",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Right-side Icon (Standing person)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_accessibility_new_24),
                        contentDescription = "Posture Icon",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Armpit to Wrist Input Field
            MeasurementField(
                label = "Armpit to Wrist",
                valueCm = userArmpitToWristCm,
                modifier = Modifier.fillMaxWidth(),
                set = onUserArmpitToWristCmUpdate
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Wrist to Foot Input Field
            MeasurementField(
                label = "Wrist to Foot",
                valueCm = userWristToFootCm,
                modifier = Modifier.fillMaxWidth(),
                set = onUserWristToFootCmUpdate
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Suggest Height Adjustment Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        adjustmentResult = computeCrutchAdjustment(
                            armpitToWristCm = userArmpitToWristCm,
                            wristToFootCm = userWristToFootCm,
                            weightKg = userWeightKg
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer // Muted green/secondary color
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.mode_heat_24px), // Using a 'suggest' icon placeholder
                        contentDescription = "Suggest",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Suggest Height Adjustment",
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Results Display
            val result = adjustmentResult

            when {
                result != null -> {
                    ResultButton(
                        label = "Set Upper Extension",
                        value = "%.2f cm".format(result.upperCm)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ResultButton(
                        label = "Set Lower Extension",
                        value = "%.2f cm".format(result.bottomCm)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ResultButton(
                        label = "Set Top Level (1–10)",
                        value = "${result.topLevel}"
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ResultButton(
                        label = "Set Bottom Level (1–10)",
                        value = "${result.bottomLevel}"
                    )

                    if (result.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Notes: ${result.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                userArmpitToWristCm > 0f || userWristToFootCm > 0f -> {
                    Text(
                        "Press the button to calculate adjustment levels...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> {
                    Text(
                        text = "Please set your Armpit to Wrist and Wrist to Foot measurements in your profile for adjustment suggestions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ------------------------
// HEADER WHEN CONNECTED
// ------------------------
@Composable
fun PairedCrutchHeader(
    model: String,
    variant: String,
    modelNumber: String,
    batteryPercent: Int,
    onChangeDevice: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Connected Crutch",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = model,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Variant: $variant",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Model No: $modelNumber",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Battery
                Text(
                    text = "Battery: $batteryPercent%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x33000000))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (batteryPercent.coerceIn(0, 100) / 100f))
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (batteryPercent < 20) Color.Red
                                else Color(0xFF37E055)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                FilledTonalButton(
                    onClick = onChangeDevice,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Change Device")
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Crutch image (replace with your asset)
            Image(
                painter = painterResource(id = R.drawable.ic_crutch),
                contentDescription = "Crutch",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.05f))
            )
        }
    }
}

// ------------------------
// LED MODES SECTION
// ------------------------
@Composable
fun LedModeSection(
    variantType: CrutchVariant,
    currentModeIndex: Int,
    onModeSelected: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Headlight Modes",
                style = MaterialTheme.typography.titleMediumEmphasized,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))

            val description = when (variantType) {
                CrutchVariant.DHRUVX ->
                    "DhruvX has focused essential lighting: Low beam, Blinker and Safety mode."

                CrutchVariant.APEX, CrutchVariant.ZENX ->
                    "Advanced lighting with Auto, High beam, Low beam, Blinker and Safety."
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB0B0B0)
            )

            Spacer(modifier = Modifier.height(12.dp))

            val modes: List<Pair<String, Int>> = when (variantType) {
                CrutchVariant.DHRUVX -> listOf(
                    "Off" to 0,
                    "Low Beam" to 3,
                    "Blinker" to 4,
                    "Safety" to 5
                )

                CrutchVariant.APEX,
                CrutchVariant.ZENX -> listOf(
                    "Off" to 0,
                    "Auto" to 1,
                    "High Beam" to 2,
                    "Low Beam" to 3,
                    "Blinker" to 4,
                    "Safety" to 5
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                modes.chunked(3).forEach { rowModes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowModes.forEach { (label, index) ->
                            val selected = currentModeIndex == index
                            FilledTonalButton(
                                onClick = { onModeSelected(index) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(18.dp),
                                colors = buttonColors(
                                    containerColor = if (selected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(
                                    label,
                                    fontSize = 13.sp,
                                    color = if (selected)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        if (rowModes.size < 3) {
                            repeat(3 - rowModes.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------
// REMINDER SECTION
// ------------------------
@Composable
fun ReminderSection(
    reminders: List<ReminderUi>,
    newTitle: String,
    newBody: String,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onAddReminder: () -> Unit,
    onSendToCrutch: (ReminderUi) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Reminders on Crutch",
                style = MaterialTheme.typography.titleMediumEmphasized,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Create small reminders that will pop up on your StrideX display.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB0B0B0)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = newTitle,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12),
                placeholder = { Text("Reminder title (e.g. Medicine)") },
                colors = outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = newBody,
                onValueChange = onBodyChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp),
                shape = RoundedCornerShape(12),
                placeholder = { Text("Short note to show on crutch screen") },
                colors = outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onAddReminder,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = buttonColors(containerColor = AccentOrange)
            ) {
                Text("Add Reminder", color = Color.White)
            }

            if (reminders.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "My Reminders",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                reminders.forEach { reminder ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                reminder.title,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (reminder.body.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    reminder.body,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFCCCCCC)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            FilledTonalButton(
                                onClick = { onSendToCrutch(reminder) },
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Text("Show Now on Crutch")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------
// EXISTING COMPONENTS (DeviceCard, DeveloperModeCard, SensorDataView)
// LEFT ALMOST AS-IS
// ------------------------

@Composable
fun DeviceCard(
    modifier: Modifier,
    device: Device,
    isConnectedDevice: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isConnectedDevice) Color(0xFF37E055)
                        else MaterialTheme.colorScheme.primary
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    device.name.take(2).uppercase(),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    device.name,
                    color = if (isConnectedDevice) Color(0xFF45FF70) else Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (isConnectedDevice) {
                    Text(
                        "Connected",
                        fontSize = 12.sp,
                        color = Color(0xFF45FF70)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isConnectedDevice) {
                Button(
                    onClick = onDisconnect,
                    colors = buttonColors(containerColor = Color.Red)
                ) {
                    Text("Disconnect", color = Color.White)
                }
            } else {
                Button(
                    onClick = onConnect,
                    colors = buttonColors(containerColor = AccentOrange)
                ) {
                    Text("Connect", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DeveloperModeCard(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    onEnter: () -> Unit,
    onExit: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (!isActive) {
                Text(
                    text = "🔧 Developer Mode",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Access raw sensor data and advanced features for development and testing purposes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onEnter,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        "Enter Developer Mode",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ For development use only",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔧 Developer Mode Active",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    FilledTonalButton(
                        onClick = onExit,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(0.dp, Color.Transparent),
                    ) {
                        Text("Exit")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorDataView(
    modifier: Modifier = Modifier,
    sensorValues: SensorValues,
    customDataInitial: String = "",
    onSendCustomData: (String) -> Unit = {},
    onToggleDeveloperMode: (() -> Unit)? = null
) {
    var customData by remember { mutableStateOf(customDataInitial) }

    Column(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "MPU6050 Sensor",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Accelerometer: X: %.2f | Y: %.2f | Z: %.2f".format(
                        sensorValues.accX, sensorValues.accY, sensorValues.accZ
                    )
                )
                Text(
                    "Gyroscope: X: %.2f | Y: %.2f | Z: %.2f".format(
                        sensorValues.gyroX, sensorValues.gyroY, sensorValues.gyroZ
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "MAX30100",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Heart Rate: ${sensorValues.hr} BPM")
                    Text("SpO2: ${sensorValues.spo2}%")
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "HX711 Sensor",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Weight: %.1f kg".format(sensorValues.weight))
                    Text("Force: %.1f N".format(sensorValues.force))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "LM393 Proximity Sensor",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (sensorValues.prox) "Proximity: Object Detected" else "Proximity: No Object"
                )
                Text("Distance: %.1f cm".format(sensorValues.distance))
                Text(
                    text = if (sensorValues.state) "Sensor State: Active" else "Sensor State: Inactive"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "Modify Sensor Data",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customData,
                    onValueChange = { customData = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    placeholder = { Text("Enter custom sensor data (e.g., JSON)") },
                    colors = outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFF3A3A3A)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onSendCustomData(customData) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        "Send Data to Device",
                        color = Color(0xFF1E1E1E),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        onToggleDeveloperMode?.let { toggle ->
            FilledTonalButton(
                onClick = toggle,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
            ) {
                Text("Exit Developer Mode")
            }
        }
    }
}