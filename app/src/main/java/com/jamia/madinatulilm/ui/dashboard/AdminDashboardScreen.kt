package com.jamia.madinatulilm.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jamia.madinatulilm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminViewModel,
    user: String,
    onSearch: (String) -> Unit,
    onLogout: () -> Unit,
    onNavigateToAttendance: () -> Unit
) {
    val studentCount by viewModel.studentCount.collectAsState()
    val teacherCount by viewModel.teacherCount.collectAsState()
    val classCount by viewModel.classCount.collectAsState()
    val presentStudentCount by viewModel.presentStudentCount.collectAsState()
    val userName by viewModel.userName.collectAsState()
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        viewModel.setUserId(user)
    }

    val navItems = listOf(
        BottomNavItem("admin_dashboard/$user", Icons.Default.Home, "Home"),
        BottomNavItem("students", Icons.Default.Group, "Students"),
        BottomNavItem("teachers", Icons.Default.Person, "Teachers"),
        BottomNavItem("classes", Icons.Default.School, "Classes"),
        BottomNavItem("approvals", Icons.Default.Checklist, "Approvals")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Admin Panel", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Welcome, ${userName.ifEmpty { "Administrator" }}",
                            style = MaterialTheme.typography.labelMedium,
                            color = PrimaryGreen.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    ConnectionStatusIndicator()
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = PrimaryGreen,
                    actionIconContentColor = PrimaryGreen
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                navItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryGreen,
                            selectedTextColor = PrimaryGreen,
                            indicatorColor = PrimaryGreen.copy(alpha = 0.1f)
                        ),
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search students, teachers...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryGreen) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        if (searchText.isNotBlank()) {
                            onSearch(searchText)
                        }
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    MetricCard(
                        title = "Students",
                        value = studentCount.toString(),
                        icon = Icons.Default.Group,
                        gradient = Brush.verticalGradient(listOf(PrimaryGreen, PrimaryGreenLight))
                    )
                }
                item {
                    MetricCard(
                        title = "Teachers",
                        value = teacherCount.toString(),
                        icon = Icons.Default.Person,
                        gradient = Brush.verticalGradient(listOf(AmberAccentDark, AmberAccent))
                    )
                }
                item {
                    MetricCard(
                        title = "Classes",
                        value = classCount.toString(),
                        icon = Icons.Default.School,
                        gradient = Brush.verticalGradient(listOf(Color(0xFF455A64), Color(0xFF78909C)))
                    )
                }
                item {
                    MetricCard(
                        title = "Attendance",
                        value = "$presentStudentCount",
                        subValue = "/ $studentCount",
                        icon = Icons.Default.CheckCircle,
                        gradient = Brush.verticalGradient(listOf(StatusApproved, Color(0xFF81C784))),
                        onClick = onNavigateToAttendance
                    )
                }
                item {
                    QuickActionCard(
                        title = "Leave Requests",
                        icon = Icons.Default.Notifications,
                        onClick = { navController.navigate("admin_leave_requests") }
                    )
                }
                item {
                    QuickActionCard(
                        title = "User Approvals",
                        icon = Icons.Default.HowToReg,
                        onClick = { navController.navigate("approvals") }
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subValue: String = "",
    icon: ImageVector,
    gradient: Brush,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (subValue.isNotEmpty()) {
                        Text(
                            text = subValue,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ConnectionStatusIndicator() {
    var isConnected by remember { mutableStateOf(false) }

    val connectedRef = Firebase.database.getReference(".info/connected")
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isConnected = snapshot.getValue(Boolean::class.java) ?: false
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        connectedRef.addValueEventListener(listener)
        onDispose {
            connectedRef.removeEventListener(listener)
        }
    }

    Surface(
        color = if (isConnected) StatusApproved.copy(alpha = 0.1f) else StatusDisapproved.copy(alpha = 0.1f),
        shape = CircleShape,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) StatusApproved else StatusDisapproved)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isConnected) "Online" else "Offline",
                style = MaterialTheme.typography.labelSmall,
                color = if (isConnected) StatusApproved else StatusDisapproved,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class BottomNavItem(val route: String, val icon: ImageVector, val label: String)
