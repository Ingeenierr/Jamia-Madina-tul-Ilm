package com.jamia.madinatulilm.data

enum class UserStatus {
    PENDING,
    APPROVED,
    DISAPPROVED
}

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val role: UserRole = UserRole.TEACHER,
    val status: UserStatus = UserStatus.PENDING,
    val lastSeen: Long? = null,
    val online: Boolean = false,
    val typingIn: String? = null
)
