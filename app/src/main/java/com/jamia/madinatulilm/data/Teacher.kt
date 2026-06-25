package com.jamia.madinatulilm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val qualifications: String = "",
    val contactInfo: String = "",
    val cnic: String = "",
    val address: String = "",
    val dob: String = "",
    val salary: Double = 0.0,
    val email: String = "",
    val classIds: List<String> = emptyList(),
    val isPaid: Boolean = false // Added for payroll tracking
)
