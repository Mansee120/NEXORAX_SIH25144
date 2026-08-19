package com.health.stridox.ui.main.home.screens.activity

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.stridox.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    navToBreathingExercises: () -> Unit,
    navToWalkingExercises: () -> Unit,
    navToPostureWithImageExercises: () -> Unit,
    navToIndoorTraining: () -> Unit,
    navToOutdoorTraining: () -> Unit,
    navBack: () -> Unit
) {
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
                        stringResource(R.string.Games_title),
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


            ChallengeItem(
                label = stringResource(R.string.challenge_breathing),
                icon = R.drawable.self_improvement,
                onClick = navToBreathingExercises
            )
            ChallengeItem(
                label = stringResource(R.string.challenge_walking),
                icon = R.drawable.directions_walk,
                onClick = navToWalkingExercises
            )
            ChallengeItem(
                label = stringResource(R.string.posture_analysis_with_Photo),
                icon = R.drawable.accessibility,
                onClick = navToPostureWithImageExercises
            )
            ChallengeItem(
                label = stringResource(R.string.challenge_indoor),
                icon = R.drawable.fitness_center,
                onClick = navToIndoorTraining
            )
            ChallengeItem(
                label = stringResource(R.string.challenge_outdoor),
                icon = R.drawable.directions_bike,
                onClick = navToOutdoorTraining
            )


            Spacer(modifier = Modifier.height(32.dp)) // Final padding
        }
    }
}

@Composable
fun GameCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(90.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary // Use primary for highlights
            )
        }
    }
}

@Composable
fun ChallengeItem(label: String, icon: Int, onClick: () -> Unit = {}) {
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
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Icon(painterResource(R.drawable.keyboard_arrow_right_24px), contentDescription = null)
        }
    }
}

@Composable
fun BadgeItem(icon: Int, name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .align(Alignment.CenterHorizontally)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            textAlign = TextAlign.Center
        )
    }
}