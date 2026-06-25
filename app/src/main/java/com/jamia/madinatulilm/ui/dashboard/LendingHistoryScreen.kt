package com.jamia.madinatulilm.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
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
import com.jamia.madinatulilm.data.finance.LendingRecord
import com.jamia.madinatulilm.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LendingHistoryScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit
) {
    val records by viewModel.lendingRecords.collectAsState()
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Issues History", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ForestGreen,
                    titleContentColor = SoftCream,
                    navigationIconContentColor = SoftCream
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
            if (records.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No lending history found.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(records, key = { it.id }) { record ->
                        LendingItem(
                            record = record, 
                            sdf = sdf,
                            onMarkReturned = { viewModel.returnBook(record) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LendingItem(record: LendingRecord, sdf: SimpleDateFormat, onMarkReturned: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ForestGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = ForestGreen, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(record.borrowerName, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.DarkGray)
                    Text("Issued Book: ${record.bookTitle}", fontSize = 12.sp, color = ForestGreen, fontWeight = FontWeight.Bold)
                }
                Surface(
                    onClick = { if (record.status != "RETURNED") onMarkReturned() },
                    color = if (record.status == "RETURNED") StatusApproved.copy(alpha = 0.1f) else StatusPending.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    enabled = record.status != "RETURNED"
                ) {
                    Text(
                        text = if (record.status == "RETURNED") "RETURNED" else "MARK RETURNED",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (record.status == "RETURNED") StatusApproved else StatusPending
                    )
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Issue Date", fontSize = 10.sp, color = Color.Gray)
                    Text(sdf.format(Date(record.borrowDate)), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Due Date", fontSize = 10.sp, color = Color.Gray)
                    Text(sdf.format(Date(record.dueDate)), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (System.currentTimeMillis() > record.dueDate && record.status != "RETURNED") Color.Red else Color.DarkGray)
                }
            }
        }
    }
}
