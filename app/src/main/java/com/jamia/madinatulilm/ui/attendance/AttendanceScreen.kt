package com.jamia.madinatulilm.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jamia.madinatulilm.data.AttendanceRecord
import com.jamia.madinatulilm.data.MadrasaClass
import com.jamia.madinatulilm.data.Student
import com.jamia.madinatulilm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    navController: NavController,
    viewModel: AttendanceViewModel,
    onNavigateBack: () -> Unit,
    classId: String?
) {
    val classes by viewModel.allClasses.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val studentsInClass by viewModel.studentsInClass.collectAsState()
    val attendanceRecords by viewModel.attendanceRecords.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(classes, classId) {
        if (classId != null && classes.isNotEmpty()) {
            viewModel.onClassSelected(classes.find { it.id == classId })
        } else {
            viewModel.onClassSelected(null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = selectedClass?.className ?: "Attendance",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (selectedClass != null) {
                            Text(
                                text = "${selectedClass?.level} - ${selectedClass?.room}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearAttendance() }, enabled = selectedClass != null) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Attendance", tint = StatusDisapproved)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = PrimaryGreen
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (classId == null) {
                        ClassDropdown(classes, selectedClass, viewModel::onClassSelected)
                    }

                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(selectedDate, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState()
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    viewModel.onDateSelected(it)
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            if (selectedClass != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(studentsInClass) { student ->
                        val record = attendanceRecords.find { it.studentId == student.id }
                        AttendanceItem(
                            student = student,
                            record = record,
                            classId = selectedClass?.id,
                            onSave = viewModel::saveAttendance,
                            onStudentClick = { navController.navigate("student_profile/${student.id}") }
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select a class to manage attendance", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDropdown(classes: List<MadrasaClass>, selectedClass: MadrasaClass?, onClassSelected: (MadrasaClass?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedClass?.className ?: "Select Class",
            onValueChange = { },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            classes.forEach { madrasaClass ->
                DropdownMenuItem(
                    text = { 
                        Column {
                            Text(madrasaClass.className, fontWeight = FontWeight.Medium)
                            Text(madrasaClass.level, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    },
                    onClick = {
                        onClassSelected(madrasaClass)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AttendanceItem(
    student: Student,
    record: AttendanceRecord?,
    classId: String?,
    onSave: (String, String) -> Unit,
    onStudentClick: () -> Unit
) {
    var status by remember(record, classId) { mutableStateOf(record?.attendance?.get(classId) ?: "") }
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Student Info
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onStudentClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${student.id.takeLast(6)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Attendance Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AttendanceToggleNode(
                    label = "P",
                    isSelected = status == "Present",
                    activeColor = AttendancePresent,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        status = "Present"
                        onSave(student.id, "Present")
                    }
                )
                AttendanceToggleNode(
                    label = "A",
                    isSelected = status == "Absent",
                    activeColor = AttendanceAbsent,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        status = "Absent"
                        onSave(student.id, "Absent")
                    }
                )
                AttendanceToggleNode(
                    label = "L",
                    isSelected = status == "Leave",
                    activeColor = AttendanceLeave,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        status = "Leave"
                        onSave(student.id, "Leave")
                    }
                )
            }
        }
    }
}

@Composable
fun AttendanceToggleNode(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) activeColor else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (isSelected) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
