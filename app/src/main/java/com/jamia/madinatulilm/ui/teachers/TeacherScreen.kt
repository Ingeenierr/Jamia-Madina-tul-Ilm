package com.jamia.madinatulilm.ui.teachers

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jamia.madinatulilm.data.Teacher
import com.jamia.madinatulilm.data.TeacherLog
import com.jamia.madinatulilm.ui.theme.*
import com.jamia.madinatulilm.utils.ExcelExporter
import com.jamia.madinatulilm.ui.dashboard.FinanceViewModel
import com.jamia.madinatulilm.data.finance.PayrollRecord
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TeacherScreen(
    viewModel: TeacherViewModel,
    financeViewModel: FinanceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDonations: (String) -> Unit
) {
    val teachers by viewModel.allTeachers.collectAsState()
    val payroll by financeViewModel.payroll.collectAsState()
    
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedTeacherIds by viewModel.selectedTeacherIds.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Teacher?>(null) }
    var showLogsDialog by remember { mutableStateOf<Teacher?>(null) }
    val context = LocalContext.current
    val toastMessage by viewModel.toastMessage.collectAsState(initial = null)

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onToastShown()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val importedTeachers = ExcelExporter.importTeachersFromExcel(context, it)
            if (importedTeachers.isNotEmpty()) {
                importedTeachers.forEach(viewModel::addTeacher)
                Toast.makeText(context, "Successfully imported ${importedTeachers.size} teachers.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Import failed or file is empty/invalid.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri ->
        uri?.let { ExcelExporter.exportTeachersToExcel(context, teachers, it) }
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                SelectionTopAppBar(
                    selectionCount = selectedTeacherIds.size,
                    onClearSelection = { viewModel.toggleSelectionMode() },
                    onSelectAll = { viewModel.selectAll() },
                    onDeleteSelected = { viewModel.deleteSelectedTeachers() }
                )
            } else {
                DefaultTopAppBar(
                    onNavigateBack = onNavigateBack,
                    onEnterSelectionMode = { viewModel.toggleSelectionMode() },
                    onImport = { importLauncher.launch("*/*") },
                    onExport = { exportLauncher.launch("Teachers_${System.currentTimeMillis()}.xlsx") }
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = ForestGreen,
                    contentColor = LuxuryGold,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Teacher")
                }
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
            ) {
                items(teachers, key = { it.id }) { teacher ->
                    val payrollRecord = payroll.find { it.teacherId == teacher.id }
                    EnhancedTeacherListItem(
                        teacher = teacher,
                        isPaid = payrollRecord?.isPaid ?: false,
                        isSelected = selectedTeacherIds.contains(teacher.id),
                        onItemClick = {
                            if (isSelectionMode) {
                                viewModel.toggleTeacherSelection(teacher.id)
                            } else {
                                showEditDialog = teacher
                            }
                        },
                        onItemLongClick = {
                            if (!isSelectionMode) {
                                viewModel.toggleSelectionMode()
                            }
                            viewModel.toggleTeacherSelection(teacher.id)
                        },
                        onCheckIn = { viewModel.checkIn(teacher.id) },
                        onCheckOut = { viewModel.checkOut(teacher.id) },
                        onViewLogs = { showLogsDialog = teacher },
                        onPayrollToggle = {
                            financeViewModel.togglePayroll(teacher.id, teacher.name, teacher.salary)
                        },
                        onDonationClick = {
                            onNavigateToDonations(teacher.id)
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddTeacherDialog(
            onDismiss = { showAddDialog = false },
            onAddTeacher = {
                viewModel.addTeacher(it)
                showAddDialog = false
            }
        )
    }

    showEditDialog?.let {
        EditTeacherDialog(
            teacher = it,
            onDismiss = { showEditDialog = null },
            onUpdateTeacher = {
                viewModel.updateTeacher(it)
                showEditDialog = null
            }
        )
    }

    showLogsDialog?.let {
        TeacherLogsDialog(
            teacher = it,
            viewModel = viewModel,
            onDismiss = { showLogsDialog = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultTopAppBar(
    onNavigateBack: () -> Unit,
    onEnterSelectionMode: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("Teacher Roster", fontWeight = FontWeight.Black) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(Icons.Default.Tune, contentDescription = "Options")
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Select Multiple") },
                    onClick = {
                        onEnterSelectionMode()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.LibraryAddCheck, contentDescription = null, tint = ForestGreen) }
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Import (CSV)") },
                    onClick = {
                        onImport()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Export (Excel)") },
                    onClick = {
                        onExport()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = ForestGreen
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopAppBar(
    selectionCount: Int,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit
) {
    TopAppBar(
        title = { Text("$selectionCount Selected", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(Icons.Default.Close, contentDescription = "Clear")
            }
        },
        actions = {
            IconButton(onClick = onSelectAll) {
                Icon(Icons.Default.SelectAll, contentDescription = "Select All")
            }
            IconButton(onClick = onDeleteSelected) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Delete", tint = StatusDisapproved)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EnhancedTeacherListItem(
    teacher: Teacher,
    isPaid: Boolean,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit,
    onViewLogs: () -> Unit,
    onPayrollToggle: () -> Unit,
    onDonationClick: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onItemLongClick()
                }
            ),
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) ForestGreen.copy(alpha = 0.1f) else Color.White,
        shadowElevation = if (isSelected) 0.dp else 2.dp,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, ForestGreen) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ForestGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (teacher.name.isNotEmpty()) teacher.name.take(1).uppercase() else "?",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = ForestGreen
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(teacher.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = ForestGreen, maxLines = 1)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = if (isPaid) StatusApproved.copy(alpha = 0.1f) else StatusDisapproved.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isPaid) "PAID" else "UNPAID",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isPaid) StatusApproved else StatusDisapproved
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(teacher.qualifications, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                    }
                }
                if (isSelected) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(24.dp))
                }
            }
            
            if (!isSelected) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Action Icons - High Responsiveness & Clear Feedback
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = onDonationClick,
                            shape = RoundedCornerShape(10.dp),
                            color = StatusApproved.copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.VolunteerActivism, null, tint = StatusApproved, modifier = Modifier.size(22.dp))
                            }
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Checkbox(
                                checked = isPaid,
                                onCheckedChange = { onPayrollToggle() },
                                colors = CheckboxDefaults.colors(checkedColor = StatusApproved)
                            )
                            Text("PAID", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (isPaid) StatusApproved else Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Main Action Buttons
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SmallActionButton("In", ForestGreenLight, onCheckIn, Modifier.weight(1f))
                        SmallActionButton("Out", StatusDisapproved, onCheckOut, Modifier.weight(1f))
                        SmallActionButton("Logs", Color.Gray, onViewLogs, Modifier.weight(1.1f), outline = true)
                    }
                }
            }
        }
    }
}

