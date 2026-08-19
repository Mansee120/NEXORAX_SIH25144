package com.health.stridox.ui.main.register

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.health.stridox.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = koinViewModel(), onRegistered: () -> Unit, onBack: () -> Unit
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
                    viewModel.onProfileUri(path)
                }
            }
        }
    }

    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        Modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp), verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.register_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(24.dp))

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val imageModifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .clickable { launcher.launch("image/*") }

            if (ui.profileUri != null) {
                AsyncImage(
                    model = ui.profileUri,
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

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.photo_camera),
                    contentDescription = "Add photo",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = ui.name,
            onValueChange = viewModel::onName,
            label = { Text(stringResource(R.string.full_name_label)) },
            isError = ui.nameError != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        ui.nameError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = ui.email,
            onValueChange = viewModel::onEmail,
            label = { Text(stringResource(R.string.email_label)) },
            isError = ui.emailError != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        ui.emailError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = ui.password,
            onValueChange = viewModel::onPassword,
            label = { Text(stringResource(R.string.password_label)) },
            isError = ui.passwordError != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    val icon =
                        if (passwordVisible) R.drawable.visibility else R.drawable.visibilityoff
                    Icon(
                        painterResource(icon),
                        contentDescription = if (passwordVisible) stringResource(R.string.hide_password_desc) else stringResource(
                            R.string.show_password_desc
                        )
                    )
                }
            })
        ui.passwordError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = ui.emergencyNumber,
            onValueChange = viewModel::onEmergency,
            label = { Text(stringResource(R.string.emergency_number_label)) },
            isError = ui.emergencyError != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        ui.emergencyError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = ui.address,
            onValueChange = viewModel::onAddress,
            label = { Text(stringResource(R.string.address_label)) },
            isError = ui.addressError != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 3
        )
        ui.addressError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

        Spacer(Modifier.height(8.dp))

        // New fields
        Row(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = ui.gender,
                onValueChange = viewModel::onGender,
                label = { Text("Gender") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = ui.birthday,
                onValueChange = viewModel::onBirthday,
                label = { Text("Birthday") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = ui.height,
                onValueChange = viewModel::onHeight,
                label = { Text("Height (cm)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = ui.weight,
                onValueChange = viewModel::onWeight,
                label = { Text("Weight (kg)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = ui.stepLength,
            onValueChange = viewModel::onStepLength,
            label = { Text("Step Length (cm)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = ui.armpitToWrist,
                onValueChange = viewModel::onArmpitToWrist,
                label = { Text("Armpit to Wrist (cm)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = ui.wristToFoot,
                onValueChange = viewModel::onWristToFoot,
                label = { Text("Wrist to Foot (cm)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }


        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.onRegister { onRegistered() } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.register_button))
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.back_to_login_text))
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
