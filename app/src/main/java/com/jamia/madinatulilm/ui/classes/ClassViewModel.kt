package com.jamia.madinatulilm.ui.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jamia.madinatulilm.data.MadrasaClass
import com.jamia.madinatulilm.data.Student
import com.jamia.madinatulilm.data.Teacher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ClassViewModel : ViewModel() {

    private val database = Firebase.database
    private val classesRef = database.getReference("classes")
    private val studentsRef = database.getReference("students")
    private val teachersRef = database.getReference("teachers")

    private val _allClasses = MutableStateFlow<List<MadrasaClass>>(emptyList())
    val allClasses: StateFlow<List<MadrasaClass>> = _allClasses

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode = _isSelectionMode.asStateFlow()

    private val _selectedClassIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedClassIds = _selectedClassIds.asStateFlow()

    private val _editingClass = MutableStateFlow<MadrasaClass?>(null)
    val editingClass = _editingClass.asStateFlow()

    private val _allStudents = MutableStateFlow<List<Student>>(emptyList())
    val allStudents = _allStudents.asStateFlow()

    private val _allTeachers = MutableStateFlow<List<Teacher>>(emptyList())
    val allTeachers = _allTeachers.asStateFlow()

    init {
        classesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _allClasses.value = snapshot.children.mapNotNull { it.getValue(MadrasaClass::class.java) }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        studentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _allStudents.value = snapshot.children.mapNotNull { it.getValue(Student::class.java) }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        teachersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _allTeachers.value = snapshot.children.mapNotNull { it.getValue(Teacher::class.java) }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun toggleSelectionMode() {
        _isSelectionMode.value = !_isSelectionMode.value
        if (!_isSelectionMode.value) {
            _selectedClassIds.value = emptySet()
        }
    }

    fun toggleClassSelection(classId: String) {
        val currentSelection = _selectedClassIds.value.toMutableSet()
        if (currentSelection.contains(classId)) {
            currentSelection.remove(classId)
        } else {
            currentSelection.add(classId)
        }
        _selectedClassIds.value = currentSelection
    }

    fun selectAll() {
        val allIds = allClasses.value.map { it.id }.toSet()
        _selectedClassIds.value = allIds
    }

    fun clearSelection() {
        _selectedClassIds.value = emptySet()
    }

    fun deleteSelectedClasses() {
        viewModelScope.launch {
            val selectedIds = _selectedClassIds.value
            selectedIds.forEach { classId ->
                classesRef.child(classId).removeValue()
            }
            toggleSelectionMode()
        }
    }

    fun addClass(madrasaClass: MadrasaClass) = viewModelScope.launch {
        val key = classesRef.push().key
        if (key != null) {
            val newClass = madrasaClass.copy(id = key)
            classesRef.child(key).setValue(newClass)
        }
    }

    fun updateClass(madrasaClass: MadrasaClass) = viewModelScope.launch {
        classesRef.child(madrasaClass.id).setValue(madrasaClass)
        stopEditing()
    }

    fun startEditing(madrasaClass: MadrasaClass?) {
        _editingClass.value = madrasaClass ?: MadrasaClass()
    }

    fun stopEditing() {
        _editingClass.value = null
    }
}
