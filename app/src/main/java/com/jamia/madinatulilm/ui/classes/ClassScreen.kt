package com.jamia.madinatulilm.ui.classes

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jamia.madinatulilm.data.MadrasaClass
import com.jamia.madinatulilm.data.Student
import com.jamia.madinatulilm.data.Teacher
import com.jamia.madinatulilm.ui.theme.PrimaryGreen
import com.jamia.madinatulilm.ui.theme.PrimaryGreenLight

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ClassScreen(viewModel: ClassViewModel, onNavigateBack: () -> Unit) {
    val classes by viewModel.allClasses.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedClassIds by viewModel.selectedClassIds.collectAsState()
    val editingClass by viewModel.editingClass.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

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
                FloatingActionButton(
                    onClick = { viewModel.startEditing(null) },
                    containerColor = PrimaryGreen,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Class")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    }
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
            onConfirm = { editedClass ->
                if (editedClass.id.isBlank()) viewModel.addClass(editedClass) else viewModel.updateClass(editedClass)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultTopAppBar(onNavigateBack: () -> Unit, onEnterSelectionMode: () -> Unit) {
    TopAppBar(
        title = { Text("Madrassa Classes", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onEnterSelectionMode) {
                Icon(Icons.Default.CheckBox, contentDescription = "Select")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = PrimaryGreen,
            navigationIconContentColor = PrimaryGreen,
            actionIconContentColor = PrimaryGreen
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
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
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
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onItemLongClick()
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = PrimaryGreen
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = madrasaClass.className,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (madrasaClass.level.isNotBlank() || madrasaClass.room.isNotBlank()) {
                    Text(
                        text = "${madrasaClass.level} • Room: ${madrasaClass.room}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${madrasaClass.studentIds.size} Students",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Medium
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassEditDialog(
    madrasaClass: MadrasaClass,
    allStudents: List<Student>,
    allTeachers: List<Teacher>,
    onDismiss: () -> Unit,
    onConfirm: (MadrasaClass) -> Unit
) {
    var className by remember { mutableStateOf(madrasaClass.className) }
    var level by remember { mutableStateOf(madrasaClass.level) }
    var room by remember { mutableStateOf(madrasaClass.room) }
    var teacherId by remember { mutableStateOf(madrasaClass.teacherId) }
    val selectedStudentIds = remember { mutableStateListOf<String>().apply { addAll(madrasaClass.studentIds) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (madrasaClass.id.isBlank()) "Add New Class" else "Edit Class Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("Class Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = level,
                        onValueChange = { level = it },
                        label = { Text("Level") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text("Room") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (allTeachers.isNotEmpty()) {
                    TeacherDropdown(allTeachers, teacherId) { teacherId = it }
                }

                Text(
                    "Enroll Students",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        allStudents.forEach { student ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (selectedStudentIds.contains(student.id)) {
                                            selectedStudentIds.remove(student.id)
                                        } else {
                                            selectedStudentIds.add(student.id)
                                        }
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = selectedStudentIds.contains(student.id),
                                    onCheckedChange = { checked ->
                                        if (checked == true) selectedStudentIds.add(student.id) else selectedStudentIds.remove(student.id)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen)
                                )
                                Text(student.fullName, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(madrasaClass.copy(
                        className = className,
                        level = level,
                        room = room,
                        teacherId = teacherId,
                        studentIds = selectedStudentIds.toList()
                    ))
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDropdown(teachers: List<Teacher>, selectedId: String, onSelection: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = teachers.find { it.id == selectedId }?.name ?: "Assign Teacher",
            onValueChange = { },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            teachers.forEach { teacher ->
                DropdownMenuItem(
                    text = { Text(teacher.name) },
                    onClick = {
                        onSelection(teacher.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
