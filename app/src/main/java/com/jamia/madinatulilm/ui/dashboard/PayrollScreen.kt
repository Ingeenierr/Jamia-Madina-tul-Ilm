package com.jamia.madinatulilm.ui.dashboard

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jamia.madinatulilm.data.finance.PayrollRecord
import com.jamia.madinatulilm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit
) {
    val payrollRecords by viewModel.payroll.collectAsState()
    val pendingUpdates by viewModel.pendingPayrollUpdates.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teacher Payroll", fontWeight = FontWeight.Bold) },
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(SoftCream, Color.White)))
                .padding(paddingValues)
        ) {
            if (payrollRecords.isEmpty()) {
                EmptyState("No payroll records found for this month.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp, top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(payrollRecords, key = { it.id }) { record ->
                        PayrollItem(
                            record = record,
                            isUpdating = pendingUpdates.contains(record.id),
                            onToggle = { viewModel.togglePayroll(record.teacherId, record.teacherName, record.amount) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PayrollItem(
    record: PayrollRecord,
    isUpdating: Boolean,
    onToggle: () -> Unit
) {
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ForestGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = ForestGreen)
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = ForestGreen)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(record.teacherName, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = ForestGreen)
                Text("Salary: ₨ ${String.format("%,.0f", record.amount)}", fontSize = 14.sp, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = record.isPaid,
                    onCheckedChange = { if (!isUpdating) onToggle() },
                    enabled = !isUpdating,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = StatusApproved,
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = Color.Transparent
                    )
                )
                Text(
                    text = if (record.isPaid) "PAID" else "PENDING",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (record.isPaid) StatusApproved else StatusDisapproved,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}
