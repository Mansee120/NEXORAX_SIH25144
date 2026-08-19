@file:OptIn(ExperimentalMaterial3Api::class)

package com.health.stridox.ui.main.home.screens.activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.health.stridox.R
import com.health.stridox.data.SensorData
import com.health.stridox.data.SensorValues
import com.health.stridox.domain.Preferences
import com.health.stridox.ui.main.LocalSensorData
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navBack: () -> Unit,
    navToGames: () -> Unit,
    preferences: Preferences = koinInject()
) {
    val coroutineScope = rememberCoroutineScope()
    var userName by remember { mutableStateOf("User") }
    var rewardPoints by remember { mutableIntStateOf(0) }
    val goalPoints = 1000
    val goalProgress by remember {
        derivedStateOf {
            rewardPoints.toFloat() / goalPoints.toFloat()
        }
    }

    // Fetch data from preferences
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            userName = preferences.getLoginResponse()?.name ?: "Alex"
            rewardPoints = preferences.getPoints()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = navBack) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = stringResource(R.string.back_content_description)
                        )
                    }
                },
                title = {
                    Text(
                        stringResource(R.string.dashboard_title),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {

            // Reward/Streak Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GameCard(
                    title = stringResource(R.string.reward_points_title),
                    value = rewardPoints.toString(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                GameCard(
                    title = stringResource(R.string.current_streak_title),
                    value = "14 \uD83D\uDD25", // Using fire emoji as per screenshot
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Games Card
            Card(
                onClick = navToGames,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Play Games",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Have fun and earn rewards!",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(
                        painter = painterResource(R.drawable.sports_esports),
                        contentDescription = "Games",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Today's Goal
            Text(
                stringResource(R.string.todays_goal_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { goalProgress }, // Placeholder 75%
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    stringResource(R.string.percentage_format, 75),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Your Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.your_badges_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
//                TextButton(onClick = { /* Handle View All */ }) {
//                    Text("View All", color = MaterialTheme.colorScheme.primary)
//                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            val badges = listOf(
                stringResource(R.string.badge_first_step) to R.drawable.south_america,
                stringResource(R.string.badge_1k_walker) to R.drawable.directions_walk,
                stringResource(R.string.badge_30_mins) to R.drawable.timer,
                stringResource(R.string.badge_7_day_streak) to R.drawable.calendar_month,
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(badges) { (name, icon) ->
                    BadgeItem(icon = icon, name = name)
                }
            }

            Spacer(modifier = Modifier.height(32.dp)) // Final padding
        }
    }
}

@Composable
fun SettingsItem(label: String, icon: Int, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Icon(painterResource(R.drawable.keyboard_arrow_right_24px), contentDescription = null)
        }
    }
}
