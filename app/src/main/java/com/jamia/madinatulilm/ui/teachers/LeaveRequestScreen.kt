package com.jamia.madinatulilm.ui.teachers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jamia.madinatulilm.ui.theme.PrimaryGreen
import com.jamia.madinatulilm.ui.theme.StatusApproved

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
                title = { Text("Request Leave", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = PrimaryGreen,
                    navigationIconContentColor = PrimaryGreen
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Leave Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )

                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (selectedDateMillis != null) {
                                val date = java.util.Date(selectedDateMillis!!)
                                val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                                format.format(date)
                            } else "Select Leave Date",
                            fontWeight = FontWeight.Medium
                        )
                    }

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason for Leave") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Button(
                onClick = {
                    if (selectedDateMillis != null && reason.isNotBlank()) {
                        val date = java.util.Date(selectedDateMillis!!)
                        val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                        viewModel.submitLeaveRequest(teacherId, reason, format.format(date))
                    }
                },
                enabled = requestState !is RequestState.Loading && selectedDateMillis != null && reason.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (requestState is RequestState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text("Submit Request", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            AnimatedVisibility(visible = requestState is RequestState.Success) {
                Surface(
                    color = StatusApproved.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusApproved)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Request Submitted Successfully!", color = StatusApproved, fontWeight = FontWeight.Bold)
                    }
                }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    onNavigateBack()
                }
            }
            
            if (requestState is RequestState.Error) {
                Text(
                    text = (requestState as RequestState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            selectedDateMillis = datePickerState.selectedDateMillis
                            showDatePicker = false
                        }) { Text("OK", fontWeight = FontWeight.Bold) }
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
