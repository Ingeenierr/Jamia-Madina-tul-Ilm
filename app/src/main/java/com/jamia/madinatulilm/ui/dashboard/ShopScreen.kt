package com.jamia.madinatulilm.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jamia.madinatulilm.data.finance.InventoryItem
import com.jamia.madinatulilm.ui.theme.ForestGreen
import com.jamia.madinatulilm.ui.theme.LuxuryGold
import com.jamia.madinatulilm.ui.theme.SoftCream
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    category: String, // "CANTEEN" or "MAKTAB"
    viewModel: FinanceViewModel,
    onNavigateToHistory: (() -> Unit)? = null,
    onNavigateBack: () -> Unit
) {
    val inventory by viewModel.getInventory(category).collectAsState()
    val libraryBooks by viewModel.libraryBooks.collectAsState()
    val cart by viewModel.cart.collectAsState()
    var showCart by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val filteredInventory = remember(inventory, libraryBooks, searchText) {
        val baseList = inventory.toMutableList()
        if (category == "MAKTAB") {
            libraryBooks.forEach { book ->
                if (baseList.none { it.name == book.title }) {
                    baseList.add(InventoryItem(
                        id = book.id,
                        name = book.title,
                        category = "MAKTAB",
                        price = 0.0,
                        unitCount = book.availableCopies,
                        boxCount = 0,
                        unitsPerBox = 1
                    ))
                }
            }
        }
        baseList.filter { it.name.contains(searchText, ignoreCase = true) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("$category Shop", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.AddBusiness, contentDescription = "Add Inventory")
                    }
                    if (category == "MAKTAB") {
                        IconButton(onClick = onNavigateToHistory ?: {}) {
                            Icon(Icons.Default.History, contentDescription = "History")
                        }
                    }
                    BadgedBox(
                        badge = {
                            if (cart.isNotEmpty()) {
                                Badge { Text(cart.size.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = { showCart = true }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ForestGreen,
                    titleContentColor = LuxuryGold,
                    navigationIconContentColor = SoftCream,
                    actionIconContentColor = SoftCream
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // Search Bar
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search products...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Inventory Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredInventory, key = { it.id }) { item ->
                    InventoryCard(
                        item = item,
                        onDelete = { viewModel.deleteInventoryItem(item) },
                        onClick = { viewModel.addToCart(item) }
                    )
                }
            }
        }
    }

    if (showCart) {
        ModalBottomSheet(onDismissRequest = { showCart = false }) {
            CartContent(cart, onRemove = viewModel::removeFromCart) {
                viewModel.checkout(category)
                showCart = false
                scope.launch {
                    snackbarHostState.showSnackbar("Sale completed successfully")
                }
            }
        }
    }

    if (showAddDialog) {
        AddInventoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, price, costPrice, units, boxes, perBox ->
                viewModel.addInventoryItem(category, name, price, costPrice, units, boxes, perBox)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddInventoryDialog(onDismiss: () -> Unit, onConfirm: (String, Double, Double, Int, Int, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var unitCount by remember { mutableStateOf("") }
    var boxCount by remember { mutableStateOf("") }
    var unitsPerBox by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Inventory", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Unit Sale Price") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = costPrice, onValueChange = { costPrice = it }, label = { Text("Unit Cost Price") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = unitCount, onValueChange = { unitCount = it }, label = { Text("Units") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = boxCount, onValueChange = { boxCount = it }, label = { Text("Boxes") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = unitsPerBox, onValueChange = { unitsPerBox = it }, label = { Text("Units Per Box") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { 
                onConfirm(
                    name, 
                    price.toDoubleOrNull() ?: 0.0, 
                    costPrice.toDoubleOrNull() ?: 0.0, 
                    unitCount.toIntOrNull() ?: 0,
                    boxCount.toIntOrNull() ?: 0,
                    unitsPerBox.toIntOrNull() ?: 1
                ) 
            }) { Text("Add Item") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun InventoryCard(item: InventoryItem, onDelete: () -> Unit, onClick: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp)).background(SoftCream),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Inventory2, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(40.dp))
                
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(item.name, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("₨ ${item.price}", color = ForestGreen, fontWeight = FontWeight.ExtraBold)
            val totalUnits = item.unitCount + (item.boxCount * item.unitsPerBox)
            Text("Stock: $totalUnits units", fontSize = 11.sp, color = Color.Gray)
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Product?") },
            text = { Text("Are you sure you want to remove '${item.name}' from inventory?") },
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
fun CartContent(
    items: List<com.jamia.madinatulilm.data.finance.TransactionItem>,
    onRemove: (String) -> Unit,
    onCheckout: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp).fillMaxHeight(0.6f)) {
        Text("Your Cart", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(items) { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(item.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Text("${item.quantity} x ₨ ${item.price}", color = Color.Gray)
                    IconButton(onClick = { onRemove(item.itemId) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    }
                }
            }
        }
        
        val total = items.sumOf { it.price * it.quantity }
        HorizontalDivider()
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Amount", fontWeight = FontWeight.Bold)
            Text("₨ $total", fontWeight = FontWeight.Black, fontSize = 20.sp, color = ForestGreen)
        }
        
        Button(
            onClick = onCheckout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
        ) {
            Text("Complete Sale", color = SoftCream, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
