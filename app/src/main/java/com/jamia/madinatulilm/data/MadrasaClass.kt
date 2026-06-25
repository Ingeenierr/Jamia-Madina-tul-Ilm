package com.jamia.madinatulilm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classes")
data class MadrasaClass(
    @PrimaryKey val id: String = "",
    val className: String = "",
    val level: String = "",
    val room: String = "",
    val teacherId: String = "",
    val studentIds: List<String> = emptyList()
)
