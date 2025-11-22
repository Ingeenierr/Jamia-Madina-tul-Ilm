package com.jamia.madinatulilm.ui.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SignUpScreen(viewModel: LoginViewModel, onSignUpSuccess: () -> Unit, onNavigateToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onSignUpSuccess()
            viewModel.resetLoginState()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create Account", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, enabled = loginState !is LoginState.Loading)
        Spacer(modifier = Modifier.height(8.dp))
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
            onClick = { viewModel.signUp(name, email, password) },
            enabled = loginState !is LoginState.Loading
        ) {
            AnimatedContent(targetState = loginState is LoginState.Loading, label = "SignUpButton") {
                if (it) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Sign Up")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onNavigateToLogin, enabled = loginState !is LoginState.Loading) {
            Text("Already have an account? Login")
        }
        if (loginState is LoginState.Error) {
            Text((loginState as LoginState.Error).message, color = MaterialTheme.colorScheme.error)
        }
    }
}
