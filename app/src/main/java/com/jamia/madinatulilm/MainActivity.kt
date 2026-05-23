package com.jamia.madinatulilm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.app.NotificationCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jamia.madinatulilm.data.LeaveRequest
import com.jamia.madinatulilm.data.LeaveStatus
import com.jamia.madinatulilm.data.UserRole
import com.jamia.madinatulilm.ui.admin.ApprovalScreen
import com.jamia.madinatulilm.ui.admin.LeaveRequestsAdminScreen
import com.jamia.madinatulilm.ui.attendance.AttendanceScreen
import com.jamia.madinatulilm.ui.attendance.AttendanceViewModel
import com.jamia.madinatulilm.ui.classes.ClassScreen
import com.jamia.madinatulilm.ui.classes.ClassViewModel
import com.jamia.madinatulilm.ui.dashboard.AdminDashboardScreen
import com.jamia.madinatulilm.ui.dashboard.AdminViewModel
import com.jamia.madinatulilm.ui.dashboard.TeacherDashboardScreen
import com.jamia.madinatulilm.ui.donations.DonationDashboardScreen
import com.jamia.madinatulilm.ui.donations.DonationDashboardViewModel
import com.jamia.madinatulilm.ui.donations.DonationScreen
import com.jamia.madinatulilm.ui.donations.DonationViewModel
import com.jamia.madinatulilm.ui.login.LoginScreen
import com.jamia.madinatulilm.ui.login.LoginViewModel
import com.jamia.madinatulilm.ui.login.SignUpScreen
import com.jamia.madinatulilm.ui.more.MoreScreen
import com.jamia.madinatulilm.ui.search.SearchResultsScreen
import com.jamia.madinatulilm.ui.search.SearchViewModel
import com.jamia.madinatulilm.ui.students.StudentProfileScreen
import com.jamia.madinatulilm.ui.students.StudentScreen
import com.jamia.madinatulilm.ui.students.StudentViewModel
import com.jamia.madinatulilm.ui.teachers.LeaveRequestScreen
import com.jamia.madinatulilm.ui.teachers.TeacherScreen
import com.jamia.madinatulilm.ui.teachers.TeacherViewModel
import com.jamia.madinatulilm.ui.theme.JamiaMadinatulIlmTheme

class MainActivity : ComponentActivity() {
    
