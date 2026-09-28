package com.example.emarketing_case.presentation.auth.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.emarketing_case.presentation.R
import com.example.emarketing_case.presentation.components.AppButton
import com.example.emarketing_case.presentation.components.AppTextField
import com.example.emarketing_case.presentation.theme.AppBrandGradient
import com.example.emarketing_case.presentation.theme.AppTextSecondary
import com.example.emarketing_case.presentation.theme.appBackground

@Composable
fun LoginScreen(
    username: String,
    password: String,
    isPasswordVisible: Boolean,
    uiState: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLoading = uiState is LoginUiState.Loading
    val isError = uiState is LoginUiState.Error
    val canSubmit = username.isNotBlank() && password.isNotBlank() && !isLoading

    Box(
        modifier = modifier
            .fillMaxSize()
            .appBackground(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 31.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 328.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
            ) {
                Spacer(modifier = Modifier.weight(1f))
                LoginHeader()
                Spacer(modifier = Modifier.size(40.dp))
                AppTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    placeholder = stringResource(R.string.login_username_label),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    isError = isError,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_login_user),
                            contentDescription = null,
                            tint = Color.Unspecified,
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                    ),
                )
                Spacer(modifier = Modifier.size(16.dp))
                AppTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    placeholder = stringResource(R.string.login_password_label),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    isError = isError,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_login_lock),
                            contentDescription = null,
                            tint = Color.Unspecified,
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = onPasswordVisibilityChange,
                            enabled = !isLoading,
                        ) {
                            if (isPasswordVisible) {
                                Icon(
                                    imageVector = Icons.Outlined.VisibilityOff,
                                    contentDescription = stringResource(R.string.login_hide_password),
                                    tint = AppTextSecondary,
                                )
                            } else {
                                Icon(
                                    painter = painterResource(R.drawable.ic_login_eye),
                                    contentDescription = stringResource(R.string.login_show_password),
                                    tint = Color.Unspecified,
                                )
                            }
                        }
                    },
                    visualTransformation = if (isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (canSubmit) onLoginClick() },
                    ),
                )

                if (uiState is LoginUiState.Error) {
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = stringResource(uiState.messageRes),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                } else {
                    Spacer(modifier = Modifier.size(28.dp))
                }

                AppButton(
                    text = stringResource(R.string.login_action),
                    onClick = onLoginClick,
                    enabled = canSubmit,
                    isLoading = isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LoginHeader() {
    Text(
        text = stringResource(R.string.login_brand),
        style = MaterialTheme.typography.displaySmall.copy(brush = AppBrandGradient),
    )
    Spacer(modifier = Modifier.size(8.dp))
    Text(
        text = stringResource(R.string.login_title),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(modifier = Modifier.size(8.dp))
    Text(
        text = stringResource(R.string.login_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
