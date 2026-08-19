package com.health.stridox.navigation

import androidx.navigation3.runtime.NavKey
import com.health.stridox.R
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
sealed class BottomScreens<NavKey>(
    val name: Int,
    @Contextual
    val selectedIcon: Int,
    @Contextual
    val unselectedIcon: Int,
    val route: NavKey,
) {
    @Serializable
    data object HealthScreen : BottomScreens<NavKey>(
        name = R.string.health,
        unselectedIcon = R.drawable.health,
        selectedIcon = R.drawable.healthfilled,
        route = Health
    )

    @Serializable
    data object DoctorsScreen : BottomScreens<NavKey>(
        name = R.string.doctors,
        unselectedIcon = R.drawable.clinical,
        selectedIcon = R.drawable.clinicalfilled,
        route = Doctors
    )
//
//    @Serializable
//    data object RemindersScreen : BottomScreens<NavKey>(
//        name = R.string.reminders,
//        unselectedIcon = R.drawable.alert,
//        selectedIcon = R.drawable.alertfilled,
//        route = Reminders
//    )

    @Serializable
    data object DevicesScreen : BottomScreens<NavKey>(
        name = R.string.devices,
        unselectedIcon = R.drawable.devices,
        selectedIcon = R.drawable.devices_filled,
        route = Devices
    )

    @Serializable
    data object MoreScreen : BottomScreens<NavKey>(
        name = R.string.more,
        unselectedIcon = R.drawable.more,
        selectedIcon = R.drawable.more,
        route = More
    )
}


@Serializable
object Health : NavKey

@Serializable
object Dashboard : NavKey

@Serializable
object Games : NavKey

@Serializable
object Breathing : NavKey

@Serializable
object Walking : NavKey

@Serializable
object PostureWithImage : NavKey

@Serializable
object IndoorTraining : NavKey

@Serializable
object OutdoorTraining : NavKey

@Serializable
object HeartRate : NavKey

@Serializable
object Spo2 : NavKey

@Serializable
object Step : NavKey

@Serializable
object Weight : NavKey

@Serializable
object Calories : NavKey

@Serializable
object Doctors : NavKey
@Serializable
object Reminders : NavKey
@Serializable
object Devices : NavKey
@Serializable
object More : NavKey

@Serializable
object PrivacyPolicy : NavKey

@Serializable
object Help : NavKey

@Serializable
object PersonalInfo : NavKey


@Serializable
object Home : NavKey
@Serializable
object Login : NavKey
@Serializable
object Register : NavKey

@Serializable
object Cadence : NavKey

@Serializable
object StepConsistency : NavKey

@Serializable
object ExertionIndex : NavKey

@Serializable
object Mobility : NavKey
