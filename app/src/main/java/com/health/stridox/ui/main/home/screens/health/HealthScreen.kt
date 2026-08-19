@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.health.stridox.ui.main.home.screens.health

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.health.stridox.R
import com.health.stridox.data.SensorData
import com.health.stridox.domain.Preferences
import com.health.stridox.ui.main.LocalBluetoothActions
import com.health.stridox.ui.main.LocalConnected
import com.health.stridox.ui.main.LocalSensorData
import kotlinx.coroutines.flow.flowOf
import org.koin.compose.koinInject
import java.util.Locale

// Helper function to format seconds into minutes/hours
fun formatMovingTime(totalSeconds: Int): String {
    if (totalSeconds < 60) {
        return "${totalSeconds}s"
    }
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    if (minutes < 60) {
        return "${minutes}m ${remainingSeconds}s"
    }
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return "${hours}h ${remainingMinutes}m"
}

@Composable
fun HealthScreen(
    navToHeartRate: () -> Unit,
    navToSpO2: () -> Unit,
    navToStep: () -> Unit,
    navToWeight: () -> Unit,
    navToCalories: () -> Unit,
    navToDashboard: () -> Unit,
    navToCadence: () -> Unit,
    navToStepConsistency: () -> Unit,
    navToExertionIndex: () -> Unit,
    navToMobility: () -> Unit,
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

    var heartRate by rememberSaveable { mutableIntStateOf(observedSensor.heartRate) }
    var spo2 by rememberSaveable { mutableIntStateOf(observedSensor.spO2) }
    var steps by rememberSaveable { mutableIntStateOf(0) } // Changed to Int, initialized to 0
    var calories by rememberSaveable { mutableIntStateOf(0) } // Initialize to 0, will be calculated
    var weight by rememberSaveable { mutableFloatStateOf(observedSensor.weight) }
    var movingTimeSeconds by rememberSaveable { mutableIntStateOf(0) } // State for tracking moving time
    var cadence by rememberSaveable { mutableIntStateOf(observedSensor.cadence.toInt()) } // Added cadence
    var stepConsistency by rememberSaveable { mutableIntStateOf(observedSensor.stepCv.toInt()) } // Added step consistency
    var exertionIndex by rememberSaveable { mutableIntStateOf(observedSensor.exertionIndex.toInt()) } // Added exertion index
    var mobility by rememberSaveable { mutableIntStateOf(observedSensor.mobility) } // Added mobility


    // Initialize movingTimeSeconds and steps from preferences on first composition
    LaunchedEffect(Unit) {
        movingTimeSeconds = preferences.getMovingTime()
        steps = preferences.getSteps()
    }

    LaunchedEffect(observedSensor, connectedObservable) {
        if (connectedObservable) {
            heartRate = observedSensor.heartRate
            spo2 = observedSensor.spO2
            weight = observedSensor.weight
            steps = observedSensor.steps.toInt()
            calories = observedSensor.calories.toInt()      // or keep as Float in UI
            movingTimeSeconds = (observedSensor.movingTimeMs / 1000L).toInt()
            cadence = observedSensor.cadence.toInt()
            stepConsistency = observedSensor.stepCv.toInt()
            exertionIndex = observedSensor.exertionIndex.toInt()
            mobility = observedSensor.mobility
            preferences.saveMovingTime(movingTimeSeconds)
            preferences.saveSteps(steps)
        }
    }

    // Example goals / ranges — replace with real values or user settings
    val stepsGoal = 10_000f
    val caloriesBurnGoal = 350f
    val hrMin = 40f
    val hrMax = 180f
    val spo2Min = 85f
    val spo2Max = 100f
    val maxWeight = 150f
    val maxCadence = 200f // Example max cadence
    val maxStepConsistency = 100f // Example max step consistency
    val maxExertionIndex = 10f // Example max exertion index
    val maxMobility = 100f // Example max mobility

    // compute progress values in [0f, 1f]
    val hrProgress by remember(heartRate, hrMin, hrMax) {
        derivedStateOf {
            ((heartRate.coerceIn(hrMin.toInt(), hrMax.toInt()) - hrMin) / (hrMax - hrMin))
                .coerceIn(0f, 1f)
        }
    }

    val spo2Progress by remember(spo2, spo2Min, spo2Max) {
        derivedStateOf {
            ((spo2.coerceIn(spo2Min.toInt(), spo2Max.toInt()) - spo2Min) / (spo2Max - spo2Min))
                .coerceIn(0f, 1f)
        }
    }

    val weightProgress by remember(weight, maxWeight) {
        derivedStateOf {
            (weight / maxWeight).coerceIn(0f, 1f)
        }
    }

    val cadenceProgress by remember(cadence, maxCadence) {
        derivedStateOf {
            (cadence.toFloat() / maxCadence).coerceIn(0f, 1f)
        }
    }

    val stepConsistencyProgress by remember(stepConsistency, maxStepConsistency) {
        derivedStateOf {
            (stepConsistency.toFloat() / maxStepConsistency).coerceIn(0f, 1f)
        }
    }

    val exertionIndexProgress by remember(exertionIndex, maxExertionIndex) {
        derivedStateOf {
            (exertionIndex.toFloat() / maxExertionIndex).coerceIn(0f, 1f)
        }
    }

    val mobilityProgress by remember(mobility, maxMobility) {
        derivedStateOf {
            (mobility.toFloat() / maxMobility).coerceIn(0f, 1f)
        }
    }


    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    stringResource(R.string.health_metrics_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        )
    }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val actions = LocalBluetoothActions.current

            Button(
                onClick = {
                    actions.requestStats()
                }, shapes = ButtonShapes(
                    RoundedCornerShape(14.dp),
                    RoundedCornerShape(50.dp)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Text("Synchronous")
            }

            // Dashboard Card
            Card(
                onClick = navToDashboard,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.rewarded),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "View your stats & rewards",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Consolidated Stats Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF6BD5FF).copy(alpha = 0.18f)
                ),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF6BD5FF).copy(alpha = 0.06f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Calories Item
                    StatsItem(
                        icon = R.drawable.fire, // Replace with your fire icon resource
                        label = "Calories",
                        value = calories.toString(),
                        subtext = "/$caloriesBurnGoal kcal",
                        color = Color(0xFFFF9800), // Orange
                        onClick = navToCalories
                    )

                    // Steps Item
                    StatsItem(
                        icon = R.drawable.steps_24px, // Replace with your shoe icon resource
                        label = "Steps",
                        value = steps.toString(),
                        subtext = "/${stepsGoal.toInt()} steps",
                        color = Color(0xFFFFC107), // Yellow/Gold
                        onClick = navToStep
                    )

                    // Moving Item
                    StatsItem(
                        icon = R.drawable.timer, // Replace with your clock icon resource
                        label = "Moving",
                        value = formatMovingTime(movingTimeSeconds), // Simplified for now, format as needed
                        color = Color(0xFF2196F3), // Blue
                    )
                }
            }

            // Metrics Cards
            HighlightMetric(
                label = stringResource(R.string.heart_rate_title),
                value = stringResource(R.string.heart_rate_format, heartRate),
                progress = hrProgress,
                accent = Color(0xFFFF6B6B),
                onClick = navToHeartRate,
                removeText = true
            )

            HighlightMetric(
                label = stringResource(R.string.spo2_title),
                value = "%1d".format(spo2),
                progress = spo2Progress,
                accent = Color(0xFF4ECDC4),
                onClick = navToSpO2
            )

            HighlightMetric(
                label = stringResource(R.string.weight_title),
                value = String.format(
                    Locale.ENGLISH,
                    stringResource(R.string.weight_format),
                    weight
                ),
                progress = weightProgress,
                accent = Color(0xFF95E1D3),
                onClick = navToWeight
            )

            // New Metrics Cards
            HighlightMetric(
                label = "Cadence",
                value = cadence.toString(),
                progress = cadenceProgress,
                accent = Color(0xFFE0B0FF), // Purple accent
                onClick = navToCadence
            )

            HighlightMetric(
                label = "Step Consistency",
                value = stepConsistency.toString(),
                progress = stepConsistencyProgress,
                accent = Color(0xFFA5D6A7),
                onClick = navToStepConsistency
            )

            HighlightMetric(
                label = "Exertion Index",
                value = exertionIndex.toString(),
                progress = exertionIndexProgress,
                accent = Color(0xFFFFE0B2),
                onClick = navToExertionIndex
            )

            HighlightMetric(
                label = "Mobility",
                value = mobility.toString(),
                progress = mobilityProgress,
                accent = Color(0xFFB39DDB),
                onClick = navToMobility
            )
        }
    }
}

