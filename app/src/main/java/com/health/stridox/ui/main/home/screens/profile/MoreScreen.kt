@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.health.stridox.ui.main.home.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.health.stridox.R
import com.health.stridox.domain.Preferences
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun MoreScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    navToPrivacyPolicy: () -> Unit,
    navToHelp: () -> Unit,
    navToPersonalInfo: () -> Unit,
    onLoggedOut: () -> Unit,
    preferences: Preferences = koinInject()
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val coroutineScope = rememberCoroutineScope()
    // Theme settings
    val currentThemeMode by preferences.getThemeMode()
        .collectAsStateWithLifecycle(initialValue = "system")
    var showThemeSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showThemeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showThemeSheet = false },
            sheetState = sheetState
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    text = "Choose Theme",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ThemeOption(
                    text = "System Default",
                    selected = currentThemeMode == "system",
                    icon = R.drawable.contrast,
                    onClick = {
                        coroutineScope.launch {
                            preferences.saveThemeMode("system")
                            sheetState.hide()
                            showThemeSheet = false
                        }
                    }
                )
                ThemeOption(
                    text = "Light",
                    selected = currentThemeMode == "light",
                    icon = R.drawable.light_mode,
                    onClick = {
                        coroutineScope.launch {
                            preferences.saveThemeMode("light")
                            sheetState.hide()
                            showThemeSheet = false
                        }
                    }
                )
                ThemeOption(
                    text = "Dark",
                    selected = currentThemeMode == "dark",
                    icon = R.drawable.dark_mode,
                    onClick = {
                        coroutineScope.launch {
                            preferences.saveThemeMode("dark")
                            sheetState.hide()
                            showThemeSheet = false
                        }
                    }
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(
                stringResource(R.string.more),
                fontWeight = FontWeight.SemiBold
            )
        }, actions = {
        })
    }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                stringResource(R.string.profile),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )
            // Profile Header Card
            ElevatedCard(
                onClick = navToPersonalInfo,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentPhoto = ui.photoUri
                    val imageModifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)

                    if (currentPhoto != null) {
                        AsyncImage(
                            model = currentPhoto,
                            contentDescription = stringResource(R.string.profile_pic_content_description),
                            modifier = imageModifier,
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.app_logo),
                            placeholder = painterResource(R.drawable.app_logo)
                        )
                    } else {
                        Image(
                            painterResource(R.drawable.app_logo),
                            contentDescription = stringResource(R.string.profile_pic_content_description),
                            modifier = imageModifier,
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = ui.name.ifBlank { "User Name" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "View Personal Info",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        painter = painterResource(R.drawable.keyboard_arrow_right_24px),
                        contentDescription = "Go to Personal Info"
                    )
                }
            }


            Text(
                "Settings & Support",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )

            // Theme selection item
            ProfileOptionItem(
                label = "App Theme",
                icon = if (currentThemeMode == "dark") R.drawable.dark_mode else if (currentThemeMode == "light") R.drawable.light_mode else R.drawable.contrast,
                onClick = { showThemeSheet = true }
            )

            ProfileOptionItem(
                label = "Privacy Policy",
                icon = R.drawable.policy,
                onClick = navToPrivacyPolicy
            )

            ProfileOptionItem(
                label = "Help",
                icon = R.drawable.help,
                onClick = navToHelp
            )

            Button(
                onClick = { viewModel.logout { onLoggedOut() } },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    stringResource(R.string.logout_button),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}


@Composable
fun ThemeOption(text: String, selected: Boolean, icon: Int, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // handled by Row clickable
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(painterResource(icon), text)
    }
}

@Composable
fun ProfileOptionItem(
    label: String,
    icon: Int,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                painter = painterResource(R.drawable.keyboard_arrow_right_24px),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}