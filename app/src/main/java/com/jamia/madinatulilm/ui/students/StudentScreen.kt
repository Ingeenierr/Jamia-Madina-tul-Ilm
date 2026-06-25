package com.jamia.madinatulilm.ui.students

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jamia.madinatulilm.data.Student
import com.jamia.madinatulilm.ui.theme.*
import com.jamia.madinatulilm.utils.ExcelExporter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StudentScreen(
    viewModel: StudentViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDonations: (String) -> Unit
) {
    val allStudents by viewModel.allStudents.collectAsState()
    val activeStudents by viewModel.activeStudents.collectAsState()
    val expelledStudents by viewModel.expelledStudents.collectAsState()
    val recentStudents by viewModel.recentStudents.collectAsState()
    val classNames by viewModel.classNames.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedStudentIds by viewModel.selectedStudentIds.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Active", "Expelled", "Recent")

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Student?>(null) }
    val context = LocalContext.current

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

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

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri ->
        uri?.let { ExcelExporter.exportStudentsToExcel(context, allStudents, it) }
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
                    onExport = { exportLauncher.launch("Students_${System.currentTimeMillis()}.xlsx") }
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = ForestGreen,
                    contentColor = LuxuryGold
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Student")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(SoftCream, Color.White)))
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = ForestGreen
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            val currentList = when (selectedTab) {
                0 -> activeStudents
                1 -> expelledStudents
                else -> recentStudents
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
            ) {
                items(currentList, key = { it.id }) { student ->
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
                        onItemLongClick = {
                            if (!isSelectionMode) {
                                viewModel.toggleSelectionMode()
                            }
                            viewModel.toggleStudentSelection(student.id)
                        },
                        onNavigateToDonations = { onNavigateToDonations(student.id) },
                        className = classNames[student.classId] ?: "No Class Assigned",
                        onToggleStatus = { viewModel.toggleStudentStatus(student) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddStudentDialog(
            classNames = classNames,
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
            classNames = classNames,
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
                    text = { Text("Export to Excel") },
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
    className: String,
    onToggleStatus: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = onItemLongClick
            ),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) ForestGreen.copy(alpha = 0.1f) else Color.White,
        shadowElevation = if (isSelected) 0.dp else 2.dp,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, ForestGreen) else null
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
                Text(
                    text = student.fullName.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(student.fullName, fontWeight = FontWeight.ExtraBold, color = ForestGreen)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.School, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(className, fontSize = 11.sp, color = if (className == "No Class Assigned") Color.Red.copy(alpha = 0.6f) else ForestGreen.copy(alpha = 0.8f))
                }
                Text("ID: ${student.id.takeLast(6).uppercase()}", fontSize = 10.sp, color = Color.Gray)
            }
            if (!isSelected) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateToDonations) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = DeepGold)
                    }
                    
                    Surface(
                        onClick = onToggleStatus,
                        color = if (student.isActive) StatusApproved.copy(alpha = 0.1f) else StatusDisapproved.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = if (student.isActive) "ACTIVE" else "EXPELLED",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (student.isActive) StatusApproved else StatusDisapproved
                        )
                    }
                }
            } else {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentDialog(
    classNames: Map<String, String>,
    onDismiss: () -> Unit,
    onAddStudent: (Student) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var guardianName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var cnic by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var classId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Student") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                
                // Class Selection Dropdown
                ClassDropdown(classNames = classNames, selectedClassId = classId) { classId = it }

                OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = guardianName, onValueChange = { guardianName = it }, label = { Text("Guardian's Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = contactNumber, onValueChange = { contactNumber = it }, label = { Text("Contact Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cnic, onValueChange = { cnic = it }, label = { Text("CNIC") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
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
                        classId = classId,
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
    classNames: Map<String, String>,
    onDismiss: () -> Unit,
    onUpdateStudent: (Student) -> Unit
) {
    var fullName by remember { mutableStateOf(student.fullName) }
    var age by remember { mutableStateOf(student.age.toString()) }
    var guardianName by remember { mutableStateOf(student.guardianName) }
    var contactNumber by remember { mutableStateOf(student.contactNumber) }
    var cnic by remember { mutableStateOf(student.cnic) }
    var address by remember { mutableStateOf(student.address) }
    var isActive by remember { mutableStateOf(student.isActive) }
    var classId by remember { mutableStateOf(student.classId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Student") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                
                // Class Selection Dropdown
                ClassDropdown(classNames = classNames, selectedClassId = classId) { classId = it }

                OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = guardianName, onValueChange = { guardianName = it }, label = { Text("Guardian's Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = contactNumber, onValueChange = { contactNumber = it }, label = { Text("Contact Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cnic, onValueChange = { cnic = it }, label = { Text("CNIC") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Is Active", modifier = Modifier.weight(1f))
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
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
                        isActive = isActive,
                        classId = classId
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDropdown(
    classNames: Map<String, String>,
    selectedClassId: String,
    onSelection: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = classNames[selectedClassId] ?: "Select Class",
            onValueChange = {},
            readOnly = true,
            label = { Text("Assigned Class") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("No Class Assigned") },
                onClick = {
                    onSelection("")
                    expanded = false
                }
            )
            classNames.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelection(id)
                        expanded = false
                    }
                )
            }
        }
    }
}
