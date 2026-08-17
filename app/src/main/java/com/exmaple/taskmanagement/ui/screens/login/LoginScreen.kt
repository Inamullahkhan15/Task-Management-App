package com.exmaple.taskmanagement.ui.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exmaple.taskmanagement.viewmodel.AuthViewModel
import com.exmaple.taskmanagement.viewmodel.LoginUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: (role: String, uid: String) -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            val successState = uiState as LoginUiState.Success
            onLoginSuccess(successState.role, successState.uid)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(modifier = Modifier.size(64.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primary) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Work, contentDescription = "Task Management Logo", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Task Management", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Sign in to manage your tasks", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(28.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Email Address", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(bottom = 6.dp))
                        OutlinedTextField(
                            value = email, onValueChange = { email = it },
                            placeholder = { Text("name@company.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Password", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(bottom = 6.dp))
                        OutlinedTextField(
                            value = password, onValueChange = { password = it },
                            placeholder = { Text("••••••••") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    AnimatedVisibility(visible = uiState is LoginUiState.Error || uiState is LoginUiState.UnverifiedEmail) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                val message = when (val state = uiState) {
                                    is LoginUiState.Error -> state.message
                                    is LoginUiState.UnverifiedEmail -> "Your email is not verified yet. Please check your inbox for the verification link."
                                    else -> ""
                                }
                                Text(text = message, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)

                                if (uiState is LoginUiState.UnverifiedEmail) {
                                    val state = uiState as LoginUiState.UnverifiedEmail
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Resend verification email",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable { authViewModel.resendVerification(state.email, state.password) }
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { authViewModel.login(email, password) },
                        enabled = uiState !is LoginUiState.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState is LoginUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.5.dp)
                        } else {
                            Text("Sign In", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Don't have an account? Sign up",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onNavigateToSignUp() }
                    )
                }
            }
        }
    }
}