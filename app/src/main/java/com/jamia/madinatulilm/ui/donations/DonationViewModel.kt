package com.jamia.madinatulilm.ui.donations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jamia.madinatulilm.data.Donation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DonationViewModel : ViewModel() {

    private val database by lazy { Firebase.database }
    private val donationsRef by lazy { database.getReference("donations") }

    fun getDonationsForStudent(studentId: String): Flow<List<Donation>> {
        val flow = MutableStateFlow<List<Donation>>(emptyList())
        donationsRef.orderByChild("studentId").equalTo(studentId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val donations = snapshot.children.mapNotNull { it.getValue(Donation::class.java) }
                    flow.value = donations
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        return flow
    }

    fun addDonation(donation: Donation) {
        viewModelScope.launch {
            val key = donationsRef.push().key
            if (key != null) {
                val newDonation = donation.copy(id = key)
                donationsRef.child(key).setValue(newDonation)
            }
        }
    }
}
