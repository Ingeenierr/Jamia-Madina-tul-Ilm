package com.jamia.madinatulilm.ui.donations

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jamia.madinatulilm.data.Student
import com.jamia.madinatulilm.utils.ExcelExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationDashboardScreen(
    viewModel: DonationDashboardViewModel,
    onNavigateBack: () -> Unit,
    students: List<Student>
) {
    val donationData by viewModel.allDonations.collectAsState()
    val totalDonations by viewModel.totalDonations.collectAsState()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { ExcelExporter.exportDonationsToExcel(context, donationData.map { it.donation }, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donation Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { exportLauncher.launch("donations.csv") }) {
                        Icon(Icons.Default.Download, contentDescription = "Export Donations")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)) {
            Text("Total Donations: Rs. ${totalDonations}", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(donationData) { item ->
                    val studentName = students.find { it.id == item.donation.studentId }?.fullName ?: "N/A"
                    DonationItem(item.donation, studentName)
                }
            }
        }
    }
}

@Composable
fun DonationItem(donation: com.jamia.madinatulilm.data.Donation, studentName: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Student: $studentName", fontWeight = FontWeight.Bold)
            Text("Amount: Rs. ${donation.amount}")
            Text("Type: ${donation.type}")
            Text("Date: ${donation.date}")
        }
    }
}
