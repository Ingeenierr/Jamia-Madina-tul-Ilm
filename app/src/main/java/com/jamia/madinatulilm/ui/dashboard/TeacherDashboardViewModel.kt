package com.jamia.madinatulilm.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jamia.madinatulilm.data.MadrasaClass
import com.jamia.madinatulilm.data.Teacher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TeacherDashboardViewModel : ViewModel() {

    private val database by lazy { Firebase.database }
    private val teachersRef by lazy { database.getReference("teachers") }
    private val classesRef by lazy { database.getReference("classes") }

    private val _teacher = MutableStateFlow<Teacher?>(null)
    val teacher: StateFlow<Teacher?> = _teacher

    private val _assignedClasses = MutableStateFlow<List<MadrasaClass>>(emptyList())
    val assignedClasses: StateFlow<List<MadrasaClass>> = _assignedClasses

    val totalStudents = _assignedClasses.map { classes ->
        classes.sumOf { it.studentIds.size }
    }

    fun setTeacherId(uid: String) {
        viewModelScope.launch {
            teachersRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val foundTeacher = snapshot.getValue(Teacher::class.java)
                        _teacher.value = foundTeacher

                        if (foundTeacher != null) {
                            classesRef.orderByChild("teacherId").equalTo(foundTeacher.id)
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        try {
                                            _assignedClasses.value = snapshot.children.mapNotNull { it.getValue(MadrasaClass::class.java) }
                                        } catch (e: Exception) {
                                            android.util.Log.e("TeacherDashboardVM", "Class parsing failed", e)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("TeacherDashboardVM", "Teacher parsing failed", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle teacher fetching error
                }
            })
        }
    }
}
