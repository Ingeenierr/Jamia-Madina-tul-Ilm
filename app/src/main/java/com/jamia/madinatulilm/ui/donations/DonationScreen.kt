package com.jamia.madinatulilm.ui.donations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jamia.madinatulilm.data.Donation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(viewModel: DonationViewModel, studentId: String, onNavigateBack: () -> Unit) {
    val donations by viewModel.getDonationsForStudent(studentId).collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donations") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Donation")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(donations) { donation ->
                DonationItem(donation)
            }
        }
    }

    if (showAddDialog) {
        AddDonationDialog(
            onDismiss = { showAddDialog = false },
            onAddDonation = { amount, type ->
                viewModel.addDonation(Donation(studentId = studentId, amount = amount, type = type, date = System.currentTimeMillis().toString()))
                showAddDialog = false
            }
        )
    }
}

@Composable
fun DonationItem(donation: Donation) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(donation.type, fontWeight = FontWeight.Bold)
            Text("Rs. ${donation.amount}")
            Text(donation.date)
        }
    }
}

@Composable
fun AddDonationDialog(onDismiss: () -> Unit, onAddDonation: (Double, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Donation") },
        text = {
            Column {
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") })
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type (e.g., Monthly Fee)") })
            }
        },
        confirmButton = {
            Button(onClick = { onAddDonation(amount.toDoubleOrNull() ?: 0.0, type) }) {
                Text("Add")
            }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}
