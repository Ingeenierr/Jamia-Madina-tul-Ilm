package com.jamia.madinatulilm.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jamia.madinatulilm.data.finance.LibraryBook
import com.jamia.madinatulilm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: FinanceViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val books by viewModel.libraryBooks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showIssueDialog by remember { mutableStateOf<LibraryBook?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library Management", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ForestGreen,
                    titleContentColor = SoftCream,
                    navigationIconContentColor = SoftCream,
                    actionIconContentColor = SoftCream
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ForestGreen,
                contentColor = SoftCream
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Book")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(SoftCream, Color.White)))
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(books, key = { it.id }) { book ->
                    BookItem(
                        book = book,
                        onDelete = { viewModel.deleteBook(book) },
                        onIssue = { showIssueDialog = book }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddBookDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, author, copies ->
                viewModel.addBook(title, author, "GENERAL", copies)
                showAddDialog = false
            }
        )
    }

    showIssueDialog?.let { book ->
        IssueBookDialog(
            book = book,
            onDismiss = { showIssueDialog = null },
            onConfirm = { borrowerId, borrowerName ->
                viewModel.lendBook(book, borrowerId, borrowerName)
                showIssueDialog = null
            }
        )
    }
}

@Composable
fun BookItem(book: LibraryBook, onDelete: () -> Unit, onIssue: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(ForestGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LibraryBooks, contentDescription = null, tint = ForestGreen)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(book.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(book.author, color = Color.Gray, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${book.availableCopies}/${book.totalCopies}", fontWeight = FontWeight.Black, color = ForestGreen)
                    Text("Available", fontSize = 10.sp, color = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onIssue,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    enabled = book.availableCopies > 0
                ) {
                    Text("Issue Book", fontSize = 12.sp)
                }
                
                IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f))
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Book?") },
            text = { Text("Remove '${book.title}' from library?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AddBookDialog(onDismiss: () -> Unit, onConfirm: (String, String, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var copies by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Book", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Book Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Author") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = copies, onValueChange = { copies = it }, label = { Text("Total Copies") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title, author, copies.toIntOrNull() ?: 1) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun IssueBookDialog(book: LibraryBook, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var borrowerName by remember { mutableStateOf("") }
    var borrowerId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Issue: ${book.title}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = borrowerName, onValueChange = { borrowerName = it }, label = { Text("Borrower Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = borrowerId, onValueChange = { borrowerId = it }, label = { Text("Student/Teacher ID") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(borrowerId, borrowerName) }, enabled = borrowerName.isNotBlank()) {
                Text("Issue Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