    private var leaveRequestEventListener: ChildEventListener? = null
    private val leaveRequestsRef = Firebase.database.getReference("leave_requests")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            JamiaMadinatulIlmTheme(darkTheme = false) {
                AppNavigation(
                    onAdminLogin = { startLeaveRequestListeners() },
                    onLogout = { stopLeaveRequestListeners() }
                )
            }
        }
    }

    private fun startLeaveRequestListeners() {
        if (leaveRequestEventListener == null) {
            leaveRequestEventListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val request = snapshot.getValue(LeaveRequest::class.java)
                    if (request != null && request.status == LeaveStatus.PENDING) {
                        val timeDiff = System.currentTimeMillis() - request.timestamp
                        if (timeDiff < 60000) { 
                             sendNotification("New Leave Request", "${request.teacherName} has requested leave.")
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            }
            leaveRequestsRef.addChildEventListener(leaveRequestEventListener!!)
        }
    }

    private fun stopLeaveRequestListeners() {
        leaveRequestEventListener?.let {
            leaveRequestsRef.removeEventListener(it)
            leaveRequestEventListener = null
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = "admin_notifications"
        val defaultSoundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Admin Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}

@Composable
fun AppNavigation(onAdminLogin: () -> Unit, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val searchViewModel: SearchViewModel = viewModel()
    val loginViewModel: LoginViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(viewModel = loginViewModel, onLoginSuccess = { uid, role ->
                val route = if (role == UserRole.ADMIN) {
                    onAdminLogin()
                    "admin_dashboard/$uid"
                } else {
                    "teacher_dashboard/$uid"
                }
                navController.navigate(route) {
                    popUpTo("login") { inclusive = true }
                }
            }, onNavigateToSignUp = { navController.navigate("signup") })
        }
        composable("signup") {
            SignUpScreen(viewModel = loginViewModel, onSignUpSuccess = {
                navController.navigate("login") {
                    popUpTo("signup") { inclusive = true }
                }
            }, onNavigateToLogin = { navController.navigate("login") })
        }
        composable("admin_dashboard/{uid}", arguments = listOf(navArgument("uid") { type = NavType.StringType })) {
            val uid = it.arguments?.getString("uid") ?: ""
            val adminViewModel: AdminViewModel = viewModel()
            AdminDashboardScreen(navController = navController, viewModel = adminViewModel, user = uid, onSearch = {
                navController.navigate("search_results/$it")
            }, onLogout = {
                onLogout()
                navController.navigate("login") {
                    popUpTo("admin_dashboard/{uid}") { inclusive = true }
                }
            }, onNavigateToAttendance = {
                navController.navigate("attendance")
            })
        }
        composable("teacher_dashboard/{uid}", arguments = listOf(navArgument("uid") { type = NavType.StringType })) {
            val uid = it.arguments?.getString("uid") ?: ""
            TeacherDashboardScreen(navController = navController, user = uid, onLogout = {
                onLogout()
                navController.navigate("login") {
                    popUpTo("teacher_dashboard/{uid}") { inclusive = true }
                }
            })
        }
        composable("students") {
            val studentViewModel: StudentViewModel = viewModel()
            StudentScreen(
                viewModel = studentViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDonations = { studentId -> navController.navigate("donations/$studentId") }
            )
        }
        composable("teachers") {
            val teacherViewModel: TeacherViewModel = viewModel()
            TeacherScreen(viewModel = teacherViewModel, onNavigateBack = { navController.popBackStack() })
        }
        composable("classes") {
            val classViewModel: ClassViewModel = viewModel()
            ClassScreen(viewModel = classViewModel, onNavigateBack = { navController.popBackStack() })
        }
        composable("attendance?classId={classId}", arguments = listOf(navArgument("classId") { nullable = true })) {
            val classId = it.arguments?.getString("classId")
            val attendanceViewModel: AttendanceViewModel = viewModel()
            AttendanceScreen(navController = navController, viewModel = attendanceViewModel, onNavigateBack = { navController.popBackStack() }, classId = classId)
        }
        composable("more") {
            MoreScreen(
                onNavigateToAttendance = { navController.navigate("attendance") },
                onNavigateToDonations = { navController.navigate("donation_dashboard") },
                onNavigateBack = { navController.popBackStack() })
        }
        composable("approvals") {
            ApprovalScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(
            "donations/{studentId}",
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) {
            val studentId = it.arguments?.getString("studentId") ?: ""
            val donationViewModel: DonationViewModel = viewModel()
            DonationScreen(viewModel = donationViewModel, studentId = studentId, onNavigateBack = { navController.popBackStack() })
        }
        composable("donation_dashboard") {
            val donationDashboardViewModel: DonationDashboardViewModel = viewModel()
            val studentViewModel: StudentViewModel = viewModel()
            val students by studentViewModel.allStudents.collectAsState()
            DonationDashboardScreen(viewModel = donationDashboardViewModel, onNavigateBack = { navController.popBackStack() }, students = students)
        }
        composable("search_results/{query}", arguments = listOf(navArgument("query") { type = NavType.StringType })) {
            val query = it.arguments?.getString("query") ?: ""
            searchViewModel.setSearchQuery(query)
            SearchResultsScreen(navController, searchViewModel)
        }
        composable(
            "student_profile/{studentId}",
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) {
            val studentId = it.arguments?.getString("studentId") ?: ""
            StudentProfileScreen(studentId = studentId, onNavigateBack = { navController.popBackStack() })
        }
        composable(
            "leave_request/{teacherId}",
            arguments = listOf(navArgument("teacherId") { type = NavType.StringType })
        ) {
            val teacherId = it.arguments?.getString("teacherId") ?: ""
            LeaveRequestScreen(teacherId = teacherId, onNavigateBack = { navController.popBackStack() })
        }
        composable("admin_leave_requests") {
            LeaveRequestsAdminScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
