package com.jamia.madinatulilm.data

data class Teacher(
    val id: String = "",
    val name: String = "",
    val qualifications: String = "",
    val contactInfo: String = "",
    val cnic: String = "",
    val address: String = "",
    val dob: String = "",
    val salary: Double = 0.0,
    val email: String = "",
    val classIds: List<String> = emptyList()
)
