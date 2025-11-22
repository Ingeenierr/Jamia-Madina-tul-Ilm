package com.jamia.madinatulilm.ui.teachers

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveRequestScreen(
    teacherId: String,
    onNavigateBack: () -> Unit
) {
    val viewModel: LeaveRequestViewModel = viewModel()
    val requestState by viewModel.requestState.collectAsState()
    
    var reason by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    val datePickerState = rememberDatePickerState()

    LaunchedEffect(teacherId) {
        viewModel.fetchTeacherName(teacherId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Leave") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (selectedDateMillis != null) {
                    val date = java.util.Date(selectedDateMillis!!)
                    val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    format.format(date)
                } else "Select Date")
            }

            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Reason for Leave") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = {
                    if (selectedDateMillis != null && reason.isNotBlank()) {
                        val date = java.util.Date(selectedDateMillis!!)
                        val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                        viewModel.submitLeaveRequest(teacherId, reason, format.format(date))
                    }
                },
                enabled = requestState !is RequestState.Loading && selectedDateMillis != null && reason.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (requestState is RequestState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Submit Request")
                }
            }

            if (requestState is RequestState.Success) {
                Text("Request Submitted Successfully!", color = MaterialTheme.colorScheme.primary)
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    onNavigateBack()
                }
            }
            
            if (requestState is RequestState.Error) {
                Text((requestState as RequestState.Error).message, color = MaterialTheme.colorScheme.error)
            }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            selectedDateMillis = datePickerState.selectedDateMillis
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }
    }
}
