package com.health.stridox.ui.main.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.health.stridox.domain.Preferences
import com.health.stridox.navigation.AppNavHost
import com.health.stridox.navigation.BottomScreens
import com.health.stridox.navigation.Navigator
import com.health.stridox.navigation.rememberNavigationState
import com.health.stridox.ui.main.LocalTriggerActivated
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import org.koin.compose.koinInject

@Composable
fun HomeScreen(
    onLoggedOut: () -> Unit,
    preferences: Preferences = koinInject()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ✅ FIXED: Get trigger state from CompositionLocal (reactive)
    val triggerActivated = LocalTriggerActivated.current

    // Initialize Fused Location Client
    val fusedLocationClient: FusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // ✅ Local state for emergency countdown (separate from trigger)
    var isEmergencyCountdownActive by remember { mutableStateOf(false) }
    var emergencyTimer by remember { mutableIntStateOf(5) }

    // Permission Launcher for Call, SMS, and Location
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val callGranted = permissions[Manifest.permission.CALL_PHONE] ?: false
        val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false

        if (callGranted && smsGranted && locationGranted) {
            scope.launch {
                val emergencyNumber = preferences.getLoginResponse()?.emergencyNumber
                if (!emergencyNumber.isNullOrEmpty()) {
                    val location = getCurrentLocation(context, fusedLocationClient)
                    makePhoneCall(context, emergencyNumber)
                    sendEmergencySms(context, emergencyNumber, location)
                } else {
                    Toast.makeText(context, "Emergency number not set", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(
                context,
                "Call, SMS, and Location permissions are required",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Function to initiate call and SMS
    fun initiateEmergencyCall() {
        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val hasSmsPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasBackgroundLocation =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

        val hasForegroundLocation =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.FOREGROUND_SERVICE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }


        if (hasCallPermission && hasSmsPermission && (hasLocationPermission || hasBackgroundLocation || hasForegroundLocation)) {
            scope.launch {
                val emergencyNumber = preferences.getLoginResponse()?.emergencyNumber
                if (!emergencyNumber.isNullOrEmpty()) {
                    val location = getCurrentLocation(context, fusedLocationClient)
                    makePhoneCall(context, emergencyNumber)
                    sendEmergencySms(context, emergencyNumber, location)
                } else {
                    Toast.makeText(context, "Emergency number not set", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "NO PERMISSIONS GRANTED, PLEASE ALLOW", Toast.LENGTH_SHORT)
                .show()
            permissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    // ✅ FIXED: Listen to trigger activation from device
    LaunchedEffect(triggerActivated) {
        if (triggerActivated) {
            Log.d("HomeScreen", "✓ Trigger received from device!")
            isEmergencyCountdownActive = true
            emergencyTimer = 5
        }
    }

    // ✅ Timer Logic for Emergency Call
    LaunchedEffect(isEmergencyCountdownActive) {
        if (isEmergencyCountdownActive) {
            while (emergencyTimer > 0) {
                delay(1000)
                emergencyTimer--
            }
            // Call after countdown completes
            if (isEmergencyCountdownActive) {
                initiateEmergencyCall()
                isEmergencyCountdownActive = false
            }
        }
    }

    // Create the list of top-level navigation items
    val navigationItems = remember {
        listOf(
            BottomScreens.HealthScreen,
            BottomScreens.DoctorsScreen,
            BottomScreens.DevicesScreen,
            BottomScreens.MoreScreen
        )
    }

    // Build a navigation state with the first item as the start route
    val navigationState = rememberNavigationState(
        startRoute = BottomScreens.HealthScreen.route,
        topLevelRoutes = navigationItems.map { it.route }.toSet()
    )

    // Create a navigator that drives your NavHost
    val navigator = remember { Navigator(navigationState) }

    NavigationSuiteScaffold(
        modifier = Modifier.animateContentSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        navigationSuiteItems = {
            navigationItems.forEachIndexed { index, item ->
                val isSelected = item.route == navigationState.topLevelRoute

                item(
                    selected = isSelected,
                    onClick = { navigator.navigate(item.route) },
                    label = {
                        Text(
                            text = stringResource(item.name),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    },
                    icon = {
                        Icon(
                            painter = painterResource(if (isSelected) item.selectedIcon else item.unselectedIcon),
                            contentDescription = stringResource(item.name)
                        )
                    }
                )
            }
        },
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = MaterialTheme.colorScheme.surface,
            navigationRailContainerColor = MaterialTheme.colorScheme.surface,
            navigationDrawerContainerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
        Scaffold(contentWindowInsets = WindowInsets()) { _ ->

            AppNavHost(navigator = navigator, onLoggedOut = onLoggedOut)

            // ✅ Show Emergency Alert Dialog
            if (isEmergencyCountdownActive) {
                AlertDialog(
                    onDismissRequest = { isEmergencyCountdownActive = false },
                    title = { Text("🚨 Emergency Alert", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Emergency call and message initiating in:",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "$emergencyTimer",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text("seconds", style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { isEmergencyCountdownActive = false },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("STOP EMERGENCY CALL AND MSG")
                        }
                    }
                )
            }
        }
    }
}

fun isLocationServiceAvailable(context: Context): Boolean {
    val availability = GoogleApiAvailability.getInstance()
        .isGooglePlayServicesAvailable(context)
    return availability == ConnectionResult.SUCCESS
}

@SuppressLint("MissingPermission")
suspend fun getCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient
): String {
    if (!isLocationServiceAvailable(context)) {
        Log.e("HomeScreen", "Google Play Services Location API not available on this device")
        return "Location service is not available on this device. Cannot get my location."
    }

    return try {
        // Use last known location
        var location = fusedLocationClient.lastLocation.await()

        // 2) If null (common in background), request a fresh update
        if (location == null) {
            Log.d("HomeScreen", "lastLocation is null, requesting single update…")
            location = fusedLocationClient.awaitSingleUpdate()
        }

        location?.let {
            // Format location as a Google Maps URL
            "My current location is: https://maps.google.com/?q=${it.latitude},${it.longitude}"
        } ?: "My current location is unknown (null location)."
    } catch (_: SecurityException) {
        "Location permission denied. My location is unknown (SecurityException)."
    } catch (e: Exception) {
        Log.e("HomeScreen", "Error getting location: ${e.message}")
        "Failed to get my location."
    }
}

@SuppressLint("MissingPermission")
private suspend fun FusedLocationProviderClient.awaitSingleUpdate(): android.location.Location? =
    suspendCancellableCoroutine { cont ->
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L // 1 second interval (not really used for single update)
        )
            .setWaitForAccurateLocation(true)
            .setMaxUpdates(1)
            .build()

        val callback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                removeLocationUpdates(this)
                if (!cont.isCompleted) {
                    cont.resume(result.lastLocation) { cause, _, _ -> cause.printStackTrace() }
                }
            }

            override fun onLocationAvailability(availability: com.google.android.gms.location.LocationAvailability) {
                if (!availability.isLocationAvailable) {
                }
            }
        }

        requestLocationUpdates(request, callback, Looper.getMainLooper())

        cont.invokeOnCancellation {
            removeLocationUpdates(callback)
        }
    }

fun makePhoneCall(context: Context, number: String) {
    try {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = "tel:$number".toUri()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to make call", Toast.LENGTH_SHORT).show()
    }
}

fun sendEmergencySms(context: Context, number: String, locationMessage: String) {
    try {
        val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            context.getSystemService(android.telephony.SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            android.telephony.SmsManager.getDefault()
        }

        val message = "Emergency! I need help. $locationMessage"

        smsManager.sendTextMessage(number, null, message, null, null)
        Toast.makeText(context, "Emergency SMS sent", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to send SMS", Toast.LENGTH_SHORT).show()
    }
}