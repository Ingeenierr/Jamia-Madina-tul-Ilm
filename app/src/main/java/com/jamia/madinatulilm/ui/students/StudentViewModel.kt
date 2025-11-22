package com.jamia.madinatulilm.ui.students

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jamia.madinatulilm.data.Student
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StudentViewModel : ViewModel() {

    private val database = Firebase.database
    private val studentsRef = database.getReference("students")

    private val _allStudents = MutableStateFlow<List<Student>>(emptyList())
    val allStudents: StateFlow<List<Student>> = _allStudents

    init {
        studentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val studentList = snapshot.children.mapNotNull { it.getValue(Student::class.java) }
                _allStudents.value = studentList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("StudentViewModel", "Error fetching students", error.toException())
            }
        })
    }

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode = _isSelectionMode.asStateFlow()

    private val _selectedStudentIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedStudentIds = _selectedStudentIds.asStateFlow()

    fun toggleSelectionMode() {
        _isSelectionMode.value = !_isSelectionMode.value
        if (!_isSelectionMode.value) {
            _selectedStudentIds.value = emptySet()
        }
    }

    fun toggleStudentSelection(studentId: String) {
        val currentSelection = _selectedStudentIds.value.toMutableSet()
        if (currentSelection.contains(studentId)) {
            currentSelection.remove(studentId)
        } else {
            currentSelection.add(studentId)
        }
        _selectedStudentIds.value = currentSelection
    }

    fun selectAll() {
        val allIds = allStudents.value.map { it.id }.toSet()
        _selectedStudentIds.value = allIds
    }

    fun clearSelection() {
        _selectedStudentIds.value = emptySet()
    }

    fun deleteSelectedStudents() {
        viewModelScope.launch {
            val selectedIds = _selectedStudentIds.value
            selectedIds.forEach { studentId ->
                studentsRef.child(studentId).removeValue()
            }
            toggleSelectionMode()
        }
    }

    fun addStudent(student: Student) {
        viewModelScope.launch {
            val key = studentsRef.push().key
            if (key != null) {
                val newStudent = student.copy(id = key)
                studentsRef.child(key).setValue(newStudent)
                    .addOnSuccessListener { Log.d("StudentViewModel", "Student added successfully") }
                    .addOnFailureListener { Log.e("StudentViewModel", "Error adding student", it) }
            }
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            studentsRef.child(student.id).setValue(student)
                .addOnSuccessListener { Log.d("StudentViewModel", "Student updated successfully") }
                .addOnFailureListener { Log.e("StudentViewModel", "Error updating student", it) }
        }
    }
}
