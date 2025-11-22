package com.jamia.madinatulilm.ui.classes

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jamia.madinatulilm.data.MadrasaClass
import com.jamia.madinatulilm.data.Student
import com.jamia.madinatulilm.data.Teacher

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ClassScreen(viewModel: ClassViewModel, onNavigateBack: () -> Unit) {
    val classes by viewModel.allClasses.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedClassIds by viewModel.selectedClassIds.collectAsState()
    val editingClass by viewModel.editingClass.collectAsState()

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                SelectionTopAppBar(
                    selectionCount = selectedClassIds.size,
                    onClearSelection = { viewModel.toggleSelectionMode() },
                    onSelectAll = { viewModel.selectAll() },
                    onDeleteSelected = { viewModel.deleteSelectedClasses() }
                )
            } else {
                DefaultTopAppBar(onNavigateBack = onNavigateBack, onEnterSelectionMode = { viewModel.toggleSelectionMode() })
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(onClick = { viewModel.startEditing(null) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Class")
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
            items(classes, key = { it.id }) {
                ClassListItem(
                    madrasaClass = it,
                    isSelected = selectedClassIds.contains(it.id),
                    onItemClick = {
                        if (isSelectionMode) {
                            viewModel.toggleClassSelection(it.id)
                        } else {
                            viewModel.startEditing(it)
                        }
                    },
                    onItemLongClick = {
                        if (!isSelectionMode) {
                            viewModel.toggleSelectionMode()
                        }
                        viewModel.toggleClassSelection(it.id)
                    },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }

    editingClass?.let {
        val allStudents by viewModel.allStudents.collectAsState()
        val allTeachers by viewModel.allTeachers.collectAsState()
        ClassEditDialog(
            madrasaClass = it,
            allStudents = allStudents,
            allTeachers = allTeachers,
            onDismiss = { viewModel.stopEditing() },
            onConfirm = {
                if (it.id.isBlank()) viewModel.addClass(it) else viewModel.updateClass(it)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultTopAppBar(onNavigateBack: () -> Unit, onEnterSelectionMode: () -> Unit) {
    TopAppBar(
        title = { Text("Manage Classes") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onEnterSelectionMode) {
                Icon(Icons.Default.CheckBox, contentDescription = "Select")
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
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
            IconButton(onClick = onSelectAll) {
                Icon(Icons.Default.SelectAll, contentDescription = "Select All")
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClassListItem(
    madrasaClass: MadrasaClass,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
            }
            Text(madrasaClass.className, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}

@Composable
fun ClassEditDialog(
    madrasaClass: MadrasaClass,
    allStudents: List<Student>,
    allTeachers: List<Teacher>,
    onDismiss: () -> Unit,
    onConfirm: (MadrasaClass) -> Unit
) {
    var className by remember { mutableStateOf(madrasaClass.className) }
    var teacherId by remember { mutableStateOf(madrasaClass.teacherId) }
    val selectedStudentIds by remember { mutableStateOf(madrasaClass.studentIds.toMutableSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (madrasaClass.id.isBlank()) "Add Class" else "Edit Class") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = className, onValueChange = { className = it }, label = { Text("Class Name") })

                if (allTeachers.isNotEmpty()) {
                    TeacherDropdown(allTeachers, teacherId) { teacherId = it }
                }

                Text("Students", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 16.dp))
                allStudents.forEach { student ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = selectedStudentIds.contains(student.id),
                            onCheckedChange = {
                                if (it) selectedStudentIds.add(student.id) else selectedStudentIds.remove(student.id)
                            }
                        )
                        Text(student.fullName)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(madrasaClass.copy(className = className, teacherId = teacherId, studentIds = selectedStudentIds.toList()))
            }) { Text("Confirm") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDropdown(teachers: List<Teacher>, selectedId: String, onSelection: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = teachers.find { it.id == selectedId }?.name ?: "Select Teacher",
            onValueChange = { },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            teachers.forEach { teacher ->
                DropdownMenuItem(text = { Text(teacher.name) }, onClick = {
                    onSelection(teacher.id)
                    expanded = false
                })
            }
        }
    }
}
