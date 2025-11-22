package com.jamia.madinatulilm.data

data class AttendanceRecord(
    val id: String = "",
    val studentId: String = "",
    val date: String = "",
    val attendance: Map<String, String> = emptyMap() // Map of classId to status
)
