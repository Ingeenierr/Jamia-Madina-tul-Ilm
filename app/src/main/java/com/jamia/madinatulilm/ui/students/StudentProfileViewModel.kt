package com.jamia.madinatulilm.ui.students

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jamia.madinatulilm.data.Student
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StudentProfileViewModel : ViewModel() {

    private val studentsRef by lazy { Firebase.database.getReference("students") }

    private val _student = MutableStateFlow<Student?>(null)
    val student: StateFlow<Student?> = _student

    fun setStudentId(studentId: String) {
        studentsRef.child(studentId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _student.value = snapshot.getValue(Student::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}
