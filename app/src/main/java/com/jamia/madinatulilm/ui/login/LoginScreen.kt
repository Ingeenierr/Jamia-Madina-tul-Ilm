package com.jamia.madinatulilm.ui.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.jamia.madinatulilm.data.UserRole

@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginSuccess: (String, UserRole) -> Unit, onNavigateToSignUp: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            val successState = loginState as LoginState.Success
            onLoginSuccess(successState.uid, successState.role)
            viewModel.resetLoginState()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, enabled = loginState !is LoginState.Loading)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            enabled = loginState !is LoginState.Loading
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.login(email, password) },
            enabled = loginState !is LoginState.Loading
        ) {
            AnimatedContent(targetState = loginState is LoginState.Loading, label = "LoginButton") {
                if (it) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Login")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onNavigateToSignUp, enabled = loginState !is LoginState.Loading) {
            Text("Don't have an account? Sign Up")
        }
        if (loginState is LoginState.Error) {
            Text((loginState as LoginState.Error).message, color = MaterialTheme.colorScheme.error)
        }
    }
}
