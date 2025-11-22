package com.jamia.madinatulilm.data

data class TeacherLog(
    val id: String = "",
    val teacherId: String = "",
    val checkInTime: Long = 0,
    val checkOutTime: Long? = null
)
