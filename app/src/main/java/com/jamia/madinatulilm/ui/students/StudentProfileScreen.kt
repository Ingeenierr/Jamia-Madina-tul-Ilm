package com.jamia.madinatulilm.ui.students

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jamia.madinatulilm.ui.theme.*
import com.jamia.madinatulilm.ui.dashboard.FinanceViewModel
import com.jamia.madinatulilm.data.finance.DonationRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    studentId: String,
    financeViewModel: FinanceViewModel,
    onNavigateBack: () -> Unit
) {
    val viewModel: StudentProfileViewModel = viewModel()
    val student by viewModel.student.collectAsState()
    val donations by financeViewModel.donations.collectAsState()
    
    val studentDonations = remember(donations, studentId) {
        donations.filter { it.studentId == studentId }
    }

    var showContact by remember { mutableStateOf(false) }

    LaunchedEffect(studentId) {
        viewModel.setStudentId(studentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = ForestGreen,
                    navigationIconContentColor = ForestGreen
                )
            )
        }
    ) { paddingValues ->
        student?.let { s ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .background(SoftCream)
            ) {
                // Profile Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(Color.White, SoftCream)
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            color = ForestGreen.copy(alpha = 0.1f)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxSize(),
                                tint = ForestGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = s.fullName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                        Text(
                            text = "Enrollment ID: ${s.id.takeLast(8).uppercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = LuxuryGold,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Personal Details Card
                    ProfileSection(title = "Personal Details") {
                        ProfileDetailItem(icon = Icons.Default.Cake, label = "Age", value = "${s.age} years")
                        ProfileDetailItem(icon = Icons.Default.CalendarToday, label = "Date of Birth", value = s.dob)
                        ProfileDetailItem(icon = Icons.Default.Badge, label = "National ID / B-Form", value = s.cnic)
                    }

                    // Guardian & Contact Card
                    ProfileSection(title = "Guardian Information") {
                        ProfileDetailItem(icon = Icons.Default.Shield, label = "Guardian Name", value = s.guardianName)
                        
                        // Masked Contact Number
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Guardian Contact", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(
                                    text = if (showContact) s.contactNumber else s.contactNumber.replace(Regex("\\d"), "*").take(s.contactNumber.length - 3) + s.contactNumber.takeLast(3),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            IconButton(onClick = { showContact = !showContact }) {
                                Icon(
                                    imageVector = if (showContact) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Reveal Contact",
                                    tint = ForestGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        ProfileDetailItem(icon = Icons.Default.Home, label = "Residential Address", value = s.address)
                    }

                    // Financial Contribution Section
                    ProfileSection(title = "Financial History") {
                        if (studentDonations.isEmpty()) {
                            Text("No donation records found.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        } else {
                            studentDonations.forEach { donation ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(donation.monthYear, style = MaterialTheme.typography.bodyMedium)
                                    Text("₨ ${donation.amount}", fontWeight = FontWeight.Bold, color = ForestGreen)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            content()
        }
    }
}

@Composable
fun ProfileDetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ForestGreen,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}
