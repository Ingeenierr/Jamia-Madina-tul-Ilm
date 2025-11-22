package com.jamia.madinatulilm.data

data class MadrasaClass(
    val id: String = "",
    val className: String = "",
    val teacherId: String = "",
    val studentIds: List<String> = emptyList()
)