@Composable
fun StatsItem(
    icon: Int,
    label: String,
    value: String,
    subtext: String? = null,
    color: Color,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            onClick?.let {
                Icon(
                    painter = painterResource(id = R.drawable.keyboard_arrow_right_24px), // Small arrow
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        subtext?.let {
            Text(
                text = it,
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun HighlightMetric(
    label: String,
    value: String,
    accent: Color,
    progress: Float? = null,
    rangeText: String? = null,
    onClick: (() -> Unit)? = null, // nullable allowed
    removeText: Boolean = false
) {
    val cardModifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 10.dp)

    val cardColors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.06f))
    val cardBorder = BorderStroke(1.dp, accent.copy(alpha = 0.18f))

    // Choose clickable or non-clickable card
    val cardContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val animated = progress?.let {
                animateFloatAsState(targetValue = it, animationSpec = spring()).value
            }

            // Circular progress OR static value
            Box(
                modifier = Modifier.size(68.dp),
                contentAlignment = Alignment.Center
            ) {
                if (animated != null) {
                    CircularProgressIndicator(
                        progress = { animated },
                        strokeWidth = 6.dp,
                        modifier = Modifier.matchParentSize(),
                        color = accent
                    )
                }
                Text(value, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.SemiBold)
                if (!rangeText.isNullOrEmpty()) {
                    Text(rangeText, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (animated != null) {
                    LinearProgressWithPercent(
                        progress = animated,
                        accent = accent,
                        removeText = removeText
                    )
                }
            }
        }
    }

    if (onClick != null) {
        // CLICKABLE CARD
        Card(
            modifier = cardModifier,
            shape = RoundedCornerShape(14.dp),
            onClick = onClick,
            border = cardBorder,
            colors = cardColors
        ) {
            cardContent()
        }
    } else {
        // NON-CLICKABLE CARD
        Card(
            modifier = cardModifier,
            shape = RoundedCornerShape(14.dp),
            border = cardBorder,
            colors = cardColors
        ) {
            cardContent()
        }
    }
}

@Composable
fun LinearProgressWithPercent(progress: Float, accent: Color, removeText: Boolean = false) {
    // row: progress bar + percent label
    Row(verticalAlignment = Alignment.CenterVertically) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp)),
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
            color = accent
        )
        if (!removeText) {
            Spacer(modifier = Modifier.width(12.dp))
            Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.SemiBold)
        }
    }
}
