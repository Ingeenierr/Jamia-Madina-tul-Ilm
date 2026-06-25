package com.jamia.madinatulilm.ui.dashboard

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jamia.madinatulilm.data.AttendanceRecord
import com.jamia.madinatulilm.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

class AdminViewModel : ViewModel() {

    private val database by lazy { Firebase.database }
    private val usersRef by lazy { database.getReference("users") }
    private val studentsRef by lazy { database.getReference("students") }
    private val teachersRef by lazy { database.getReference("teachers") }
    private val classesRef by lazy { database.getReference("classes") }
    private val attendanceRef by lazy { database.getReference("attendance") }

    private val _studentCount = MutableStateFlow(0)
    val studentCount: StateFlow<Int> = _studentCount

    private val _teacherCount = MutableStateFlow(0)
    val teacherCount: StateFlow<Int> = _teacherCount

    private val _classCount = MutableStateFlow(0)
    val classCount: StateFlow<Int> = _classCount

    private val _presentStudentCount = MutableStateFlow(0)
    val presentStudentCount: StateFlow<Int> = _presentStudentCount

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName

    fun setUserId(uid: String) {
        usersRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                _userName.value = user?.name ?: ""
            }

            override fun onCancelled(error: DatabaseError) { 
                // Handle error
            }
        })
    }

    init {
        studentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _studentCount.value = snapshot.childrenCount.toInt()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        teachersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _teacherCount.value = snapshot.childrenCount.toInt()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        classesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _classCount.value = snapshot.childrenCount.toInt()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        attendanceRef.orderByChild("date").equalTo(today).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val presentCount = snapshot.children.mapNotNull { it.getValue(AttendanceRecord::class.java) }
                        .count { record -> record.attendance.values.any { it == "Present" } }
                    _presentStudentCount.value = presentCount
                } catch (e: Exception) {
                    android.util.Log.e("AdminViewModel", "Attendance data parsing failed", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
