package com.health.stridox.ui.main.home.screens.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import com.health.stridox.R
import com.health.stridox.util.MLKitPostureAnalyzer
import com.health.stridox.util.PostureAnalyzer

// --- Data Structures ---

data class PostureResult(
    val name: String,
    val status: PostureStatus
)

enum class PostureStatus(val iconId: Int, val color: @Composable () -> Color) {
    GOOD(R.drawable.check_circle, { MaterialTheme.colorScheme.primary }),
    WARNING(R.drawable.warning, { MaterialTheme.colorScheme.tertiary }),
    BAD(R.drawable.error, { MaterialTheme.colorScheme.error })
}

data class AnalysisOutput(
    val score: Int,
    val results: List<PostureResult>,
    val improvementAreas: List<String>
)

private fun getRequiredPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}
// --- Composables ---

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PostureAnalysisWithImageScreen(
    navBack: () -> Unit,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val analyzer: PostureAnalyzer = remember { MLKitPostureAnalyzer(context) }
    var analysisStarted by rememberSaveable { mutableStateOf(false) }
    var isAnalyzing by rememberSaveable { mutableStateOf(false) }
    var postureScore by rememberSaveable { mutableIntStateOf(0) }
    var results by remember { mutableStateOf<List<PostureResult>>(emptyList()) }
    var improvementAreas by remember { mutableStateOf<List<String>>(emptyList()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showPermissionDeniedDialog by rememberSaveable { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current

    var isPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                getRequiredPermission()
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Re-check when coming back from Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isPermissionGranted = ContextCompat.checkSelfPermission(
                    context,
                    getRequiredPermission()
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            imageUri = uri
            if (uri != null) {
                // Reset state and start analysis process
                isAnalyzing = true
                analysisStarted = false
                postureScore = 0
                results = emptyList()
                improvementAreas = emptyList()
            }
        }
    )

    // Launcher for requesting permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, launch the image picker
                imagePicker.launch("image/*")
            } else {
                // Permission denied. Check if it's a permanent denial (user checked "Don't ask again")
                val isPermanentlyDenied = activity?.let {
                    !ActivityCompat.shouldShowRequestPermissionRationale(
                        it,
                        getRequiredPermission()
                    )
                }
                if (isPermanentlyDenied == true) {
                    showPermissionDeniedDialog = true
                }
            }
        }
    )

    // Trigger analysis when isAnalyzing is true and an image is available
    LaunchedEffect(isAnalyzing) {
        if (isAnalyzing && imageUri != null) {
            val output = analyzer.analyze(imageUri!!)

            postureScore = output.score
            results = output.results
            improvementAreas = output.improvementAreas

            isAnalyzing = false
            analysisStarted = true
        }
    }

    // --- Alert Dialog for Permanent Denial ---
    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = { Text(stringResource(R.string.permission_required_title)) },
            text = { Text(stringResource(R.string.permission_denied_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDeniedDialog = false
                        // Navigate user to app settings
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        activity?.startActivity(intent)
                    }
                ) {
                    Text(stringResource(R.string.go_to_settings_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDeniedDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
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
                Text(stringResource(R.string.posture_analysis_title), fontWeight = FontWeight.Bold)
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
            if (postureScore == 0 && isPermissionGranted) {
                Text(
                    stringResource(R.string.select_image_hint),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Permission Warning/Request area
            if (!isPermissionGranted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.permission_required_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.grant_permission_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }


            // Posture Image/Placeholder
            PostureImagePlaceholder(isAnalyzing, imageUri)

            Spacer(modifier = Modifier.height(24.dp))

            // Start Analysis Button
            Button(
                onClick = {
                    if (!isAnalyzing) {
                        if (isPermissionGranted) {
                            // Launch the photo picker only if permission is granted
                            imagePicker.launch("image/*")
                        } else {
                            // Request permissions using the launcher
                            permissionLauncher.launch(getRequiredPermission())
                        }
                    }
                },
                enabled = !isAnalyzing,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.analyzing_status),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (!isPermissionGranted) {
                    Text(
                        stringResource(R.string.grant_permission_restart_button),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        stringResource(R.string.start_analysis_button),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Results Section
            AnimatedVisibility(analysisStarted) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    Text(
                        stringResource(R.string.your_results_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    PostureResultsCard(postureScore, results)
                }
            }

            // Areas to Improve Section
            AnimatedVisibility(analysisStarted && improvementAreas.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    Text(
                        stringResource(R.string.areas_to_improve_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    improvementAreas.forEach { area ->
                        ImprovementAreaCard(title = area)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PostureImagePlaceholder(isAnalyzing: Boolean, imageUri: Uri?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                // Display the selected image
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Selected Posture Image",
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Overlay for status
            if (isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.analyzing_image_overlay),
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }
            } else if (imageUri == null) {
                // Initial Placeholder
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(R.drawable.photo_camera),
                        contentDescription = stringResource(R.string.capture_posture_desc),
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.tap_to_pick_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PostureResultsCard(score: Int, results: List<PostureResult>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                3.dp
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Score Progress Indicator
            val animatedProgress by animateFloatAsState(
                targetValue = score / 100f,
                animationSpec = spring(),
                label = "scoreProgress"
            )
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.matchParentSize(),
                    strokeWidth = 8.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        score.toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "/100",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Result List
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                results.take(3).forEach { result ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(result.status.iconId),
                            contentDescription = null,
                            tint = result.status.color(),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            result.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImprovementAreaCard(title: String, details: String = "Example suggestions for improvement.") {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
//            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                3.dp
            )
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
//                Icon(
//                    painter = if (expanded) painterResource(R.drawable.keyboard_arrow_up) else painterResource(
//                        R.drawable.keyboard_arrow_down
//                    ),
//                    contentDescription = if (expanded) "Collapse" else "Expand",
//                    tint = MaterialTheme.colorScheme.onSurfaceVariant
//                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        details,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}