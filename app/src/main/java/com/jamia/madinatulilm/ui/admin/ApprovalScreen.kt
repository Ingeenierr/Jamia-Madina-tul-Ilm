package com.jamia.madinatulilm.ui.admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jamia.madinatulilm.data.User
import com.jamia.madinatulilm.data.UserStatus

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
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    onApprove = { viewModel.approveUser(user) } // Correctly call the single-user approve function
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultTopAppBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = { Text("Manage Users") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
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
        title = { Text("$selectionCount selected") },
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(Icons.Default.Close, contentDescription = "Clear Selection")
            }
        },
        actions = {
            IconButton(onClick = onApproveSelected) {
                Icon(Icons.Default.Check, contentDescription = "Approve")
            }
            IconButton(onClick = onDisapproveSelected) {
                Icon(Icons.Default.Block, contentDescription = "Disapprove")
            }
            IconButton(onClick = onDeleteSelected) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = onItemLongClick
            ),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                when (user.status) {
                    UserStatus.PENDING -> {
                        Button(onClick = onApprove, modifier = Modifier.fillMaxWidth()) { 
                            Text("Approve") 
                        }
                    }
                    UserStatus.APPROVED -> {
                        Text("Approved", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Button(onClick = onRevoke, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { 
                            Text("Revoke")
                        }
                    }
                    UserStatus.DISAPPROVED -> {
                        Text("Disapproved", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Button(onClick = onReinstate) { 
                            Text("Reinstate")
                        }
                    }
                }
            }
        }
    }
}
