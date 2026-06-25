package com.jamia.madinatulilm.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

@IgnoreExtraProperties
@Entity(tableName = "students")
data class Student(
    @PrimaryKey val id: String = "",
    val fullName: String = "",
    val age: Int = 0,
    val guardianName: String = "",
    val contactNumber: String = "",
    val cnic: String = "",
    val address: String = "",
    val dob: String = "",
    val classId: String = "",
    @get:PropertyName("active")
    @set:PropertyName("active")
    var isActive: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
