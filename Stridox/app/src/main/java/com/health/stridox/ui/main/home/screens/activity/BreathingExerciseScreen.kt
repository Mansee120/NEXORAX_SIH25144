package com.health.stridox.ui.main.home.screens.activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.stridox.R
import com.health.stridox.domain.Preferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.Locale

// Data structures for breathing
private data class BreathingCycle(
    val inhale: Int, val holdIn: Int, val exhale: Int, val holdOut: Int
) {
    val totalDuration get() = inhale + holdIn + exhale + holdOut
}

private enum class BreathingState {
    IDLE, INHALING, HOLD_IN, EXHALING, HOLD_OUT
}

// Map tab index to breathing cycle structure
private fun getExerciseCycle(index: Int): BreathingCycle {
    return when (index) {
        0 -> BreathingCycle(
            inhale = 4,
            holdIn = 4,
            exhale = 4,
            holdOut = 4
        ) // Box Breathing
        1 -> BreathingCycle(
            inhale = 4,
            holdIn = 7,
            exhale = 8,
            holdOut = 0
        ) // 4-7-8 Relax
        2 -> BreathingCycle(
            inhale = 5,
            holdIn = 0,
            exhale = 5,
            holdOut = 0
        ) // Deep Relaxation
        else -> BreathingCycle(
            inhale = 4,
            holdIn = 0,
            exhale = 4,
            holdOut = 0
        )
    }
}

