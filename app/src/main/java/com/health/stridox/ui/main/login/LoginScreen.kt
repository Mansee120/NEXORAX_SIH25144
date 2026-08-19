package com.health.stridox.ui.main.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.health.stridox.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()

    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.login_title), style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = ui.username,
            onValueChange = viewModel::onUsernameChange,
            label = { Text(stringResource(R.string.username_label)) },
            isError = ui.usernameError != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
        )
        ui.usernameError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = ui.password,
            onValueChange = viewModel::onPasswordChange,
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
            }
        )
        ui.passwordError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.onLogin { onLoginSuccess() } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.login_button))
        }

        ui.message?.let { Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp)) }

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = onRegisterClick,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.register_text))
        }
    }
}
