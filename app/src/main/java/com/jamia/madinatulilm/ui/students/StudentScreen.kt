package com.jamia.madinatulilm.ui.students

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jamia.madinatulilm.data.Student
import com.jamia.madinatulilm.utils.ExcelExporter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StudentScreen(
    viewModel: StudentViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDonations: (String) -> Unit
) {
    val students by viewModel.allStudents.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedStudentIds by viewModel.selectedStudentIds.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Student?>(null) }
    val context = LocalContext.current

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val importedStudents = ExcelExporter.importStudentsFromExcel(context, it)
            if (importedStudents.isNotEmpty()) {
                importedStudents.forEach(viewModel::addStudent)
                Toast.makeText(context, "Successfully imported ${importedStudents.size} students.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Import failed or file is empty/invalid.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { ExcelExporter.exportStudentsToExcel(context, students, it) }
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                SelectionTopAppBar(
                    selectionCount = selectedStudentIds.size,
                    onClearSelection = { viewModel.toggleSelectionMode() },
                    onSelectAll = { viewModel.selectAll() },
                    onDeleteSelected = { viewModel.deleteSelectedStudents() }
                )
            } else {
                DefaultTopAppBar(
                    onNavigateBack = onNavigateBack,
                    onEnterSelectionMode = { viewModel.toggleSelectionMode() },
                    onImport = { importLauncher.launch("*/*") },
                    onExport = { exportLauncher.launch("Students_${System.currentTimeMillis()}.csv") }
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Student")
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
            items(students, key = { it.id }) { student ->
                StudentListItem(
                    student = student,
                    isSelected = selectedStudentIds.contains(student.id),
                    onItemClick = {
                        if (isSelectionMode) {
                            viewModel.toggleStudentSelection(student.id)
                        } else {
                            showEditDialog = student
                        }
                    },
                    onItemLongClick = { // Long press still works as a shortcut
                        if (!isSelectionMode) {
                            viewModel.toggleSelectionMode()
                        }
                        viewModel.toggleStudentSelection(student.id)
                    },
                    onNavigateToDonations = { onNavigateToDonations(student.id) },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }

    if (showAddDialog) {
        AddStudentDialog(
            onDismiss = { showAddDialog = false },
            onAddStudent = {
                viewModel.addStudent(it)
                showAddDialog = false
            }
        )
    }

    showEditDialog?.let {
        EditStudentDialog(
            student = it,
            onDismiss = { showEditDialog = null },
            onUpdateStudent = {
                viewModel.updateStudent(it)
                showEditDialog = null
            }
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
        title = { Text("Manage Students") },
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
fun StudentListItem(
    student: Student,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
    onNavigateToDonations: () -> Unit,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(student.fullName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Guardian: ${student.guardianName}", fontSize = 16.sp, color = Color.Gray)
                Text("CNIC: ${student.cnic}", fontSize = 14.sp, color = Color.Gray)
            }
            if (!isSelected) {
                IconButton(onClick = onNavigateToDonations) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = "Donations")
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentDialog(
    onDismiss: () -> Unit,
    onAddStudent: (Student) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var guardianName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var cnic by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Student") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") })
                OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") })
                OutlinedTextField(value = guardianName, onValueChange = { guardianName = it }, label = { Text("Guardian's Name") })
                OutlinedTextField(value = contactNumber, onValueChange = { contactNumber = it }, label = { Text("Contact Number") })
                OutlinedTextField(value = cnic, onValueChange = { cnic = it }, label = { Text("CNIC") })
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newStudent = Student(
                        fullName = fullName,
                        age = age.toIntOrNull() ?: 0,
                        guardianName = guardianName,
                        contactNumber = contactNumber,
                        cnic = cnic,
                        address = address,
                        dob = ""
                    )
                    onAddStudent(newStudent)
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
fun EditStudentDialog(
    student: Student,
    onDismiss: () -> Unit,
    onUpdateStudent: (Student) -> Unit
) {
    var fullName by remember { mutableStateOf(student.fullName) }
    var age by remember { mutableStateOf(student.age.toString()) }
    var guardianName by remember { mutableStateOf(student.guardianName) }
    var contactNumber by remember { mutableStateOf(student.contactNumber) }
    var cnic by remember { mutableStateOf(student.cnic) }
    var address by remember { mutableStateOf(student.address) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Student") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") })
                OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") })
                OutlinedTextField(value = guardianName, onValueChange = { guardianName = it }, label = { Text("Guardian's Name") })
                OutlinedTextField(value = contactNumber, onValueChange = { contactNumber = it }, label = { Text("Contact Number") })
                OutlinedTextField(value = cnic, onValueChange = { cnic = it }, label = { Text("CNIC") })
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedStudent = student.copy(
                        fullName = fullName,
                        age = age.toIntOrNull() ?: 0,
                        guardianName = guardianName,
                        contactNumber = contactNumber,
                        cnic = cnic,
                        address = address,
                        dob = ""
                    )
                    onUpdateStudent(updatedStudent)
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
