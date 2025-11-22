package com.jamia.madinatulilm.ui.teachers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jamia.madinatulilm.data.LeaveRequest
import com.jamia.madinatulilm.data.LeaveStatus
import com.jamia.madinatulilm.data.Teacher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LeaveRequestViewModel : ViewModel() {

    private val database = Firebase.database
    private val leaveRequestsRef = database.getReference("leave_requests")
    private val teachersRef = database.getReference("teachers")

    private val _requestState = MutableStateFlow<RequestState>(RequestState.Idle)
    val requestState: StateFlow<RequestState> = _requestState

    private val _teacherName = MutableStateFlow("")

    fun fetchTeacherName(teacherId: String) {
        teachersRef.child(teacherId).get().addOnSuccessListener {
            val teacher = it.getValue(Teacher::class.java)
            _teacherName.value = teacher?.name ?: ""
        }
    }

    fun submitLeaveRequest(teacherId: String, reason: String, date: String) {
        _requestState.value = RequestState.Loading
        viewModelScope.launch {
            val key = leaveRequestsRef.push().key
            if (key != null) {
                val request = LeaveRequest(
                    id = key,
                    teacherId = teacherId,
                    teacherName = _teacherName.value,
                    reason = reason,
                    date = date,
                    status = LeaveStatus.PENDING
                )
                leaveRequestsRef.child(key).setValue(request)
                    .addOnSuccessListener {
                        _requestState.value = RequestState.Success
                    }
                    .addOnFailureListener {
                        _requestState.value = RequestState.Error(it.message ?: "Failed to submit request")
                    }
            } else {
                _requestState.value = RequestState.Error("Could not generate request ID")
            }
        }
    }

    fun resetState() {
        _requestState.value = RequestState.Idle
    }
}

sealed class RequestState {
    object Idle : RequestState()
    object Loading : RequestState()
    object Success : RequestState()
    data class Error(val message: String) : RequestState()
}
