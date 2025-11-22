package com.jamia.madinatulilm.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jamia.madinatulilm.data.LeaveRequest
import com.jamia.madinatulilm.data.LeaveStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveRequestsAdminScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: LeaveRequestsAdminViewModel = viewModel()
    val leaveRequests by viewModel.leaveRequests.collectAsState()

    val pendingRequests = leaveRequests.filter { it.status == LeaveStatus.PENDING }
    val historyRequests = leaveRequests.filter { it.status != LeaveStatus.PENDING }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Leave Requests") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (pendingRequests.isNotEmpty()) {
                item {
                    Text("Pending Requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                items(pendingRequests) { request ->
                    LeaveRequestCard(request, viewModel)
                }
            } else {
                item {
                    Text("No pending requests.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }

            if (historyRequests.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Request History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                items(historyRequests) { request ->
                    LeaveRequestCard(request, viewModel)
                }
            }
        }
    }
}

@Composable
fun LeaveRequestCard(request: LeaveRequest, viewModel: LeaveRequestsAdminViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(request.teacherName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(request.date, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Reason: ${request.reason}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (request.status == LeaveStatus.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { viewModel.rejectRequest(request.id) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Reject")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.approveRequest(request.id) }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Approve")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve")
                    }
                }
            } else {
                Text(
                    text = "Status: ${request.status.name}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (request.status == LeaveStatus.APPROVED) Color.Green else Color.Red
                )
            }
        }
    }
}
