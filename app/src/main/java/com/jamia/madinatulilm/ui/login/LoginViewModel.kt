package com.jamia.madinatulilm.ui.login

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jamia.madinatulilm.data.Teacher
import com.jamia.madinatulilm.data.User
import com.jamia.madinatulilm.data.UserRole
import com.jamia.madinatulilm.data.UserStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val auth = Firebase.auth
    private val usersRef = Firebase.database.getReference("users")
    private val teachersRef = Firebase.database.getReference("teachers")

    fun login(email: String, pass: String) {
        _loginState.value = LoginState.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    firebaseUser?.uid?.let { uid ->
                        usersRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val user = snapshot.getValue(User::class.java)
                                if (user != null) {
                                    if (user.status == UserStatus.APPROVED) {
                                        _loginState.value = LoginState.Success(uid, user.role)
                                    } else {
                                        _loginState.value = LoginState.Error("Account not approved.")
                                    }
                                } else {
                                    _loginState.value = LoginState.Error("User data not found.")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                _loginState.value = LoginState.Error("Failed to get user data.")
                            }
                        })
                    }
                } else {
                    _loginState.value = LoginState.Error(task.exception?.message ?: "Invalid credentials")
                }
            }
    }

    fun signUp(name: String, email: String, pass: String, role: UserRole = UserRole.TEACHER) {
        _loginState.value = LoginState.Loading
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    firebaseUser?.uid?.let { uid ->
                        val user = User(id = uid, name = name, email = email, role = role, status = UserStatus.PENDING)
                        usersRef.child(uid).setValue(user)

                        if (role == UserRole.TEACHER) {
                            val teacher = Teacher(id = uid, name = name, email = email)
                            teachersRef.child(uid).setValue(teacher)
                        }

                        // Don't auto-login, let them know they need approval
                        _loginState.value = LoginState.Error("Sign-up successful. Please wait for admin approval.")
                    }
                } else {
                    _loginState.value = LoginState.Error(task.exception?.message ?: "Sign-up failed")
                }
            }
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val uid: String, val role: UserRole) : LoginState()
    data class Error(val message: String) : LoginState()
}
