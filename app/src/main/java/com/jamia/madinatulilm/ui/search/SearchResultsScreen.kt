package com.jamia.madinatulilm.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(navController: NavController, viewModel: SearchViewModel) {
    val searchResults by viewModel.searchResults.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Results") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (searchResults.students.isNotEmpty()) {
                item {
                    Text("Students", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                items(searchResults.students) { student ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate("students") }) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(student.fullName, fontWeight = FontWeight.Bold)
                            Text("CNIC: ${student.cnic}")
                        }
                    }
                }
            }

            if (searchResults.teachers.isNotEmpty()) {
                item {
                    Text("Teachers", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                items(searchResults.teachers) {
                    Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate("teachers") }) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(it.name, fontWeight = FontWeight.Bold)
                            Text("CNIC: ${it.cnic}")
                        }
                    }
                }
            }

            if (searchResults.classes.isNotEmpty()) {
                item {
                    Text("Classes", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                items(searchResults.classes) {
                    Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate("classes") }) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(it.className, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
