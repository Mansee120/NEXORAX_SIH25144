@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.health.stridox.ui.main.home.screens.activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

enum class WorkoutCategory {
    ALL, CARDIO, STRENGTH, REHAB, YOGA
}

// Constants for metrics calculation (simplified)
private const val CALORIES_PER_SECOND_CARDIO = 0.15f // Approx 9 kcal/min
private const val CALORIES_PER_SECOND_STRENGTH = 0.10f // Approx 6 kcal/min

private fun formatTimer(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds)
}

// We need to move WorkoutList to be composable-aware or init it with context if we want to use resources
// But since this is a global list, we can keep the keys or use a provider.
// For simplicity in this refactor, I'll keep the list but the strings will need to be resolved at call site or
// we can change the 'name' to be an Int (ResId).
// However, since the user asked to replace hardcoded strings, I will refactor the Workout data class to use resource IDs for names.

data class WorkoutResource(
    val id: Int,
    val nameRes: Int,
    val category: WorkoutCategory,
    val durationMinutes: Int,
    val imageResId: Int,
    val pointsReward: Int
)

val WorkoutList = listOf(
    WorkoutResource(
        id = 1,
        nameRes = R.string.workout_treadmill,
        category = WorkoutCategory.CARDIO,
        durationMinutes = 30,
        imageResId = R.drawable.directions_walk,
        pointsReward = 25
    ),
    WorkoutResource(
        id = 2,
        nameRes = R.string.workout_bike,
        category = WorkoutCategory.CARDIO,
        durationMinutes = 45,
        imageResId = R.drawable.stationary_bike,
        pointsReward = 25
    ),
    WorkoutResource(
        id = 3,
        nameRes = R.string.workout_strength,
        category = WorkoutCategory.STRENGTH,
        durationMinutes = 40,
        imageResId = R.drawable.guided_strength,
        pointsReward = 25
    ),
    WorkoutResource(
        id = 4,
        nameRes = R.string.workout_yoga,
        category = WorkoutCategory.YOGA,
        durationMinutes = 20,
        imageResId = R.drawable.self_improvement,
        pointsReward = 25
    ),
    WorkoutResource(
        id = 5,
        nameRes = R.string.workout_rehab,
        category = WorkoutCategory.REHAB,
        durationMinutes = 15,
        imageResId = R.drawable.rehab_stretches,
        pointsReward = 25
    ),
)


