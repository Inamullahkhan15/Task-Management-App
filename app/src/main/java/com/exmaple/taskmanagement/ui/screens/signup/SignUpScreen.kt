package com.exmaple.taskmanagement.ui.screens.signup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
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
import com.exmaple.taskmanagement.viewmodel.SignUpUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel,
    onSignUpSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val signUpState by authViewModel.signUpState.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(signUpState) {
        if (signUpState is SignUpUiState.Success) {
            onSignUpSuccess()
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
                            Icon(Icons.Default.Work, contentDescription = "Logo", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Create account", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Sign up to get started", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(28.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Full Name", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(bottom = 6.dp))
                        OutlinedTextField(
                            value = name, onValueChange = { name = it },
                            placeholder = { Text("Ali Khan") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Email Address", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(bottom = 6.dp))
                        OutlinedTextField(
                            value = email, onValueChange = { email = it },
                            placeholder = { Text("name@company.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
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
                            placeholder = { Text("At least 6 characters") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Confirm Password", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(bottom = 6.dp))
                        OutlinedTextField(
                            value = confirmPassword, onValueChange = { confirmPassword = it },
                            placeholder = { Text("Re-enter password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    AnimatedVisibility(visible = signUpState is SignUpUiState.Error) {
                        if (signUpState is SignUpUiState.Error) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                            ) {
                                Text(
                                    text = (signUpState as SignUpUiState.Error).message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { authViewModel.signUp(name, email, password, confirmPassword) },
                        enabled = signUpState !is SignUpUiState.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp)
                    ) {
                        if (signUpState is SignUpUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.5.dp)
                        } else {
                            Text("Create Account", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Already have an account? Sign in",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onBackToLogin() }
                    )
                }
            }
        }
    }
}