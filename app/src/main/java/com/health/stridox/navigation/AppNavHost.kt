@file:OptIn(
    ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class
)

package com.health.stridox.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.health.stridox.ui.main.home.screens.activity.BreathingExerciseScreen
import com.health.stridox.ui.main.home.screens.activity.DashboardScreen
import com.health.stridox.ui.main.home.screens.activity.GamesScreen
import com.health.stridox.ui.main.home.screens.activity.IndoorTrainingScreen
import com.health.stridox.ui.main.home.screens.activity.OutdoorTrainingScreen
import com.health.stridox.ui.main.home.screens.activity.PostureAnalysisWithImageScreen
import com.health.stridox.ui.main.home.screens.activity.WalkingExerciseScreen
import com.health.stridox.ui.main.home.screens.calories.CaloriesScreen
import com.health.stridox.ui.main.home.screens.device.DeviceScreen
import com.health.stridox.ui.main.home.screens.doctor.DoctorsScreen
import com.health.stridox.ui.main.home.screens.health.CadenceScreen
import com.health.stridox.ui.main.home.screens.health.ExertionIndexScreen
import com.health.stridox.ui.main.home.screens.health.HealthScreen
import com.health.stridox.ui.main.home.screens.health.MobilityScreen
import com.health.stridox.ui.main.home.screens.health.StepConsistencyScreen
import com.health.stridox.ui.main.home.screens.heart.HeartRateScreen
import com.health.stridox.ui.main.home.screens.profile.HelpScreen
import com.health.stridox.ui.main.home.screens.profile.MoreScreen
import com.health.stridox.ui.main.home.screens.profile.PersonalInfoScreen
import com.health.stridox.ui.main.home.screens.profile.PrivacyPolicyScreen
import com.health.stridox.ui.main.home.screens.reminder.RemindersScreen
import com.health.stridox.ui.main.home.screens.spo.SpO2Screen
import com.health.stridox.ui.main.home.screens.steps.StepsScreen
import com.health.stridox.ui.main.home.screens.weight.WeightDetailsScreen

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppNavHost(navigator: Navigator, onLoggedOut: () -> Unit) {
    NavDisplay(
        entries = navigator.state.toEntries(entryProvider {
            entry<Health> {
                HealthScreen(
                    navToHeartRate = { navigator.navigate(HeartRate) },
                    navToSpO2 = { navigator.navigate(Spo2) },
                    navToStep = { navigator.navigate(Step) },
                    navToWeight = { navigator.navigate(Weight) },
                    navToCalories = { navigator.navigate(Calories) },
                    navToDashboard = { navigator.navigate(Dashboard) },
                    navToCadence = { navigator.navigate(Cadence) },
                    navToStepConsistency = { navigator.navigate(StepConsistency) },
                    navToExertionIndex = { navigator.navigate(ExertionIndex) },
                    navToMobility = { navigator.navigate(Mobility) }
                )
            }
            entry<HeartRate> {
                HeartRateScreen(
                    onBackClick = {
                        navigator.goBack()
                    })
            }
            entry<Spo2> {
                SpO2Screen(
                    onBackClick = {
                        navigator.goBack()
                    })
            }
            entry<Step> {
                StepsScreen(
                    onBackClick = {
                        navigator.goBack()
                    })
            }
            entry<Weight> {
                WeightDetailsScreen(
                    onBackClick = {
                        navigator.goBack()
                    })
            }
            entry<Calories> {
                CaloriesScreen(
                    onBackClick = {
                        navigator.goBack()
                    })
            }
            entry<Doctors> {
                DoctorsScreen()
            }
            entry<Reminders> {
                RemindersScreen(navBack = { navigator.goBack() })
            }
            entry<Devices> {
                DeviceScreen()
            }
            entry<More> {
                MoreScreen(
                    onLoggedOut = onLoggedOut,
                    navToPrivacyPolicy = { navigator.navigate(PrivacyPolicy) },
                    navToHelp = { navigator.navigate(Help) },
                    navToPersonalInfo = { navigator.navigate(PersonalInfo) }
                )
            }

            entry<PersonalInfo> {
                PersonalInfoScreen(
                    navBack = { navigator.goBack() }
                )
            }

            entry<Dashboard> {
                DashboardScreen(
                    navBack = { navigator.goBack() },
                    navToGames = { navigator.navigate(Games) }
                )
            }
            entry<Games> {
                GamesScreen(
                    navToBreathingExercises = {
                        navigator.navigate(Breathing)
                    },
                    navToWalkingExercises = {
                        navigator.navigate(Walking)
                    },
                    navToPostureWithImageExercises = {
                        navigator.navigate(PostureWithImage)
                    },
                    navToIndoorTraining = {
                        navigator.navigate(IndoorTraining)
                    },
                    navToOutdoorTraining = {
                        navigator.navigate(OutdoorTraining)
                    },
                    navBack = {
                        navigator.goBack()
                    })
            }
            entry<Breathing> {
                BreathingExerciseScreen(
                    navBack = {
                        navigator.goBack()
                    })
            }
            entry<Walking> {
                WalkingExerciseScreen(
                    navBack = {
                        navigator.goBack()
                    })
            }
            entry<PostureWithImage> {
                PostureAnalysisWithImageScreen(
                    navBack = {
                        navigator.goBack()
                    })
            }
            entry<IndoorTraining> {
                IndoorTrainingScreen(
                    navBack = {
                        navigator.goBack()
                    })
            }
            entry<OutdoorTraining> {
                OutdoorTrainingScreen(
                    navBack = {
                        navigator.goBack()
                    })
            }
            entry<PrivacyPolicy> {
                PrivacyPolicyScreen(
                    navBack = {
                        navigator.goBack()
                    }
                )
            }
            entry<Help> {
                HelpScreen(
                    navBack = {
                        navigator.goBack()
                    }
                )
            }
            entry<Cadence> {
                CadenceScreen(
                    onBackClick = {
                        navigator.goBack()
                    }
                )
            }
            entry<StepConsistency> {
                StepConsistencyScreen(
                    onBackClick = {
                        navigator.goBack()
                    }
                )
            }
            entry<ExertionIndex> {
                ExertionIndexScreen(
                    onBackClick = {
                        navigator.goBack()
                    }
                )
            }
            entry<Mobility> {
                MobilityScreen(
                    onBackClick = {
                        navigator.goBack()
                    }
                )
            }
        }),
        onBack = { navigator.goBack() },
        transitionSpec = {
            ContentTransform(
                slideInHorizontally(initialOffsetX = { it }),
                slideOutHorizontally(targetOffsetX = { -it })
            )
        },
        popTransitionSpec = {
            ContentTransform(
                slideInHorizontally(initialOffsetX = { -it }),
                slideOutHorizontally(targetOffsetX = { it })
            )
        },
        predictivePopTransitionSpec = {
            ContentTransform(
                slideInHorizontally(initialOffsetX = { -it }),
                slideOutHorizontally(targetOffsetX = { it })
            )
        },
    )
}
