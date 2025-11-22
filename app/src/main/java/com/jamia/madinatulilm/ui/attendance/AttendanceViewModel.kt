package com.jamia.madinatulilm.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jamia.madinatulilm.data.AttendanceRecord
import com.jamia.madinatulilm.data.MadrasaClass
import com.jamia.madinatulilm.data.Student
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AttendanceViewModel : ViewModel() {

    private val database = Firebase.database
    private val classesRef = database.getReference("classes")
    private val studentsRef = database.getReference("students")
    private val attendanceRef = database.getReference("attendance")

    private val _allClasses = MutableStateFlow<List<MadrasaClass>>(emptyList())
    val allClasses: StateFlow<List<MadrasaClass>> = _allClasses

    private val _selectedClass = MutableStateFlow<MadrasaClass?>(null)
    val selectedClass: StateFlow<MadrasaClass?> = _selectedClass

    private val _selectedDate = MutableStateFlow(getCurrentDateString())
    val selectedDate: StateFlow<String> = _selectedDate

    private val _studentsInClass = MutableStateFlow<List<Student>>(emptyList())
    val studentsInClass: StateFlow<List<Student>> = _studentsInClass

    private val _attendanceRecords = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val attendanceRecords: StateFlow<List<AttendanceRecord>> = _attendanceRecords

    init {
        classesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val classList = snapshot.children.mapNotNull { it.getValue(MadrasaClass::class.java) }
                _allClasses.value = classList
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        viewModelScope.launch {
            selectedDate.collect {
                attendanceRef.orderByChild("date").equalTo(it)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            _attendanceRecords.value = snapshot.children.mapNotNull { it.getValue(AttendanceRecord::class.java) }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error
                        }
                    })
            }
        }
    }

    fun onClassSelected(madrasaClass: MadrasaClass?) {
        _selectedClass.value = madrasaClass
        if (madrasaClass != null) {
            studentsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val allStudents = snapshot.children.mapNotNull { it.getValue(Student::class.java) }
                    _studentsInClass.value = allStudents.filter { student -> madrasaClass.studentIds.contains(student.id) }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    fun onDateSelected(millis: Long) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        _selectedDate.value = sdf.format(calendar.time)
    }

    fun saveAttendance(studentId: String, status: String) {
        viewModelScope.launch {
            val classId = _selectedClass.value?.id ?: return@launch
            val date = _selectedDate.value
            val existingRecord = _attendanceRecords.value.find { it.studentId == studentId }

            if (existingRecord != null) {
                val updatedAttendance = existingRecord.attendance.toMutableMap()
                updatedAttendance[classId] = status
                attendanceRef.child(existingRecord.id).child("attendance").setValue(updatedAttendance)
            } else {
                val recordId = attendanceRef.push().key ?: return@launch
                val newAttendance = mapOf(classId to status)
                val record = AttendanceRecord(
                    id = recordId,
                    studentId = studentId,
                    date = date,
                    attendance = newAttendance
                )
                attendanceRef.child(recordId).setValue(record)
            }
        }
    }

    fun clearAttendance() {
        viewModelScope.launch {
            val classId = _selectedClass.value?.id ?: return@launch
            val date = _selectedDate.value
            _attendanceRecords.value.forEach { record ->
                val updatedAttendance = record.attendance.toMutableMap()
                if (updatedAttendance.containsKey(classId)) {
                    updatedAttendance.remove(classId)
                    attendanceRef.child(record.id).child("attendance").setValue(updatedAttendance)
                }
            }
        }
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
