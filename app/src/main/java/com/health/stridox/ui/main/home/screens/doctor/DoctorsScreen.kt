@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.health.stridox.ui.main.home.screens.doctor

import android.content.Intent
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.health.stridox.R
import com.health.stridox.data.Doctor


private val doctors = listOf(
    Doctor(
        "Dr. Sarah Johnson",
        "City General Hospital",
        "+91 11111 11111",
        true,
        Color(0xFFE91E63)
    ),
    Doctor(
        "Dr. Michael Chen",
        "St. Mary's Medical Center",
        "+91 22222 22222",
        true,
        Color(0xFF2196F3)
    ),
    Doctor(
        "Dr. Emily Parker",
        "Children's Health Clinic",
        "+91 33333 33333",
        false,
        Color(0xFF9C27B0)
    )
)

// --- Colors (tweak to fit your Theme) ---
private val AvailableGreen = Color(0xFF00C853)
private val MessageBlue = Color(0xFF2196F3)


@Composable
fun DoctorsScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.doctors_title),
                        fontWeight = FontWeight.SemiBold,
                    )
                },
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(12.dp)
        ) {
            items(doctors, key = { it.name }) { doctor ->
                DoctorCard(doctor = doctor)
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun DoctorCard(doctor: Doctor) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        // Main row is anchored below the header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            val initials = remember(doctor.name) {
                doctor.name.split(" ")
                    .filter { it.isNotBlank() }
                    .map { it.first().uppercaseChar() }
                    .take(2)
                    .joinToString("")
            }

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(doctor.avatarColor ?: Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Middle column holds connect button and phone; given fixed widths so phone won't wrap vertically.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = doctor.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = doctor.specialty,
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelMediumEmphasized,
                    maxLines = 4
                )
            }
        }
        // Right-side action icons: call & message vertically stacked and centered
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp)
        ) {
            // Connect button (center-left)
            Button(
                onClick = { /* connect action */ },
                shape = RoundedCornerShape(28.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                enabled = doctor.isAvailable,
                modifier = Modifier
                    .wrapContentHeight()
                    .weight(1f)
            ) {
                Text(
                    text = if (doctor.isAvailable) stringResource(R.string.connect_button) else stringResource(
                        R.string.unavailable_button
                    ),
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButtonCircle(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = "tel:${doctor.phone}".toUri()
                    }
                    context.startActivity(intent)
                },
                icon = painterResource(id = R.drawable.call_24px),
                contentDescription = stringResource(R.string.call_desc),
                background = AvailableGreen,
                enabled = doctor.isAvailable
            )
            val smsBodyTxt = stringResource(R.string.doctor_sms_body)
            IconButtonCircle(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "sms:${doctor.phone}".toUri()
                        putExtra("sms_body", smsBodyTxt)
                    }
                    context.startActivity(intent)
                },
                icon = painterResource(id = R.drawable.sms_24px),
                contentDescription = stringResource(R.string.message_desc),
                background = MessageBlue,
                enabled = doctor.isAvailable
            )
        }
    }
}

@Composable
private fun IconButtonCircle(
    onClick: () -> Unit,
    icon: Painter,
    contentDescription: String?,
    background: Color,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier,
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = background,
            contentColor = Color.White,
            disabledContainerColor = Color.LightGray,
            disabledContentColor = Color.Gray
        )
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}
