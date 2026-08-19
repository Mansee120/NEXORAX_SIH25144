@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)

package com.health.stridox.ui.main.home.screens.activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.health.stridox.R
import com.health.stridox.data.SensorData
import com.health.stridox.domain.Preferences
import com.health.stridox.ui.main.LocalConnected
import com.health.stridox.ui.main.LocalSensorData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.Locale
import kotlin.math.roundToInt

// --- Data Structures ---

enum class OutdoorActivity {
    RUNNING, WALKING, CYCLING
}

private fun formatTimer(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds)
}

private fun formatPace(totalSeconds: Int, distanceKm: Float): String {
    if (distanceKm <= 0.001f) return "0'00\""
    val minutesPerKm = (totalSeconds / 60f) / distanceKm
    val minutes = minutesPerKm.toInt()
    val seconds = ((minutesPerKm - minutes) * 60).roundToInt()
    return String.format(Locale.ENGLISH, "%d'%02d\"", minutes, seconds)
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OutdoorTrainingScreen(
    navBack: () -> Unit,
    preferences: Preferences = koinInject()
) {
    val sensor = LocalSensorData.current
    val connected = LocalConnected.current
    val coroutineScope = rememberCoroutineScope()
    // --- Make connection + sensor observable like your reference ---
    val connectedObservable by remember(connected) {
        snapshotFlow { connected }
    }.collectAsStateWithLifecycle(initialValue = connected)

    val observedSensor: SensorData by remember(sensor) {
        sensor?.let {
            snapshotFlow { it.copy() }
        } ?: flowOf(SensorData())
    }.collectAsStateWithLifecycle(initialValue = sensor?.copy() ?: SensorData())

    // --- UI State ---
    var selectedActivity by rememberSaveable { mutableStateOf(OutdoorActivity.RUNNING) }
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var totalSecondsElapsed by rememberSaveable { mutableIntStateOf(0) }
    var distanceMeters by rememberSaveable { mutableIntStateOf(0) }
    var caloriesBurned by rememberSaveable { mutableIntStateOf(0) }
    var currentHeartRate by rememberSaveable { mutableIntStateOf(0) }

    // Extra: steps counted from IMU data
    var steps by rememberSaveable { mutableIntStateOf(0) }

    // --- Movement detection constants (tune these) ---
    val MOVEMENT_THRESHOLD = 0.8f

    // Step length (rough) per activity, in meters
    val stepLengthMeters: Float = when (selectedActivity) {
        OutdoorActivity.WALKING -> 0.75f   // avg walking step
        OutdoorActivity.RUNNING -> 1.1f    // longer running stride
        OutdoorActivity.CYCLING -> 3.0f    // treat 1 "movement" as ~3 m travelled
    }

    // Calories per step / movement (very rough – tweak later)
    val caloriesPerStep: Float = when (selectedActivity) {
        OutdoorActivity.WALKING -> 0.04f   // kcal / step
        OutdoorActivity.RUNNING -> 0.06f
        OutdoorActivity.CYCLING -> 0.05f
    }

    // Reset when activity changes or stopped
    LaunchedEffect(selectedActivity, isPlaying) {
        if (!isPlaying) {
            totalSecondsElapsed = 0
            distanceMeters = 0
            caloriesBurned = 0
            currentHeartRate = 0
            steps = 0
        }
    }

    // --- Main loop: uses SENSOR when connected, DUMMY when not ---
    LaunchedEffect(isPlaying, selectedActivity, connectedObservable) {
        if (!isPlaying) return@LaunchedEffect

        while (isPlaying) {
            delay(1000)
            totalSecondsElapsed++

            if (connectedObservable) {
                // ✅ CONNECTED: use real SensorData to detect movement

                val movementDetected =
                    (kotlin.math.abs(observedSensor.accelerometerX) > MOVEMENT_THRESHOLD ||
                              kotlin.math.abs(observedSensor.accelerometerY) > MOVEMENT_THRESHOLD ||
                              kotlin.math.abs(observedSensor.accelerometerZ) > MOVEMENT_THRESHOLD ||
                              kotlin.math.abs(observedSensor.gyroscopeX) > MOVEMENT_THRESHOLD ||
                              kotlin.math.abs(observedSensor.gyroscopeY) > MOVEMENT_THRESHOLD ||
                              kotlin.math.abs(observedSensor.gyroscopeZ) > MOVEMENT_THRESHOLD)

                if (movementDetected) {
                    steps += 1

                    // Convert movement → distance
                    distanceMeters += stepLengthMeters.toInt()
                }

                // Heart rate: prefer real sensor value
                currentHeartRate = if (observedSensor.heartRate > 0) {
                    observedSensor.heartRate
                } else {
                    // fallback within reasonable range if sensor is giving 0
                    (110 + (totalSecondsElapsed % 20)).coerceAtMost(160)
                }

                // Calories from steps/activity
                caloriesBurned = (steps * caloriesPerStep).toInt()
            } else {
                // ❌ NOT CONNECTED: full dummy simulation

                // simulate distance based on activity
                distanceMeters += when (selectedActivity) {
                    OutdoorActivity.RUNNING -> 1 + (0..2).random()
                    OutdoorActivity.WALKING -> 1 + (0..1).random()
                    OutdoorActivity.CYCLING -> 3 + (0..4).random()
                }

                // simulate steps roughly from distance
                steps += 1

                // simulate HR
                currentHeartRate = (110 + (totalSecondsElapsed % 30)).coerceAtMost(160)

                // simulate calories from steps
                caloriesBurned = (steps * caloriesPerStep).toInt()
            }
            if (totalSecondsElapsed >= 1800) {
                val reward = 25
                coroutineScope.launch {
                    val currentPoints = preferences.getPoints()
                    preferences.savePoints(currentPoints + reward)
                }
            }
        }
    }

    // --- Derived values for UI ---
    val distanceKm = remember(distanceMeters) {
        String.format(Locale.ENGLISH, "%.2f", distanceMeters / 1000f)
    }

    val pace = remember(totalSecondsElapsed, distanceMeters) {
        formatPace(totalSecondsElapsed, distanceMeters / 1000f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Outdoor Training", // Placeholder for stringResource(R.string.outdoor_training_title)
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navBack) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back), // Reusing arrow_back
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(visible = true) { // Show controls always for simulation ease
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Stop/End Button (Green Square)
                    Button(
                        onClick = {
                            if (isPlaying) {
                                isPlaying = false
                            } else if (distanceMeters > 0) {
                                // Reset if stopped and metrics recorded
                                totalSecondsElapsed = 0
                                distanceMeters = 0
                                caloriesBurned = 0
                                currentHeartRate = 0
                                isPlaying = false
                            }
                        },
                        shape = CircleShape,
                        modifier = Modifier
                            .size(45.dp)
                            .padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer // Bright green from image
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(if (isPlaying) R.drawable.stop_circle else R.drawable.refresh),
                            contentDescription = if (isPlaying) "Stop Workout" else "Reset Workout",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }

                    // Play/Pause Button (Dark Circle)
                    Button(
                        onClick = {
                            isPlaying = if (!isPlaying && distanceMeters > 0) {
                                true
                            } else if (!isPlaying && distanceMeters == 0) {
                                true
                            } else {
                                false
                            }
                        },
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                            contentDescription = if (isPlaying) "Pause Workout" else "Start Workout",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            // Activity Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color(0xFF2B3B30), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                OutdoorActivity.entries.forEach { activity ->
                    val isSelected = selectedActivity == activity
                    val (iconId, text) = when (activity) {
                        OutdoorActivity.RUNNING -> Pair(R.drawable.directions_run, "Running")
                        OutdoorActivity.WALKING -> Pair(R.drawable.directions_walk, "Walking")
                        OutdoorActivity.CYCLING -> Pair(R.drawable.directions_bike, "Cycling")
                    }

                    ToggleButton(
                        checked = isSelected,
                        onCheckedChange = {
                            selectedActivity = activity
                            if (isPlaying) {
                                // Stop current activity if switching type while playing
                                isPlaying = false
                                totalSecondsElapsed = 0
                                distanceMeters = 0
                                caloriesBurned = 0
                                currentHeartRate = 0
                            }
                        },
                        colors = ToggleButtonDefaults.toggleButtonColors(
                            containerColor = if (isSelected) Color(0xFF00FF00) else Color.Transparent,
                            contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                            .animateContentSize()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(iconId),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(text, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metrics Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MetricCard(
                        title = "Distance",
                        value = "$distanceKm km",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Time",
                        value = formatTimer(totalSecondsElapsed),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MetricCard(
                        title = "Pace",
                        value = pace,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Calories",
                        value = "$caloriesBurned kcal",
                        modifier = Modifier.weight(1f)
                    )
                }
                MetricCard(
                    title = "Heart Rate",
                    value = if (currentHeartRate > 0) "$currentHeartRate bpm" else "-- bpm",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}