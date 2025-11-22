package com.jamia.madinatulilm.ui.attendance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jamia.madinatulilm.data.AttendanceRecord
import com.jamia.madinatulilm.data.MadrasaClass
import com.jamia.madinatulilm.data.Student

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
                title = { Text(selectedClass?.className ?: "Manage Attendance") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearAttendance() }, enabled = selectedClass != null) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Attendance")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Use a Column for better spacing and full-width controls
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (classId == null) {
                    Text("Class", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    ClassDropdown(classes, selectedClass, viewModel::onClassSelected)
                } else {
                    // If classId is passed, show the class name statically
                    Text("Class", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = selectedClass?.className ?: "",
                        onValueChange = {}, 
                        readOnly = true, 
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text("Date", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedDate)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

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
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    Text("Please select a class to view attendance.")
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
            value = selectedClass?.className ?: "Select a class",
            onValueChange = { },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            classes.forEach { madrasaClass ->
                DropdownMenuItem(text = { Text(madrasaClass.className) }, onClick = {
                    onClassSelected(madrasaClass)
                    expanded = false
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceItem(student: Student, record: AttendanceRecord?, classId: String?, onSave: (String, String) -> Unit, onStudentClick: () -> Unit) {
    var status by remember(record, classId) { mutableStateOf(record?.attendance?.get(classId) ?: "") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = student.fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onStudentClick)
            )
            Spacer(modifier = Modifier.height(12.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    shape = ButtonDefaults.shape,
                    onClick = { status = "Present"; onSave(student.id, "Present") },
                    selected = status == "Present"
                ) {
                    Text("Present")
                }
                SegmentedButton(
                    shape = ButtonDefaults.shape,
                    onClick = { status = "Absent"; onSave(student.id, "Absent") },
                    selected = status == "Absent"
                ) {
                    Text("Absent")
                }
                SegmentedButton(
                    shape = ButtonDefaults.shape,
                    onClick = { status = "Leave"; onSave(student.id, "Leave") },
                    selected = status == "Leave"
                ) {
                    Text("Leave")
                }
            }
        }
    }
}