@Composable
fun SmallActionButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    outline: Boolean = false
) {
    if (outline) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, color)
        ) {
            Text(text, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = color)
        ) {
            Text(text, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTeacherDialog(
    onDismiss: () -> Unit,
    onAddTeacher: (Teacher) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var qualifications by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    var cnic by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recruit New Teacher", fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = qualifications, onValueChange = { qualifications = it }, label = { Text("Qualifications") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = contactInfo, onValueChange = { contactInfo = it }, label = { Text("Contact Info") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cnic, onValueChange = { cnic = it }, label = { Text("CNIC Number") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Residential Address") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("Monthly Salary") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newTeacher = Teacher(
                        name = name,
                        qualifications = qualifications,
                        contactInfo = contactInfo,
                        cnic = cnic,
                        address = address,
                        dob = "",
                        salary = salary.toDoubleOrNull() ?: 0.0
                    )
                    onAddTeacher(newTeacher)
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm Recruitment", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTeacherDialog(
    teacher: Teacher,
    onDismiss: () -> Unit,
    onUpdateTeacher: (Teacher) -> Unit
) {
    var name by remember { mutableStateOf(teacher.name) }
    var qualifications by remember { mutableStateOf(teacher.qualifications) }
    var contactInfo by remember { mutableStateOf(teacher.contactInfo) }
    var cnic by remember { mutableStateOf(teacher.cnic) }
    var address by remember { mutableStateOf(teacher.address) }
    var salary by remember { mutableStateOf(teacher.salary.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Teacher Info", fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = qualifications, onValueChange = { qualifications = it }, label = { Text("Qualifications") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = contactInfo, onValueChange = { contactInfo = it }, label = { Text("Contact Info") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cnic, onValueChange = { cnic = it }, label = { Text("CNIC Number") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Residential Address") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("Monthly Salary") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedTeacher = teacher.copy(
                        name = name,
                        qualifications = qualifications,
                        contactInfo = contactInfo,
                        cnic = cnic,
                        address = address,
                        dob = "",
                        salary = salary.toDoubleOrNull() ?: 0.0
                    )
                    onUpdateTeacher(updatedTeacher)
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Records", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TeacherLogsDialog(
    teacher: Teacher,
    viewModel: TeacherViewModel,
    onDismiss: () -> Unit
) {
    val logs by viewModel.getTeacherLogs(teacher.id).collectAsState(initial = emptyList())
    val sdf = remember { SimpleDateFormat("dd/MM/yy hh:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Logs: ${teacher.name}", fontWeight = FontWeight.Black) },
        text = {
            if (logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("No logs found for this teacher.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(logs) { log ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = BackgroundLight,
                            shadowElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Login, contentDescription = null, tint = StatusApproved, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("In: ${sdf.format(Date(log.checkInTime))}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Logout, contentDescription = null, tint = StatusDisapproved, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Out: ${log.checkOutTime?.let { sdf.format(Date(it)) } ?: "Ongoing"}", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) { Text("Close") }
        }
    )
}
