package com.jamia.madinatulilm.ui.admin

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jamia.madinatulilm.data.LeaveRequest
import com.jamia.madinatulilm.data.LeaveStatus
import com.jamia.madinatulilm.ui.theme.*

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
                title = { Text("Leave Requests", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = ForestGreen
                )
            )
        },
        containerColor = SoftCream
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SoftCream, Color.White)))) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (pendingRequests.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Pending Approval", color = ForestGreen)
                    }
                    items(pendingRequests, key = { it.id }) { request ->
                        AdminLeaveRequestCard(request, viewModel)
                    }
                }

                if (historyRequests.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(title = "Recently Processed", color = Color.Gray)
                    }
                    items(historyRequests, key = { it.id }) { request ->
                        AdminLeaveRequestCard(request, viewModel)
                    }
                }

                if (leaveRequests.isEmpty()) {
                    item {
                        EmptyLeavesState()
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(width = 4.dp, height = 18.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black,
            color = color,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun AdminLeaveRequestCard(request: LeaveRequest, viewModel: LeaveRequestsAdminViewModel) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(32.dp),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 12.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar Container with Glow
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Brush.linearGradient(listOf(ForestGreen, ForestGreenLight)),
                            CircleShape
                        )
                        .padding(2.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .background(ForestGreen.copy(alpha = 0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person, 
                            null, 
                            tint = ForestGreen, 
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.teacherName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2D3436),
                        letterSpacing = (-0.5).sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(ForestGreen.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Event, null, tint = ForestGreen, modifier = Modifier.size(10.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = request.date,
                            style = MaterialTheme.typography.labelMedium,
                            color = ForestGreen,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                
                if (request.status != LeaveStatus.PENDING) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusChip(status = request.status)
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.deleteRequest(request.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline, 
                                contentDescription = "Delete", 
                                tint = Color.Gray.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Glassmorphism-style Reason Container
            Surface(
                color = BackgroundLight.copy(alpha = 0.4f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.08f))
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "OFFICIAL REASON",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = request.reason,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            if (request.status == LeaveStatus.PENDING) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.rejectRequest(request.id) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDisapproved),
                        border = androidx.compose.foundation.BorderStroke(2.dp, StatusDisapproved.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Decline", fontWeight = FontWeight.Black)
                    }
                    
                    Button(
                        onClick = { viewModel.approveRequest(request.id) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusApproved),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Approve", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyLeavesState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.EventAvailable, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
        Spacer(Modifier.height(16.dp))
        Text("All caught up!", fontWeight = FontWeight.Bold, color = Color.Gray)
        Text("No pending leave requests found.", fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun StatusChip(status: LeaveStatus) {
    val color = when (status) {
        LeaveStatus.APPROVED -> StatusApproved
        LeaveStatus.REJECTED -> StatusDisapproved
        else -> Color.Gray
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = color,
            fontSize = 9.sp
        )
    }
}
