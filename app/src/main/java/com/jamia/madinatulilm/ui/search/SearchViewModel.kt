package com.jamia.madinatulilm.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jamia.madinatulilm.data.MadrasaClass
import com.jamia.madinatulilm.data.Student
import com.jamia.madinatulilm.data.Teacher
import kotlinx.coroutines.flow.*

data class SearchResults(
    val students: List<Student> = emptyList(),
    val teachers: List<Teacher> = emptyList(),
    val classes: List<MadrasaClass> = emptyList()
)

class SearchViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    private val _teachers = MutableStateFlow<List<Teacher>>(emptyList())
    private val _classes = MutableStateFlow<List<MadrasaClass>>(emptyList())

    private val database by lazy { Firebase.database }
    private val studentsRef by lazy { database.getReference("students") }
    private val teachersRef by lazy { database.getReference("teachers") }
    private val classesRef by lazy { database.getReference("classes") }

    init {
        studentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val studentList = snapshot.children.mapNotNull { it.getValue(Student::class.java) }
                    _students.value = studentList
                } catch (e: Exception) {
                    android.util.Log.e("SearchViewModel", "Student parsing failed", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        teachersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val teacherList = snapshot.children.mapNotNull { it.getValue(Teacher::class.java) }
                    _teachers.value = teacherList
                } catch (e: Exception) {
                    android.util.Log.e("SearchViewModel", "Teacher parsing failed", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        classesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val classList = snapshot.children.mapNotNull { it.getValue(MadrasaClass::class.java) }
                    _classes.value = classList
                } catch (e: Exception) {
                    android.util.Log.e("SearchViewModel", "Class parsing failed", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    val searchResults: StateFlow<SearchResults> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(SearchResults())
            } else {
                combine(
                    _students.map { students -> students.filter { it.doesMatchSearchQuery(query) } },
                    _teachers.map { teachers -> teachers.filter { it.doesMatchSearchQuery(query) } },
                    _classes.map { classes -> classes.filter { it.className.contains(query, ignoreCase = true) } }
                ) { filteredStudents, filteredTeachers, filteredClasses ->
                    SearchResults(students = filteredStudents, teachers = filteredTeachers, classes = filteredClasses)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResults())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

// Helper functions to keep search logic consistent
private fun Student.doesMatchSearchQuery(query: String): Boolean {
    val matchingCombinations = listOf(fullName, cnic, guardianName, contactNumber)
    return matchingCombinations.any { it.contains(query, ignoreCase = true) }
}

private fun Teacher.doesMatchSearchQuery(query: String): Boolean {
    val matchingCombinations = listOf(name, cnic, qualifications, contactInfo)
    return matchingCombinations.any { it.contains(query, ignoreCase = true) }
}
