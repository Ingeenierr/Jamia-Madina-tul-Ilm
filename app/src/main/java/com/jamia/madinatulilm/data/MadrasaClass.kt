package com.jamia.madinatulilm.data

data class MadrasaClass(
    val id: String = "",
    val className: String = "",
    val level: String = "",
    val room: String = "",
    val teacherId: String = "",
    val studentIds: List<String> = emptyList()
)
