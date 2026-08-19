package com.health.stridox.data

import android.bluetooth.BluetoothDevice
import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

// small interface / data class of actions your UI may call
data class BluetoothActions(
    val connectToDevice: (BluetoothDevice) -> Unit = {},
    val disconnect: () -> Unit = {},
    val sendCommand: (String) -> Unit = {},
    val startScan: () -> Unit = {},
    val stopScan: () -> Unit = {},
    val callRequest: () -> Unit = {},
    val setLedMode: (Int) -> Unit = {},
    val sendNotificationToCrutch: (title: String, body: String) -> Unit = { _, _ -> },
    val requestStats: () -> Unit = {},
    val findDevice: () -> Unit = {},
    val requestWeight: () -> Unit = {} // Added for Posture Analysis
)

data class Device(
    val id: Long,
    val name: String,
    val phone: String? = null,
    val isConnected: Boolean = false,
    val btDevice: BluetoothDevice? = null
)

data class Doctor(
    val name: String,
    val specialty: String,
    val phone: String,
    val isAvailable: Boolean,
    val avatarColor: Color? = null

)

@Entity(tableName = "intake_reminders")
data class IntakeReminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String,
    val date: String, // "YYYY-MM-DD" string for the reminder date
    val time: String, // "HH:mm" string for simplicity
    val isActive: Boolean = true
)

data class SensorData(
    // MPU6050 - Accelerometer & Gyroscope
    val accelerometerX: Float = 0f,
    val accelerometerY: Float = 0f,
    val accelerometerZ: Float = 0f,
    val gyroscopeX: Float = 0f,
    val gyroscopeY: Float = 0f,
    val gyroscopeZ: Float = 0f,
    val infraredValue: Long = 0L,
    val redValue: Long = 0L,
    val ldrValue: Int = 0,

    val steps: Long = 0L,
    val calories: Float = 0f,
    val movingTimeMs: Long = 0L,
    val heartRate: Int = 0,
    val spO2: Int = 0,
    val batteryPercent: Int = 0,
    val ledMode: Int = 0,
    val brightnessPercent: Int = 0,
    val autoSleepIndex: Int = 0,
    val modelName: String = "",
    val variantName: String = "",
    val modelNumber: String = "",

    // NEW STAT FIELDS
    val cadence: Float = 0f, // steps per minute
    val stepCv: Float = 0f, // step Coefficient of Variation
    val consistency: Int = 0, // 0..100
    val exertionIndex: Float = 0f, // heuristic
    val mobility: Int = 0, // 0..100

    val timestamp: Long = System.currentTimeMillis(),
    // HX711 - Weight/Force Sensor
    val weight: Float = 0f,
    val force: Float = 0f,
    // LM393 - Digital Sensor (Proximity/Obstacle Detection)
    val proximityDetected: Boolean = false,
    val obstacleDistance: Float = 0f, // in cm
    val sensorState: Boolean = false, // Digital state
    val triggerActive: Boolean = false, // Digital state
)


data class SensorValues(
    val accX: Float = 0f,
    val accY: Float = 0f,
    val accZ: Float = 0f,
    val gyroX: Float = 0f,
    val gyroY: Float = 0f,
    val gyroZ: Float = 0f,
    val hr: Int = 0,
    val spo2: Int = 0,
    val weight: Float = 0f,
    val force: Float = 0f,
    val prox: Boolean = false,
    val distance: Float = 0f,
    val state: Boolean = false
)

@Serializable
data class LoginUser(
    val name: String,
    val password: String,
    val email: String,
    val emergencyNumber: String,
    val profileUrl: String? = null,
    val isLogin: Boolean = false,
    val address: String = "",
    val gender: String = "",
    val birthday: String = "",
    val height: String = "",
    val weight: String = "",
    val stepLength: String = "",
    val armpitToWrist: String = "",
    val wristToFoot: String = ""
)
