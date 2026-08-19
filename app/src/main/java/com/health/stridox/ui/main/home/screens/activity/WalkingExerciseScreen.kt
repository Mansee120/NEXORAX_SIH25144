package com.health.stridox.ui.main.home.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.health.stridox.R
import com.health.stridox.data.SensorData
import com.health.stridox.domain.Preferences
import com.health.stridox.ui.main.LocalConnected
import com.health.stridox.ui.main.LocalSensorData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import org.koin.compose.koinInject
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

// Utility function adapted from HealthScreen.kt to match the required format (MM:SS)
private fun formatMovingTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return "${String.format(Locale.ENGLISH, "%02d", minutes)}:${
        String.format(
            Locale.ENGLISH,
            "%02d",
            remainingSeconds
        )
    }"
}

// Constants
private const val DISTANCE_PER_STEP_KM = 0.000762f // Approx 0.762 meters per step
private const val STEPS_GOAL = 10_000f
private const val MOVEMENT_THRESHOLD = 0.8f // From HealthScreen.kt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WalkingExerciseScreen(
    navBack: () -> Unit,
    preferences: Preferences = koinInject()
) {
    val sensor = LocalSensorData.current
    val connected = LocalConnected.current

    val connectedObservable by remember(connected) {
        snapshotFlow { connected }
    }.collectAsStateWithLifecycle(initialValue = connected)

    val observedSensor: SensorData by remember(sensor) {
        sensor?.let {
            snapshotFlow { it.copy() }
        } ?: flowOf(SensorData())
    }.collectAsStateWithLifecycle(initialValue = sensor?.copy() ?: SensorData())

    // State for tracking exercise
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var totalSecondsElapsed by rememberSaveable { mutableIntStateOf(0) }
    var currentSteps by rememberSaveable { mutableIntStateOf(0) }
    var currentDistanceKm by rememberSaveable { mutableFloatStateOf(0f) }


    // Sensor and time tracking logic
    LaunchedEffect(observedSensor, connectedObservable, isPlaying) {
        if (!isPlaying) return@LaunchedEffect

        if (connectedObservable) {
            // Check for movement
            val movementDetected = (
                      abs(observedSensor.accelerometerX) > MOVEMENT_THRESHOLD ||
                                abs(observedSensor.accelerometerY) > MOVEMENT_THRESHOLD ||
                                abs(observedSensor.accelerometerZ) > MOVEMENT_THRESHOLD ||
                                abs(observedSensor.gyroscopeX) > MOVEMENT_THRESHOLD ||
                                abs(observedSensor.gyroscopeY) > MOVEMENT_THRESHOLD ||
                                abs(observedSensor.gyroscopeZ) > MOVEMENT_THRESHOLD
                      )

            if (movementDetected) {
                // Increment session metrics on movement detected
                totalSecondsElapsed += 1
                currentSteps += 1
                currentDistanceKm = currentSteps * DISTANCE_PER_STEP_KM
            }
        } else {
            // Simulation when disconnected, as per HealthScreen.kt's logic for dummy data
            while (isPlaying) {
                delay(1000) // update every 1 second
                totalSecondsElapsed += 1
                currentSteps += 1
                currentDistanceKm = currentSteps * DISTANCE_PER_STEP_KM
            }
        }
    }

    // Calculated progress and display values
    val stepsProgress by remember(currentSteps) {
        derivedStateOf { (currentSteps / STEPS_GOAL).coerceIn(0f, 1f) }
    }
    val goalPercentage = (stepsProgress * 100).roundToInt().coerceAtMost(100)

    Scaffold(
        topBar = {
            TopAppBar(navigationIcon = {
                IconButton(onClick = navBack) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = stringResource(R.string.back_content_description)
                    )
                }
            }, title = {
                Text(stringResource(R.string.walking_exercise_title), fontWeight = FontWeight.Bold)
            })
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Empty space where map was in the image
            Spacer(modifier = Modifier.height(32.dp))

            // Metrics Row 1: Duration & Distance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WalkingMetricCard(
                    title = stringResource(R.string.duration_metric_title),
                    value = formatMovingTime(totalSecondsElapsed),
                    modifier = Modifier.weight(1f)
                )
                WalkingMetricCard(
                    title = stringResource(R.string.distance_metric_title),
                    value = String.format(
                        Locale.ENGLISH,
                        stringResource(R.string.distance_format),
                        currentDistanceKm
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metric Row 2: Steps
            WalkingMetricCard(
                title = stringResource(R.string.steps_metric_title),
                value = currentSteps.toString(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Daily Goal Progress
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.daily_goal_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        stringResource(R.string.percentage_format, goalPercentage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { stepsProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(64.dp))


            // Control Buttons (Fixed at the bottom)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stop Button (Square icon in a circle)
                IconButton(
                    onClick = {
                        isPlaying = false
                        totalSecondsElapsed = 0
                        currentSteps = 0
                        currentDistanceKm = 0f
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .weight(1f, fill = false)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.stop_circle),
                        contentDescription = stringResource(R.string.stop_content_description),
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(24.dp))

                // Play/Pause Button (Pill-shaped, Primary color)
                Button(
                    onClick = { isPlaying = !isPlaying },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .height(72.dp)
                        .weight(3f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        painter = if (isPlaying) painterResource(R.drawable.pause) else painterResource(
                            R.drawable.play
                        ),
                        contentDescription = if (isPlaying) stringResource(R.string.pause_content_description) else stringResource(
                            R.string.play_content_description
                        ),
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isPlaying) stringResource(R.string.pause_button_text) else stringResource(
                            R.string.start_button_text
                        ),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

    }
}

@Composable
fun WalkingMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}