// --- Composables ---

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IndoorTrainingScreen(
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

    val coroutineScope = rememberCoroutineScope()

    // --- State for tracking exercise ---
    var selectedCategory by rememberSaveable { mutableStateOf(WorkoutCategory.ALL) }
    var selectedWorkout by rememberSaveable { mutableStateOf<WorkoutResource?>(null) }
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var totalSecondsElapsed by rememberSaveable { mutableIntStateOf(0) }
    var caloriesBurned by rememberSaveable { mutableIntStateOf(0) }
    var currentHeartRate by rememberSaveable { mutableIntStateOf(0) }

    // Optional: track “movement ticks” like steps for this workout
    var movementCount by rememberSaveable { mutableIntStateOf(0) }

    val filteredWorkouts by remember(selectedCategory) {
        derivedStateOf {
            if (selectedCategory == WorkoutCategory.ALL) {
                WorkoutList
            } else {
                WorkoutList.filter { it.category == selectedCategory }
            }
        }
    }

    // Base calories/sec from workout type
    val caloriesPerSecondBase by remember(selectedWorkout) {
        derivedStateOf {
            when (selectedWorkout?.category) {
                WorkoutCategory.CARDIO -> CALORIES_PER_SECOND_CARDIO
                WorkoutCategory.STRENGTH -> CALORIES_PER_SECOND_STRENGTH
                else -> 0.05f // Default low burn rate
            }
        }
    }

    // --- Movement + intensity from SensorData when connected ---
    val MOVEMENT_THRESHOLD = 0.8f

    fun isMovementDetected(data: SensorData): Boolean {
        return (kotlin.math.abs(data.accelerometerX) > MOVEMENT_THRESHOLD ||
                  kotlin.math.abs(data.accelerometerY) > MOVEMENT_THRESHOLD ||
                  kotlin.math.abs(data.accelerometerZ) > MOVEMENT_THRESHOLD ||
                  kotlin.math.abs(data.gyroscopeX) > MOVEMENT_THRESHOLD ||
                  kotlin.math.abs(data.gyroscopeY) > MOVEMENT_THRESHOLD ||
                  kotlin.math.abs(data.gyroscopeZ) > MOVEMENT_THRESHOLD)
    }

    // Optional: intensity factor from HR (if we have it)
    fun intensityFactorFromHr(hr: Int): Float {
        if (hr <= 0) return 1f
        return when {
            hr < 90 -> 0.7f   // light
            hr < 120 -> 1.0f   // moderate
            hr < 150 -> 1.3f   // hard
            else -> 1.5f   // very hard
        }
    }

    // --- Timer, Calories, and Sensor Logic ---
    LaunchedEffect(isPlaying, connectedObservable, selectedWorkout) {
        if (!isPlaying) return@LaunchedEffect

        val targetDurationSeconds = selectedWorkout?.durationMinutes?.times(60) ?: Int.MAX_VALUE

        while (isPlaying && totalSecondsElapsed < targetDurationSeconds) {
            delay(1000)
            totalSecondsElapsed++

            if (connectedObservable) {
                // ✅ CONNECTED → use real sensor data

                val movementDetected = isMovementDetected(observedSensor)
                if (movementDetected) {
                    movementCount += 1
                }

                // Heart rate from sensor if available
                currentHeartRate = if (observedSensor.heartRate > 0) {
                    observedSensor.heartRate
                } else {
                    // fallback if sensor HR is 0
                    (100 + (totalSecondsElapsed % 20)).coerceAtMost(150)
                }

                // Scale calories by intensity & movement
                val intensityFactor = intensityFactorFromHr(currentHeartRate)
                val movementFactor = if (movementDetected) 1f else 0.3f // burn less if not moving
                val caloriesDelta =
                    caloriesPerSecondBase * intensityFactor * movementFactor

                caloriesBurned = (caloriesBurned + caloriesDelta).roundToInt()
            } else {
                // ❌ NOT CONNECTED → keep your dummy logic

                // Calories with base per-second rate
                caloriesBurned =
                    (caloriesBurned + caloriesPerSecondBase).roundToInt()

                // Simulate high-intensity HR during a workout
                currentHeartRate = (100..150).random()
            }

            // Check for completion
            if (totalSecondsElapsed >= targetDurationSeconds) {
                isPlaying = false
                selectedWorkout?.pointsReward?.let { reward ->
                    coroutineScope.launch {
                        val currentPoints = preferences.getPoints()
                        preferences.savePoints(currentPoints + reward)
                    }
                }
                selectedWorkout = null
            }
        }
    }

    // Reset metrics when a new workout is selected or stopped/cleared
    LaunchedEffect(selectedWorkout) {
        if (selectedWorkout == null) {
            isPlaying = false
            totalSecondsElapsed = 0
            caloriesBurned = 0
            currentHeartRate = 0
            movementCount = 0
        }
    }


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
                Text(stringResource(R.string.indoor_training_title), fontWeight = FontWeight.Bold)
            })
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            AnimatedVisibility(visible = selectedWorkout != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { selectedWorkout = null },
                        shape = CircleShape,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(45.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.refresh),
                            contentDescription = stringResource(R.string.refresh_content_description),
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Button(
                        onClick = { isPlaying = !isPlaying },
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                            contentDescription = if (isPlaying) stringResource(R.string.pause_content_description_indoor) else stringResource(
                                R.string.play_content_description_indoor
                            ),
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
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

            // Metrics Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MetricCard(
                        title = stringResource(R.string.timer_title),
                        value = formatTimer(totalSecondsElapsed),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = stringResource(R.string.calories_title_metric),
                        value = "$caloriesBurned " + stringResource(R.string.kcal_unit),
                        modifier = Modifier.weight(1f)
                    )
                }
                MetricCard(
                    title = stringResource(R.string.heart_rate_title),
                    value = if (currentHeartRate > 0) stringResource(
                        R.string.heart_rate_format,
                        currentHeartRate
                    ) else "-- " + stringResource(R.string.bpm_unit),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category Tabs
            PrimaryScrollableTabRow(
                selectedTabIndex = WorkoutCategory.entries.indexOf(selectedCategory),
                modifier = Modifier.padding(bottom = 8.dp, start = 12.dp, end = 16.dp),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                indicator = {},
                divider = {},
                minTabWidth = 10.dp
            ) {
                WorkoutCategory.entries.forEach { category ->
                    val isSelected = selectedCategory == category
                    Tab(
                        selected = isSelected,
                        onClick = {
                            selectedCategory = category
                            if (isPlaying) selectedWorkout =
                                null // Reset if filtering while playing
                        },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ) {
                        ToggleButton(
                            checked = isSelected,
                            onCheckedChange = { selectedCategory = category },
                            colors = ToggleButtonDefaults.toggleButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            val categoryName = when (category) {
                                WorkoutCategory.ALL -> stringResource(R.string.category_all)
                                WorkoutCategory.CARDIO -> stringResource(R.string.category_cardio)
                                WorkoutCategory.STRENGTH -> stringResource(R.string.category_strength)
                                WorkoutCategory.REHAB -> stringResource(R.string.category_rehab)
                                WorkoutCategory.YOGA -> stringResource(R.string.category_yoga)
                            }
                            Text(categoryName)
                        }
                    }
                }
            }

            // Workout List
            Text(
                stringResource(R.string.choose_workout_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textAlign = TextAlign.Start
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .animateContentSize(animationSpec = tween(durationMillis = 300)),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                filteredWorkouts.forEach { workout ->
                    WorkoutCard(
                        workout = workout,
                        isSelected = selectedWorkout?.id == workout.id,
                        onClick = {
                            selectedWorkout = if (selectedWorkout?.id == it.id) null else it
                        }
                    )
                }
            }

            // Ensure space for FAB
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
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
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun outlinedCardBorder(enabled: Boolean = true): BorderStroke {
    val color =
        if (enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        }
    return remember(color) { BorderStroke(1.dp, color) }
}

@Composable
fun WorkoutCard(workout: WorkoutResource, isSelected: Boolean, onClick: (WorkoutResource) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(workout) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceColorAtElevation(
                3.dp
            ),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = outlinedCardBorder(enabled = isSelected)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(workout.nameRes),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                val categoryName = when (workout.category) {
                    WorkoutCategory.ALL -> stringResource(R.string.category_all)
                    WorkoutCategory.CARDIO -> stringResource(R.string.category_cardio)
                    WorkoutCategory.STRENGTH -> stringResource(R.string.category_strength)
                    WorkoutCategory.REHAB -> stringResource(R.string.category_rehab)
                    WorkoutCategory.YOGA -> stringResource(R.string.category_yoga)
                }
                
                Text(
                    "${workout.durationMinutes} " + stringResource(R.string.min_unit) + " • $categoryName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Placeholder for workout image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                // Use a simple icon/text placeholder as I don't have the required drawables (treadmill_run, etc.)
                Icon(
                    painter = painterResource(workout.imageResId),
                    contentDescription = stringResource(workout.nameRes),
                    modifier = Modifier.size(70.dp)
                )
            }
        }
    }
}