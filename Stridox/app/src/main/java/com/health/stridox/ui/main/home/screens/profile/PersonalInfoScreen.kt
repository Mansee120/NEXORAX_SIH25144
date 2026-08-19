@file:OptIn(ExperimentalMaterial3Api::class)

package com.health.stridox.ui.main.home.screens.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.health.stridox.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun PersonalInfoScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    navBack: () -> Unit
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val path = saveImageToInternalStorage(context, uri)
                if (path != null) {
                    viewModel.onImagePicked(path)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Information") },
                navigationIcon = {
                    IconButton(onClick = navBack) {
                        Icon(painterResource(R.drawable.arrow_back), contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (ui.isEditing) viewModel.cancelEditing() else viewModel.startEditing()
                        }
                    ) {
                        Text(
                            text = if (ui.isEditing) "Cancel" else "Edit",
                            color = if (ui.isEditing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Profile Picture
            Box(contentAlignment = Alignment.Center) {
                val currentPhoto = if (ui.isEditing) ui.tempPhotoUri else ui.photoUri
                val imageModifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .then(if (ui.isEditing) Modifier.clickable { launcher.launch("image/*") } else Modifier)

                if (currentPhoto != null) {
                    AsyncImage(
                        model = currentPhoto,
                        contentDescription = null,
                        modifier = imageModifier,
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.app_logo),
                        placeholder = painterResource(R.drawable.app_logo)
                    )
                } else {
                    Image(
                        painterResource(R.drawable.app_logo),
                        contentDescription = null,
                        modifier = imageModifier,
                        contentScale = ContentScale.Fit
                    )
                }

                if (ui.isEditing) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.3f))
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.photo_camera),
                            contentDescription = "Change photo",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (ui.isEditing) {
                Text(
                    text = "Update your personal details",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Name
                ProfileEditTextField(
                    value = ui.tempName,
                    onChange = viewModel::onTempName,
                    label = "Name",
                    icon = R.drawable.person,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Email
                ProfileEditTextField(
                    value = ui.tempEmail,
                    onChange = viewModel::onTempEmail,
                    label = "Email",
                    icon = R.drawable.mail,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Address
                ProfileEditTextField(
                    value = ui.tempAddress,
                    onChange = viewModel::onTempAddress,
                    label = "Address",
                    icon = R.drawable.home,
                    singleLine = false,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Emergency
                ProfileEditTextField(
                    value = ui.tempEmergency,
                    onChange = viewModel::onTempEmergency,
                    label = "Emergency Contact",
                    icon = R.drawable.emergency,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Gender
                ProfileEditTextField(
                    value = ui.tempGender,
                    onChange = viewModel::onTempGender,
                    label = "Gender",
                    icon = R.drawable.person,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Birthday
                ProfileEditTextField(
                    value = ui.tempBirthday,
                    onChange = viewModel::onTempBirthday,
                    label = "Birthday",
                    icon = R.drawable.calendar_month,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Height
                ProfileEditTextField(
                    value = ui.tempHeight,
                    onChange = viewModel::onTempHeight,
                    label = "Height (cm)",
                    icon = R.drawable.outline_accessibility_new_24,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Weight
                ProfileEditTextField(
                    value = ui.tempWeight,
                    onChange = viewModel::onTempWeight,
                    label = "Weight (kg)",
                    icon = R.drawable.weight_24px,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Step Length
                ProfileEditTextField(
                    value = ui.tempStepLength,
                    onChange = viewModel::onTempStepLength,
                    label = "Step Length (cm)",
                    icon = R.drawable.steps_24px,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )

                ProfileEditTextField(
                    value = ui.armpitToWrist,
                    onChange = viewModel::onTempArmpitToWrist,
                    label = "Armpit to Wrist (cm)",
                    icon = R.drawable.steps_24px,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )
                Spacer(Modifier.width(8.dp))
                ProfileEditTextField(
                    value = ui.wristToFoot,
                    onChange = viewModel::onTempWristToFoot,
                    label = "Wrist to Foot (cm)",
                    icon = R.drawable.steps_24px,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )


                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        viewModel.saveProfile()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
            } else {

                ProfileCardItem(
                    icon = R.drawable.person,
                    label = "Name",
                    value = ui.name
                )

                ProfileCardItem(
                    icon = R.drawable.mail,
                    label = "Email",
                    value = ui.email
                )

                ProfileCardItem(
                    icon = R.drawable.home,
                    label = "Address",
                    value = ui.address.ifBlank { "Not set" }
                )

                ProfileCardItem(
                    icon = R.drawable.emergency,
                    label = "Emergency Contact",
                    value = ui.emergency.ifBlank { "Not set" }
                )

                ProfileCardItem(
                    icon = R.drawable.person,
                    label = "Gender",
                    value = ui.gender.ifBlank { "Not set" }
                )

                ProfileCardItem(
                    icon = R.drawable.calendar_month,
                    label = "Birthday",
                    value = ui.birthday.ifBlank { "Not set" }
                )

                ProfileCardItem(
                    icon = R.drawable.outline_accessibility_new_24,
                    label = "Height",
                    value = if (ui.height.isNotBlank()) "${ui.height} cm" else "Not set"
                )

                ProfileCardItem(
                    icon = R.drawable.weight_24px,
                    label = "Weight",
                    value = if (ui.weight.isNotBlank()) "${ui.weight} kg" else "Not set"
                )

                ProfileCardItem(
                    icon = R.drawable.steps_24px,
                    label = "Step Length",
                    value = if (ui.stepLength.isNotBlank()) "${ui.stepLength} cm" else "Not set"
                )


                ProfileCardItem(
                    icon = R.drawable.steps_24px,
                    label = "Armpit to Wrist (cm)",
                    value = if (ui.armpitToWrist.isNotBlank()) "${ui.armpitToWrist} cm" else "Not set"
                )

                ProfileCardItem(
                    icon = R.drawable.steps_24px,
                    label = "Wrist to Foot (cm)",
                    value = if (ui.wristToFoot.isNotBlank()) "${ui.wristToFoot} cm" else "Not set"
                )
            }
        }
    }
}

private fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = "profile_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun ProfileCardItem(
    icon: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Icon circle
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

            // Text Column (Label + Value)
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )

                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ProfileEditTextField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    icon: Int,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                painter = painterResource(icon),
                contentDescription = null
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        maxLines = maxLines
    )
}
