package com.jamia.madinatulilm.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jamia.madinatulilm.ui.components.GlassyBottomBar
import com.jamia.madinatulilm.ui.components.NavigationItem
import com.jamia.madinatulilm.ui.components.ConnectionStatusIndicator
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
        NavigationItem("admin_dashboard/$user", Icons.Default.Home, "Home"),
        NavigationItem("students", Icons.Default.Group, "Students"),
        NavigationItem("teachers", Icons.Default.Person, "Teachers"),
        NavigationItem("approvals", Icons.Default.HowToReg, "Approvals"),
        NavigationItem("admin_leave_requests", Icons.AutoMirrored.Filled.EventNote, "Leaves")
    )

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(800)) + 
                slideInVertically(initialOffsetY = { 40 }) + 
                scaleIn(initialScale = 0.95f),
        exit = fadeOut(animationSpec = tween(500)) + scaleOut(targetScale = 1.05f)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text("Admin Panel", fontWeight = FontWeight.Black, fontSize = 24.sp)
                            Text(
                                text = "Welcome, ${userName.ifEmpty { "Administrator" }}",
                                style = MaterialTheme.typography.labelMedium,
                                color = ForestGreen.copy(alpha = 0.7f)
                            )
                        }
                    },
                    actions = {
                        ConnectionStatusIndicator()
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = StatusDisapproved)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = ForestGreen
                    )
                )
            },
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                GlassyBottomBar(
                    items = navItems,
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            },
            containerColor = SoftCream
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search system...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ForestGreen) },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { if (searchText.isNotBlank()) onSearch(searchText) }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = Color.White
                    )
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        MetricCard(
                            title = "Students",
                            value = studentCount.toString(),
                            icon = Icons.Default.Group,
                            gradient = Brush.verticalGradient(listOf(ForestGreen, ForestGreenLight)),
                            onClick = { navController.navigate("students") }
                        )
                    }
                    item {
                        MetricCard(
                            title = "Teachers",
                            value = teacherCount.toString(),
                            icon = Icons.Default.Person,
                            gradient = Brush.verticalGradient(listOf(DeepGold, LuxuryGold)),
                            onClick = { navController.navigate("teachers") }
                        )
                    }
                    item {
                        MetricCard(
                            title = "Classes",
                            value = classCount.toString(),
                            icon = Icons.Default.School,
                            gradient = Brush.verticalGradient(listOf(Color(0xFF455A64), Color(0xFF78909C))),
                            onClick = { navController.navigate("classes") }
                        )
                    }
                    item {
                        MetricCard(
                            title = "Daily Attendance",
                            value = "$presentStudentCount",
                            subValue = "/ $studentCount",
                            icon = Icons.Default.CheckCircle,
                            gradient = Brush.verticalGradient(listOf(StatusApproved, Color(0xFF81C784))),
                            onClick = onNavigateToAttendance
                        )
                    }
                    item {
                        MetricCard(
                            title = "Finance Hub",
                            value = "₨ Accounts",
                            icon = Icons.Default.AccountBalanceWallet,
                            gradient = Brush.verticalGradient(listOf(ForestGreen, DeepGold)),
                            onClick = { navController.navigate("finance_dashboard") }
                        )
                    }
                    item {
                        MetricCard(
                            title = "Library",
                            value = "Books",
                            icon = Icons.AutoMirrored.Filled.LibraryBooks,
                            gradient = Brush.verticalGradient(listOf(Color(0xFF5D4037), Color(0xFF8D6E63))),
                            onClick = { navController.navigate("finance_library") }
                        )
                    }
                    item {
                        MetricCard(
                            title = "User Approvals",
                            value = "Approvals",
                            icon = Icons.Default.HowToReg,
                            gradient = Brush.verticalGradient(listOf(Color(0xFF607D8B), Color(0xFF90A4AE))),
                            onClick = { navController.navigate("approvals") }
                        )
                    }
                    item {
                        MetricCard(
                            title = "Leave Requests",
                            value = "Leaves",
                            icon = Icons.AutoMirrored.Filled.EventNote,
                            gradient = Brush.verticalGradient(listOf(Color(0xFF795548), Color(0xFFA1887F))),
                            onClick = { navController.navigate("admin_leave_requests") }
                        )
                    }
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
            .height(130.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp)
        ) {
            // Background Icon - Perfectly positioned
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.15f),
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 20.dp)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top part: Icon and small indicator
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }

                // Bottom part: Value and Title
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 22.sp
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
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
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
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1.0f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ForestGreen.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(22.dp))
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = ForestGreen,
                fontSize = 14.sp
            )
        }
    }
}
