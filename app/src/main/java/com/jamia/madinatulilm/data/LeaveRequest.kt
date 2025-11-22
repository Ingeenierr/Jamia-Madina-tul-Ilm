package com.jamia.madinatulilm.data

enum class LeaveStatus {
    PENDING,
    APPROVED,
    REJECTED
}

data class LeaveRequest(
    val id: String = "",
    val teacherId: String = "",
    val teacherName: String = "",
    val reason: String = "",
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: LeaveStatus = LeaveStatus.PENDING
)
