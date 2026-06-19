package com.jamia.madinatulilm.ui.admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(users) { user ->
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultTopAppBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = { Text("User Management", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = PrimaryGreen
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onItemLongClick()
                }
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                StatusBadge(status = user.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (user.status) {
                    UserStatus.PENDING -> {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onApprove()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusApproved),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Approve", fontSize = 14.sp)
                        }
                    }
                    UserStatus.APPROVED -> {
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onRevoke()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDisapproved),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusDisapproved)
                        ) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Revoke", fontSize = 14.sp)
                        }
                    }
                    UserStatus.DISAPPROVED -> {
                        FilledTonalButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onReinstate()
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = StatusApproved.copy(alpha = 0.1f), contentColor = StatusApproved),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Reinstate", fontSize = 14.sp)
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
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
