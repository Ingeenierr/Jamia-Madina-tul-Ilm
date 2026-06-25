package com.jamia.madinatulilm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jamia.madinatulilm.ui.theme.StatusApproved
import com.jamia.madinatulilm.ui.theme.StatusDisapproved

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
