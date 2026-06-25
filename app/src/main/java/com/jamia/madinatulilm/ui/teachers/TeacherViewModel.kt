package com.jamia.madinatulilm.ui.teachers

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jamia.madinatulilm.data.Teacher
import com.jamia.madinatulilm.data.TeacherLog
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TeacherViewModel : ViewModel() {

    private val database by lazy { Firebase.database }
    private val teachersRef by lazy { database.getReference("teachers") }
    private val teacherLogsRef by lazy { database.getReference("teacher_logs") }

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode = _isSelectionMode.asStateFlow()

    private val _selectedTeacherIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedTeacherIds = _selectedTeacherIds.asStateFlow()

    private val _allTeachers = MutableStateFlow<List<Teacher>>(emptyList())

    val allTeachers: StateFlow<List<Teacher>> = searchText
        .combine(_allTeachers) { text, teachers ->
            if (text.isBlank()) {
                teachers
            } else {
                teachers.filter { it.doesMatchSearchQuery(text) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    init {
        teachersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val teacherList = snapshot.children.mapNotNull { it.getValue(Teacher::class.java) }
                    _allTeachers.value = teacherList
                } catch (e: Exception) {
                    android.util.Log.e("TeacherViewModel", "Data parsing failed", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TeacherViewModel", "Error fetching teachers", error.toException())
            }
        })
    }

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    fun onSearchTextChanged(text: String) {
        _searchText.value = text
    }

    fun toggleSelectionMode() {
        _isSelectionMode.value = !_isSelectionMode.value
        if (!_isSelectionMode.value) {
            _selectedTeacherIds.value = emptySet()
        }
    }

    fun toggleTeacherSelection(teacherId: String) {
        val currentSelection = _selectedTeacherIds.value.toMutableSet()
        if (currentSelection.contains(teacherId)) {
            currentSelection.remove(teacherId)
        } else {
            currentSelection.add(teacherId)
        }
        _selectedTeacherIds.value = currentSelection
    }

    fun selectAll() {
        val allIds = allTeachers.value.map { it.id }.toSet()
        _selectedTeacherIds.value = allIds
    }

    fun clearSelection() {
        _selectedTeacherIds.value = emptySet()
    }

    fun deleteSelectedTeachers() {
        viewModelScope.launch {
            val selectedIds = _selectedTeacherIds.value
            selectedIds.forEach { teacherId ->
                teachersRef.child(teacherId).removeValue()
            }
            toggleSelectionMode()
        }
    }

    fun addTeacher(teacher: Teacher) {
        viewModelScope.launch {
            val key = teachersRef.push().key
            if (key != null) {
                val newTeacher = teacher.copy(id = key)
                teachersRef.child(key).setValue(newTeacher)
                    .addOnSuccessListener { Log.d("TeacherViewModel", "Teacher added successfully") }
                    .addOnFailureListener { Log.e("TeacherViewModel", "Error adding teacher", it) }
            }
        }
    }

    fun togglePayrollStatus(teacher: Teacher) {
        viewModelScope.launch {
            val updatedTeacher = teacher.copy(isPaid = !teacher.isPaid)
            updateTeacher(updatedTeacher)
        }
    }

    fun updateTeacher(teacher: Teacher) {
        viewModelScope.launch {
            teachersRef.child(teacher.id).setValue(teacher)
                .addOnSuccessListener { Log.d("TeacherViewModel", "Teacher updated successfully") }
                .addOnFailureListener { Log.e("TeacherViewModel", "Error updating teacher", it) }
        }
    }

    fun checkIn(teacherId: String) {
        viewModelScope.launch {
            teacherLogsRef.orderByChild("teacherId").equalTo(teacherId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val openLog = snapshot.children.mapNotNull { it.getValue(TeacherLog::class.java) }
                            .firstOrNull { it.checkOutTime == null }

                        if (openLog == null) {
                            val logKey = teacherLogsRef.push().key
                            if (logKey != null) {
                                val newLog = TeacherLog(id = logKey, teacherId = teacherId, checkInTime = System.currentTimeMillis())
                                teacherLogsRef.child(logKey).setValue(newLog)
                                viewModelScope.launch { _toastMessage.emit("Checked in successfully") }
                            }
                        } else {
                            viewModelScope.launch { _toastMessage.emit("Already checked in") }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
        }
    }

    fun checkOut(teacherId: String) {
        viewModelScope.launch {
            teacherLogsRef.orderByChild("teacherId").equalTo(teacherId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val openLog = snapshot.children.mapNotNull { it.getValue(TeacherLog::class.java) }
                            .firstOrNull { it.checkOutTime == null }

                        if (openLog != null) {
                            teacherLogsRef.child(openLog.id).child("checkOutTime").setValue(System.currentTimeMillis())
                            viewModelScope.launch { _toastMessage.emit("Checked out successfully") }
                        } else {
                            viewModelScope.launch { _toastMessage.emit("No open check-in found") }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
        }
    }

    fun getTeacherLogs(teacherId: String): Flow<List<TeacherLog>> {
        val flow = MutableStateFlow<List<TeacherLog>>(emptyList())
        teacherLogsRef.orderByChild("teacherId").equalTo(teacherId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val logs = snapshot.children.mapNotNull { it.getValue(TeacherLog::class.java) }
                    flow.value = logs
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        return flow
    }

    fun onToastShown() {
        viewModelScope.launch {
            // This is a placeholder to consume the event
        }
    }
}

// Helper function for searching
fun Teacher.doesMatchSearchQuery(query: String): Boolean {
    val matchingCombinations = listOf(
        name,
        cnic,
        qualifications,
        contactInfo
    )
    return matchingCombinations.any { it.contains(query, ignoreCase = true) }
}
