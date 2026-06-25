package com.jamia.madinatulilm.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jamia.madinatulilm.data.finance.DonationRecord
import com.jamia.madinatulilm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationFinanceScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit
) {
    val records by viewModel.donations.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donations & Zakat", fontWeight = FontWeight.Bold) },
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
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = ForestGreen, contentColor = Color.White) {
                Icon(Icons.Default.VolunteerActivism, contentDescription = "Add Donation")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(SoftCream, Color.White)))
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp, top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(records, key = { it.id }) { record ->
                    DonationItem(record, onDelete = { viewModel.deleteDonation(record) })
                }
            }
        }
    }

    if (showAddDialog) {
        AddDonationDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, amount, type, fixed ->
                viewModel.addDonation(name, amount, type, fixed)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun DonationItem(record: DonationRecord, onDelete: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(record.donorName, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = ForestGreen)
                Text(record.type, fontSize = 12.sp, color = if (record.isFixed) LuxuryGold else Color.Gray, fontWeight = FontWeight.Bold)
            }
            Text("₨ ${String.format("%,.0f", record.amount)}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = ForestGreen)
            
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.LightGray)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Record?") },
            text = { Text("Are you sure you want to remove this donation entry?") },
            confirmButton = {
                TextButton(onClick = { 
                    onDelete()
                    showDeleteConfirm = false
                }) { Text("Delete", color = StatusDisapproved) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AddDonationDialog(onDismiss: () -> Unit, onConfirm: (String, Double, String, Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("FAMILY") }
    var isFixed by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Donation", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Donor Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = type == "FAMILY", onClick = { type = "FAMILY" })
                    Text("Family")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = type == "COMMITTEE", onClick = { type = "COMMITTEE" })
                    Text("Committee")
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isFixed, onCheckedChange = { isFixed = it })
                    Text("Fixed Monthly Commitment")
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                onConfirm(name, amount.toDoubleOrNull() ?: 0.0, type, isFixed) 
            }, colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
