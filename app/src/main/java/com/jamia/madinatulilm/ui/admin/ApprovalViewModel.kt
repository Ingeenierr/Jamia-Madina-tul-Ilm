package com.jamia.madinatulilm.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jamia.madinatulilm.data.User
import com.jamia.madinatulilm.data.UserStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ApprovalViewModel : ViewModel() {

    private val usersRef = Firebase.database.getReference("users")

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode = _isSelectionMode.asStateFlow()

    private val _selectedUserIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedUserIds = _selectedUserIds.asStateFlow()

    init {
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _users.value = snapshot.children.mapNotNull { it.getValue(User::class.java) }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun toggleSelectionMode() {
        _isSelectionMode.value = !_isSelectionMode.value
        if (!_isSelectionMode.value) {
            _selectedUserIds.value = emptySet()
        }
    }

    fun toggleUserSelection(userId: String) {
        val currentSelection = _selectedUserIds.value.toMutableSet()
        if (currentSelection.contains(userId)) {
            currentSelection.remove(userId)
        } else {
            currentSelection.add(userId)
        }
        _selectedUserIds.value = currentSelection
    }

    fun approveUser(user: User) {
        updateUserStatus(user.id, UserStatus.APPROVED)
    }

    fun approveSelectedUsers() {
        viewModelScope.launch {
            _selectedUserIds.value.forEach { userId ->
                updateUserStatus(userId, UserStatus.APPROVED)
            }
            toggleSelectionMode()
        }
    }

    fun disapproveSelectedUsers() {
        viewModelScope.launch {
            _selectedUserIds.value.forEach { userId ->
                updateUserStatus(userId, UserStatus.DISAPPROVED)
            }
            toggleSelectionMode()
        }
    }

    fun deleteSelectedUsers() {
        viewModelScope.launch {
            _selectedUserIds.value.forEach { userId ->
                val user = _users.value.find { it.id == userId }
                // Only allow deleting PENDING or DISAPPROVED users
                if (user?.status == UserStatus.PENDING || user?.status == UserStatus.DISAPPROVED) {
                    usersRef.child(userId).removeValue()
                }
            }
            toggleSelectionMode()
        }
    }

    fun revokeUserAccess(user: User) {
        updateUserStatus(user.id, UserStatus.DISAPPROVED)
    }

    fun reinstateUserAccess(user: User) {
        updateUserStatus(user.id, UserStatus.APPROVED)
    }

    private fun updateUserStatus(userId: String, status: UserStatus) {
        usersRef.child(userId).child("status").setValue(status)
    }
}
