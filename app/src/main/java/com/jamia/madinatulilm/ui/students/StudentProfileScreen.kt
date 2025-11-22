package com.jamia.madinatulilm.ui.students

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(studentId: String, onNavigateBack: () -> Unit) {
    val viewModel: StudentProfileViewModel = viewModel()
    val student by viewModel.student.collectAsState()

    LaunchedEffect(studentId) {
        viewModel.setStudentId(studentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(student?.fullName ?: "Student Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        student?.let { 
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Personal Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileDetailItem(icon = Icons.Default.Person, label = "Full Name", value = it.fullName)
                        ProfileDetailItem(icon = Icons.Default.Cake, label = "Age", value = it.age.toString())
                        ProfileDetailItem(icon = Icons.Default.CalendarToday, label = "Date of Birth", value = it.dob)
                        ProfileDetailItem(icon = Icons.Default.Badge, label = "CNIC", value = it.cnic)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Contact Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileDetailItem(icon = Icons.Default.Shield, label = "Guardian Name", value = it.guardianName)
                        ProfileDetailItem(icon = Icons.Default.Phone, label = "Guardian Contact", value = it.contactNumber)
                        ProfileDetailItem(icon = Icons.Default.Home, label = "Address", value = it.address)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileDetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon, 
            contentDescription = label, 
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
