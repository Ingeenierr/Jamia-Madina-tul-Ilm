package com.jamia.madinatulilm.ui.donations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jamia.madinatulilm.ui.dashboard.FinanceViewModel
import com.jamia.madinatulilm.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(
    financeViewModel: FinanceViewModel, 
    targetId: String, 
    onNavigateBack: () -> Unit
) {
    val allDonations by financeViewModel.donations.collectAsState()
    val filteredDonations = remember(allDonations, targetId) {
        allDonations.filter { it.studentId == targetId || it.teacherId == targetId }
    }
    
    var showAddDialog by remember { mutableStateOf(false) }
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Records", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ForestGreen,
                    titleContentColor = SoftCream,
                    navigationIconContentColor = SoftCream
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ForestGreen,
                contentColor = LuxuryGold
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Record")
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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredDonations) { donation ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(donation.type, fontWeight = FontWeight.Bold, color = ForestGreen)
                                Text(sdf.format(Date(donation.timestamp)), fontSize = 12.sp, color = Color.Gray)
                            }
                            Text("₨ ${String.format("%,.0f", donation.amount)}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = StatusApproved)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddDonationDialog(
            onDismiss = { showAddDialog = false },
            onAddDonation = { amount, type, name ->
                financeViewModel.addDonation(
                    name = name,
                    amount = amount,
                    type = type,
                    studentId = targetId,
                    teacherId = targetId
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddDonationDialog(onDismiss: () -> Unit, onAddDonation: (Double, String, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Financial Entry", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Entity Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type (e.g. Monthly Fee)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onAddDonation(amount.toDoubleOrNull() ?: 0.0, type, name) }) {
                Text("Add Entry")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
