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

    private val database by lazy { Firebase.database }
    private val classesRef by lazy { database.getReference("classes") }
    private val studentsRef by lazy { database.getReference("students") }
    private val teachersRef by lazy { database.getReference("teachers") }

    private val _allClasses = MutableStateFlow<List<MadrasaClass>>(emptyList())
    val allClasses: StateFlow<List<MadrasaClass>> = _allClasses

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode = _isSelectionMode.asStateFlow()

    private val _selectedClassIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedClassIds = _selectedClassIds.asStateFlow()

    private val _editingClass = MutableStateFlow<MadrasaClass?>(null)
    val editingClass = _editingClass.asStateFlow()

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _allStudents = MutableStateFlow<List<Student>>(emptyList())
    val allStudents = _allStudents.asStateFlow()

    private val _allTeachers = MutableStateFlow<List<Teacher>>(emptyList())
    val allTeachers = _allTeachers.asStateFlow()

    init {
        classesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    _allClasses.value = snapshot.children.mapNotNull { it.getValue(MadrasaClass::class.java) }
                } catch (e: Exception) {
                    android.util.Log.e("ClassViewModel", "Class parsing failed", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        studentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    _allStudents.value = snapshot.children.mapNotNull { it.getValue(Student::class.java) }
                } catch (e: Exception) {
                    android.util.Log.e("ClassViewModel", "Student parsing failed", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        teachersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    _allTeachers.value = snapshot.children.mapNotNull { it.getValue(Teacher::class.java) }
                } catch (e: Exception) {
                    android.util.Log.e("ClassViewModel", "Teacher parsing failed", e)
                }
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
            
            // Sync with assigned teacher
            if (newClass.teacherId.isNotBlank()) {
                val teacher = allTeachers.value.find { it.id == newClass.teacherId }
                teacher?.let {
                    val updatedClassIds = it.classIds.toMutableList()
                    if (!updatedClassIds.contains(key)) {
                        updatedClassIds.add(key)
                        teachersRef.child(it.id).child("classIds").setValue(updatedClassIds)
                    }
                }
            }
            
            // Sync with enrolled students
            newClass.studentIds.forEach { studentId ->
                studentsRef.child(studentId).child("classId").setValue(key)
            }
            _uiEvent.emit("Class '${newClass.className}' created successfully!")
        }
    }

    fun updateClass(madrasaClass: MadrasaClass) = viewModelScope.launch {
        val oldClass = allClasses.value.find { it.id == madrasaClass.id }
        
        // 1. Update the class itself
        classesRef.child(madrasaClass.id).setValue(madrasaClass)

        // 2. Handle Teacher change logic (Sync Teacher.classIds)
        if (oldClass?.teacherId != madrasaClass.teacherId) {
            // Remove from old teacher
            oldClass?.teacherId?.let { oldId ->
                if (oldId.isNotBlank()) {
                    val oldTeacher = allTeachers.value.find { it.id == oldId }
                    val updatedIds = oldTeacher?.classIds?.filter { it != madrasaClass.id }
                    teachersRef.child(oldId).child("classIds").setValue(updatedIds)
                }
            }
            // Add to new teacher
            if (madrasaClass.teacherId.isNotBlank()) {
                val newTeacher = allTeachers.value.find { it.id == madrasaClass.teacherId }
                val updatedIds = (newTeacher?.classIds ?: emptyList()).toMutableList()
                if (!updatedIds.contains(madrasaClass.id)) {
                    updatedIds.add(madrasaClass.id)
                    teachersRef.child(madrasaClass.teacherId).child("classIds").setValue(updatedIds)
                }
            }
        }

        // 3. Handle Student Enrollment change (Sync Student.classId)
        val removedStudents = (oldClass?.studentIds ?: emptyList()).filter { !madrasaClass.studentIds.contains(it) }
        val addedStudents = madrasaClass.studentIds.filter { !(oldClass?.studentIds ?: emptyList()).contains(it) }

        removedStudents.forEach { studentsRef.child(it).child("classId").setValue("") }
        addedStudents.forEach { studentsRef.child(it).child("classId").setValue(madrasaClass.id) }

        _uiEvent.emit("Class '${madrasaClass.className}' updated successfully!")
        stopEditing()
    }

    fun startEditing(madrasaClass: MadrasaClass?) {
        _editingClass.value = madrasaClass ?: MadrasaClass()
    }

    fun stopEditing() {
        _editingClass.value = null
    }
}
