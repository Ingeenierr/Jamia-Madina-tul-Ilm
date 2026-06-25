package com.jamia.madinatulilm.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Students ---
    @Query("SELECT * FROM students WHERE isActive = 1")
    fun getActiveStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE isActive = 0")
    fun getExpelledStudents(): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Update
    suspend fun updateStudent(student: Student)

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: String): Student?

    // --- Teachers ---
    @Query("SELECT * FROM teachers WHERE isPaid = 0")
    fun getUnpaidTeachers(): Flow<List<Teacher>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: Teacher)

    @Update
    suspend fun updateTeacher(teacher: Teacher)

    @Query("SELECT * FROM teachers WHERE id = :id")
    suspend fun getTeacherById(id: String): Teacher?

    // --- Classes ---
    @Query("SELECT * FROM classes")
    fun getAllClasses(): Flow<List<MadrasaClass>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(madrasaClass: MadrasaClass)

    @Update
    suspend fun updateClass(madrasaClass: MadrasaClass)
    
    @Query("SELECT * FROM classes WHERE id = :id")
    suspend fun getClassById(id: String): MadrasaClass?
}
