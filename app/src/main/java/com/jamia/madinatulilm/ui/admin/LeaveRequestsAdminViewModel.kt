package com.jamia.madinatulilm.ui.admin

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jamia.madinatulilm.data.LeaveRequest
import com.jamia.madinatulilm.data.LeaveStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LeaveRequestsAdminViewModel : ViewModel() {

    private val leaveRequestsRef = Firebase.database.getReference("leave_requests")

    private val _leaveRequests = MutableStateFlow<List<LeaveRequest>>(emptyList())
    val leaveRequests: StateFlow<List<LeaveRequest>> = _leaveRequests

    init {
        leaveRequestsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _leaveRequests.value = snapshot.children.mapNotNull { it.getValue(LeaveRequest::class.java) }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun approveRequest(requestId: String) {
        updateRequestStatus(requestId, LeaveStatus.APPROVED)
    }

    fun rejectRequest(requestId: String) {
        updateRequestStatus(requestId, LeaveStatus.REJECTED)
    }

    private fun updateRequestStatus(requestId: String, status: LeaveStatus) {
        leaveRequestsRef.child(requestId).child("status").setValue(status)
    }
}