// Helper to get instruction text
@Composable
private fun getInstruction(
    state: BreathingState,
    duration: Int
): String {
    return when (state) {
        BreathingState.INHALING -> stringResource(R.string.instruction_inhale, duration)
        BreathingState.HOLD_IN -> stringResource(R.string.instruction_hold, duration)
        BreathingState.EXHALING -> stringResource(R.string.instruction_exhale, duration)
        BreathingState.HOLD_OUT -> stringResource(R.string.instruction_hold, duration)
        BreathingState.IDLE -> stringResource(R.string.instruction_idle)
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BreathingExerciseScreen(
    navBack: () -> Unit, rewardPoints: Int = 25, // Reward points for session completion
    preferences: Preferences = koinInject() // Inject Preferences
) {
    val tabs = listOf(
        stringResource(R.string.exercise_box_breathing),
        stringResource(R.string.exercise_478_relax),
        stringResource(R.string.exercise_deep_relaxation)
    )
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var durationMinutes by rememberSaveable { mutableFloatStateOf(5f) }
    var soundFeedbackEnabled by rememberSaveable { mutableStateOf(true) }
    var hapticsEnabled by rememberSaveable { mutableStateOf(true) }

    // State for breathing logic
    var currentBreathingState by rememberSaveable { mutableStateOf(BreathingState.IDLE) }
    var secondsRemainingInPhase by rememberSaveable { mutableIntStateOf(0) }
    var currentCycle by rememberSaveable { mutableIntStateOf(0) }
    var totalSecondsElapsed by rememberSaveable { mutableIntStateOf(0) }

    val coroutineScope = rememberCoroutineScope()
    val totalSessionSeconds = (durationMinutes * 60).toInt()
    val cycleStructure = getExerciseCycle(selectedTabIndex)
    val totalCycles = (totalSessionSeconds / cycleStructure.totalDuration).coerceAtLeast(1)

    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current // For sound implementation

    val performReset = {
        isPlaying = false
        currentBreathingState = BreathingState.IDLE
        currentCycle = 0
        totalSecondsElapsed = 0
        secondsRemainingInPhase = 0

        if (hapticsEnabled) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        if (soundFeedbackEnabled) {
            // TODO: Implement sound playback logic (e.g., a simple beep for reset)
        }
    }

    // Reset logic when tab changes or duration changes
    LaunchedEffect(selectedTabIndex, durationMinutes) {
        isPlaying = false
        currentBreathingState = BreathingState.IDLE
        currentCycle = 0
        totalSecondsElapsed = 0
        secondsRemainingInPhase = 0
    }

    // Breathing Logic (The state machine)
    LaunchedEffect(isPlaying) {
        if (!isPlaying) {
            currentBreathingState = BreathingState.IDLE
            secondsRemainingInPhase = 0
            return@LaunchedEffect
        }

        // Initialize on play
        if (currentCycle == 0) {
            currentCycle = 1
            currentBreathingState = BreathingState.INHALING
            secondsRemainingInPhase = cycleStructure.inhale
        }

        while (isPlaying && totalSecondsElapsed < totalSessionSeconds) {

            // Advance state machine every second
            if (secondsRemainingInPhase > 0) {
                delay(1000)
                secondsRemainingInPhase--
                totalSecondsElapsed++
            } else {
                // Transition to next phase
                val nextState = when (currentBreathingState) {
                    BreathingState.INHALING -> {
                        if (cycleStructure.holdIn > 0) BreathingState.HOLD_IN else BreathingState.EXHALING
                    }

                    BreathingState.HOLD_IN -> BreathingState.EXHALING
                    BreathingState.EXHALING -> {
                        if (cycleStructure.holdOut > 0) BreathingState.HOLD_OUT else BreathingState.INHALING
                    }

                    BreathingState.HOLD_OUT -> BreathingState.INHALING
                    BreathingState.IDLE -> BreathingState.INHALING // Should not happen in loop
                }

                // Determine new phase duration
                val newDuration = when (nextState) {
                    BreathingState.INHALING -> {
                        // Check if session is over before starting a new cycle
                        if (totalSecondsElapsed + cycleStructure.totalDuration > totalSessionSeconds) {
                            isPlaying = false
                            currentBreathingState = BreathingState.IDLE
                            totalSecondsElapsed = totalSessionSeconds
                            // Session Completed - Reward Points Logic
                            coroutineScope.launch {
                                val currentPoints = preferences.getPoints()
                                preferences.savePoints(currentPoints + rewardPoints)
                            }
                            break
                        }
                        currentCycle++
                        cycleStructure.inhale
                    }

                    BreathingState.HOLD_IN -> cycleStructure.holdIn
                    BreathingState.EXHALING -> cycleStructure.exhale
                    BreathingState.HOLD_OUT -> cycleStructure.holdOut
                    BreathingState.IDLE -> 0
                }

                currentBreathingState = nextState
                secondsRemainingInPhase = newDuration
            }
        }

        // Final state check if loop exits due to totalSessionSeconds limit
        if (totalSecondsElapsed >= totalSessionSeconds) {
            isPlaying = false
            currentBreathingState = BreathingState.IDLE
            // Session Completed - Reward Points Logic
            coroutineScope.launch {
                val currentPoints = preferences.getPoints()
                preferences.savePoints(currentPoints + rewardPoints)
            }
        }
    }

    // Determine current phase duration for animation
    val currentPhaseDuration = when (currentBreathingState) {
        BreathingState.INHALING -> cycleStructure.inhale
        BreathingState.EXHALING -> cycleStructure.exhale
        else -> 1 // Dummy for holds/idle
    }

    // Determine target value for animation
    val targetAnimationValue = when (currentBreathingState) {
        BreathingState.INHALING -> 1f
        BreathingState.EXHALING -> 0f
        else -> {
            // For HOLD_IN, maintain 1f (max size). For HOLD_OUT, maintain 0f (min size).
            // For IDLE, default to 0f.
            when (currentBreathingState) {
                BreathingState.HOLD_IN -> 1f
                BreathingState.HOLD_OUT -> 0f
                else -> 0f
            }
        }
    }

    // Animation progress logic
    val animationProgress by animateFloatAsState(
        targetValue = targetAnimationValue, animationSpec = when (currentBreathingState) {
            BreathingState.INHALING, BreathingState.EXHALING -> tween(
                durationMillis = currentPhaseDuration * 1000, easing = LinearEasing
            )
            // Holds and Idle should transition instantly or maintain state
            else -> tween(durationMillis = 1000)
        }, label = "breathingAnimation"
    )

    // Timer display (MM:SS)
    val remainingTime = totalSessionSeconds - totalSecondsElapsed
    val displayMinutes = remainingTime / 60
    val displaySeconds = remainingTime % 60
    val timerText = if (remainingTime <= 0) "00:00" else String.format(
        Locale.ENGLISH, "%02d:%02d", displayMinutes, displaySeconds
    )

    // Instruction text
    val instructionDuration = when (currentBreathingState) {
        BreathingState.INHALING -> cycleStructure.inhale
        BreathingState.HOLD_IN -> cycleStructure.holdIn
        BreathingState.EXHALING -> cycleStructure.exhale
        BreathingState.HOLD_OUT -> cycleStructure.holdOut
        BreathingState.IDLE -> cycleStructure.inhale // Show inhale duration as default instruction
    }
    val instructionText = getInstruction(currentBreathingState, instructionDuration)

    // Breathing action text
    val actionText = when (currentBreathingState) {
        BreathingState.INHALING -> stringResource(R.string.action_inhale)
        BreathingState.HOLD_IN -> stringResource(R.string.action_hold)
        BreathingState.EXHALING -> stringResource(R.string.action_exhale)
        BreathingState.HOLD_OUT -> stringResource(R.string.action_hold)
        BreathingState.IDLE -> stringResource(R.string.action_ready)
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
                Text(
                    stringResource(R.string.breathing_exercises_title),
                    fontWeight = FontWeight.Bold
                )
            }, actions = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.workspace_premium),
                        contentDescription = stringResource(R.string.points_content_description),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "+$rewardPoints",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            })
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .animateContentSize()
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(!isPlaying) {
                // Tabs for different exercises
                PrimaryScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    indicator = {},
                    divider = {}) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTabIndex == index
                        Tab(
                            selected = isSelected,
                            onClick = { selectedTabIndex = index },
                            modifier = Modifier.padding(horizontal = 4.dp),
                            enabled = !isPlaying, // Disable tab changes while playing
                            selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ) {
                            ToggleButton(
                                checked = isSelected,
                                onCheckedChange = {
                                    selectedTabIndex = index
                                },
                                enabled = !isPlaying, // Disable button while playing
                                colors = ToggleButtonDefaults.toggleButtonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(title)
                            }
                        }
                    }
                }
            }

            // Breathing Animation
            Box(
                modifier = Modifier
                    .animateContentSize()
                    .size(250.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent), contentAlignment = Alignment.Center
            ) {
                // Outer ring 1 (simulating dark green from image)
                Box(
                    modifier = Modifier
                        .animateContentSize()
                        .size(250.dp * (0.8f + 0.2f * animationProgress))
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                )
                // Outer ring 2
                Box(
                    modifier = Modifier
                        .animateContentSize()
                        .size(220.dp * (0.8f + 0.2f * animationProgress))
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                )
                // Center Circle (The animated part)
                Box(
                    modifier = Modifier
                        .animateContentSize()
                        .size(190.dp * (0.8f + 0.2f * animationProgress))
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    val color = MaterialTheme.colorScheme.onPrimary
                    Text(
                        text = actionText,
                        color = { color },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Instructions and Timer
            Text(
                instructionText,
                modifier = Modifier.padding(horizontal = 32.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                timerText, // Dynamic timer
                fontSize = 36.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                stringResource(
                    R.string.cycles_format,
                    currentCycle.coerceAtMost(totalCycles),
                    totalCycles
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Play/Pause and Reset Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play/Pause Button
                Button(
                    onClick = {
                        isPlaying = !isPlaying
                        if (hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        if (soundFeedbackEnabled) {
                            // TODO: Play sound for play/pause
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        painter = if (isPlaying) painterResource(R.drawable.pause)
                        else painterResource(R.drawable.play),
                        contentDescription = if (isPlaying) stringResource(R.string.pause_desc) else stringResource(
                            R.string.play_desc
                        ),
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(Modifier.width(8.dp))
                // Reset Button (Visible only when playing)
                AnimatedVisibility(totalSecondsElapsed != 0) {
                    IconButton(
                        onClick = performReset,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.refresh),
                            contentDescription = stringResource(R.string.reset_desc),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Session Settings Card
            AnimatedVisibility(!isPlaying) {
                Column {
                    Spacer(modifier = Modifier.height(18.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            stringResource(R.string.session_settings_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Duration Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.duration_label),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${durationMinutes.toInt()} " + stringResource(R.string.min_unit),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = durationMinutes,
                            onValueChange = { durationMinutes = it },
                            valueRange = 1f..30f,
                            steps = 29,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isPlaying // Disable slider while playing
                        )

//                        // Sound Feedback Toggle
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 8.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text("Sound Feedback", modifier = Modifier.weight(1f))
//                            Switch(
//                                checked = soundFeedbackEnabled,
//                                onCheckedChange = { soundFeedbackEnabled = it },
//                                enabled = !isPlaying // Disable switch while playing
//                            )
//                        }

                        // Haptics Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.haptics_label),
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = hapticsEnabled,
                                onCheckedChange = { hapticsEnabled = it },
                                enabled = !isPlaying // Disable switch while playing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
        }
    }
}