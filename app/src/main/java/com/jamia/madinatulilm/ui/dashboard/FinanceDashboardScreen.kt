package com.jamia.madinatulilm.ui.dashboard

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jamia.madinatulilm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceDashboardScreen(
    navController: NavController,
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit
) {
    val balance by viewModel.monthlyBalance.collectAsState()
    var showResetConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri ->
        uri?.let { viewModel.exportReport(context, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Management", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { exportLauncher.launch("Finance_Report_${System.currentTimeMillis()}.xlsx") }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export")
                    }
                    IconButton(onClick = { showResetConfirm = true }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset", tint = StatusDisapproved)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hero Card: Monthly Balance
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = ForestGreen),
                    elevation = CardDefaults.cardElevation(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(150.dp).align(Alignment.BottomEnd).offset(x = 30.dp, y = 30.dp),
                            tint = SoftCream.copy(alpha = 0.05f)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "CURRENT MONTHLY BALANCE",
                                color = LuxuryGold,
                                style = MaterialTheme.typography.labelLarge,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "₨ ${String.format("%,.2f", balance)}",
                                color = SoftCream,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "کل ماہانہ میزانیہ",
                                color = LuxuryGold.copy(alpha = 0.8f),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "Select Department",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen,
                    modifier = Modifier.align(Alignment.Start).padding(start = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().height(320.dp)
                ) {
                    item { DepartmentCard("Canteen", "کینٹین", Icons.Default.Restaurant, { navController.navigate("finance_canteen") }) }
                    item { DepartmentCard("Bookshop", "مکتبہ", Icons.Default.MenuBook, { navController.navigate("finance_maktab") }) }
                    item { DepartmentCard("Donations", "عطیات", Icons.Default.VolunteerActivism, { navController.navigate("finance_donations") }) }
                    item { DepartmentCard("Payroll", "تنخواہ", Icons.Default.Payments, { navController.navigate("finance_payroll") }) }
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Monthly Balance?") },
            text = { Text("This will permanently clear all revenue, donation, and payroll records for the current month. Are you sure?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetMonthlyBalance()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDisapproved)
                ) { Text("Reset Now") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun DepartmentCard(
    title: String,
    urduTitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, LuxuryGold.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(ForestGreen.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.ExtraBold, color = ForestGreen, fontSize = 16.sp)
            Text(urduTitle, color = ForestGreen.copy(alpha = 0.6f), fontSize = 14.sp)
        }
    }
}
