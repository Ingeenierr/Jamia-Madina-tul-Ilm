package com.jamia.madinatulilm.ui.teachers

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jamia.madinatulilm.data.Teacher
import com.jamia.madinatulilm.data.TeacherLog
import com.jamia.madinatulilm.utils.ExcelExporter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TeacherScreen(
    viewModel: TeacherViewModel,
    onNavigateBack: () -> Unit
) {
    val teachers by viewModel.allTeachers.collectAsState()
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

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
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
                    onExport = { exportLauncher.launch("Teachers_${System.currentTimeMillis()}.csv") }
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Teacher")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(teachers, key = { it.id }) { teacher ->
                TeacherListItem(
                    teacher = teacher,
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
                    modifier = Modifier.animateItemPlacement()
                )
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
        title = { Text("Manage Teachers") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More Options")
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Select") },
                    onClick = {
                        onEnterSelectionMode()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.CheckBox, contentDescription = "Select") }
                )
                DropdownMenuItem(
                    text = { Text("Import from CSV") },
                    onClick = {
                        onImport()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Upload, contentDescription = "Import") }
                )
                DropdownMenuItem(
                    text = { Text("Export to CSV") },
                    onClick = {
                        onExport()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Download, contentDescription = "Export") }
                )
            }
        }
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
        title = { Text("$selectionCount selected") },
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(Icons.Default.Close, contentDescription = "Clear Selection")
            }
        },
        actions = {
            IconButton(onClick = onDeleteSelected) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Selected")
            }
            IconButton(onClick = onSelectAll) {
                Icon(Icons.Default.SelectAll, contentDescription = "Select All")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TeacherListItem(
    teacher: Teacher,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit,
    onViewLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = onItemLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (isSelected) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(teacher.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Qualifications: ${teacher.qualifications}", fontSize = 16.sp, color = Color.Gray)
                    Text("Salary: ${teacher.salary}", fontSize = 16.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onCheckIn) { Text("Check-in") }
                Button(onClick = onCheckOut) { Text("Check-out") }
                OutlinedButton(onClick = onViewLogs) { Text("View Logs") }
            }
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
        title = { Text("Add New Teacher") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                OutlinedTextField(value = qualifications, onValueChange = { qualifications = it }, label = { Text("Qualifications") })
                OutlinedTextField(value = contactInfo, onValueChange = { contactInfo = it }, label = { Text("Contact Info") })
                OutlinedTextField(value = cnic, onValueChange = { cnic = it }, label = { Text("CNIC") })
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") })
                OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("Salary") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
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
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
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
        title = { Text("Edit Teacher") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                OutlinedTextField(value = qualifications, onValueChange = { qualifications = it }, label = { Text("Qualifications") })
                OutlinedTextField(value = contactInfo, onValueChange = { contactInfo = it }, label = { Text("Contact Info") })
                OutlinedTextField(value = cnic, onValueChange = { cnic = it }, label = { Text("CNIC") })
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") })
                OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("Salary") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
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
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
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
        title = { Text("Logs for ${teacher.name}") },
        text = {
            if (logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No logs found for this teacher.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(logs) { log ->
                        Card(elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Check-in: ${sdf.format(Date(log.checkInTime))}", fontWeight = FontWeight.Bold)
                                Text("Check-out: ${log.checkOutTime?.let { sdf.format(Date(it)) } ?: "Not yet"}")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}
