package com.jamia.madinatulilm.ui.theme

import androidx.compose.ui.graphics.Color

// Premium Madrassa Palette
val ForestGreen = Color(0xFF013220)
val ForestGreenLight = Color(0xFF1B5E20)
val SoftCream = Color(0xFFFCF5E5)
val LuxuryGold = Color(0xFFD4AF37)
val DeepGold = Color(0xFF996515)

// Status Colors
val StatusApproved = Color(0xFF2E7D32)
val StatusPending = Color(0xFFFBC02D)
val StatusDisapproved = Color(0xFFD32F2F)

// Attendance Colors
val AttendancePresent = StatusApproved
val AttendanceAbsent = StatusDisapproved
val AttendanceLeave = StatusPending

// Legacy compatibility (mapping to new palette where possible)
val PrimaryGreen = ForestGreen
val PrimaryGreenLight = ForestGreenLight
val PrimaryGreenDark = ForestGreen
val AmberAccent = LuxuryGold
val AmberAccentLight = SoftCream
val AmberAccentDark = DeepGold
val BackgroundLight = SoftCream
val SurfaceLight = Color.White
val TextPrimary = Color(0xFF1A1C19)
val TextSecondary = Color(0xFF434944)
