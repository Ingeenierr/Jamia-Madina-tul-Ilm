package com.jamia.madinatulilm.ui.admin

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jamia.madinatulilm.data.User
import com.jamia.madinatulilm.data.UserStatus
import com.jamia.madinatulilm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalScreen(viewModel: ApprovalViewModel = viewModel(), onNavigateBack: () -> Unit) {
    val users by viewModel.users.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedUserIds by viewModel.selectedUserIds.collectAsState()

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                SelectionTopAppBar(
                    selectionCount = selectedUserIds.size,
                    onClearSelection = { viewModel.toggleSelectionMode() },
                    onApproveSelected = { viewModel.approveSelectedUsers() },
                    onDisapproveSelected = { viewModel.disapproveSelectedUsers() },
                    onDeleteSelected = { viewModel.deleteSelectedUsers() }
                )
            } else {
                DefaultTopAppBar(onNavigateBack = onNavigateBack)
            }
        },
        containerColor = SoftCream
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SoftCream, Color.White)))) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(users, key = { it.id }) { user ->
                    UserApprovalCard(
                        user = user,
                        isSelected = selectedUserIds.contains(user.id),
                        onItemClick = {
                            if (isSelectionMode) {
                                viewModel.toggleUserSelection(user.id)
                            }
                        },
                        onItemLongClick = {
                            if (!isSelectionMode) {
                                viewModel.toggleSelectionMode()
                            }
                            viewModel.toggleUserSelection(user.id)
                        },
                        onRevoke = { viewModel.revokeUserAccess(user) },
                        onReinstate = { viewModel.reinstateUserAccess(user) },
                        onApprove = { viewModel.approveUser(user) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultTopAppBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = { Text("User Approvals", fontWeight = FontWeight.Black) },
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopAppBar(
    selectionCount: Int,
    onClearSelection: () -> Unit,
    onApproveSelected: () -> Unit,
    onDisapproveSelected: () -> Unit,
    onDeleteSelected: () -> Unit
) {
    TopAppBar(
        title = { Text("$selectionCount Selected", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(Icons.Default.Close, contentDescription = "Clear Selection")
            }
        },
        actions = {
            IconButton(onClick = onApproveSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Approve All", tint = StatusApproved)
            }
            IconButton(onClick = onDisapproveSelected) {
                Icon(Icons.Default.Block, contentDescription = "Disapprove All", tint = StatusDisapproved)
            }
            IconButton(onClick = onDeleteSelected) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Selected")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserApprovalCard(
    user: User,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
    onRevoke: () -> Unit,
    onReinstate: () -> Unit,
    onApprove: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(if (isSelected) 0.98f else 1f, label = "cardScale")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onItemLongClick()
                }
            ),
        shape = RoundedCornerShape(32.dp),
        color = if (isSelected) ForestGreen.copy(alpha = 0.08f) else Color.White,
        shadowElevation = if (isSelected) 0.dp else 12.dp,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, ForestGreen) 
                 else androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar with Gradient Border
                Box(
                    modifier = Modifier
                        .size(60.dp)
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
                            .background(ForestGreen.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.take(1).uppercase(),
                            fontWeight = FontWeight.Black,
                            color = ForestGreen,
                            fontSize = 24.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2D3436),
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = user.role.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = ForestGreen,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                StatusBadge(status = user.status)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Professional Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (user.status) {
                    UserStatus.PENDING -> {
                        Button(
                            onClick = onApprove,
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusApproved),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.Verified, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Approve User", fontWeight = FontWeight.Black)
                        }
                    }
                    UserStatus.APPROVED -> {
                        OutlinedButton(
                            onClick = onRevoke,
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDisapproved),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, StatusDisapproved.copy(alpha = 0.4f))
                        ) {
                            Icon(Icons.Default.Block, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Revoke Access", fontWeight = FontWeight.Bold)
                        }
                    }
                    UserStatus.DISAPPROVED -> {
                        Button(
                            onClick = onReinstate,
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Reinstate", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: UserStatus) {
    val (color, text) = when (status) {
        UserStatus.APPROVED -> StatusApproved to "APPROVED"
        UserStatus.PENDING -> StatusPending to "PENDING"
        UserStatus.DISAPPROVED -> StatusDisapproved to "DISAPPROVED"
    }

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}
