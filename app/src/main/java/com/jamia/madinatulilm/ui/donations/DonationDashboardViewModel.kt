package com.jamia.madinatulilm.ui.donations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jamia.madinatulilm.data.Donation
import com.jamia.madinatulilm.data.Student
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DonationListItemData(
    val donation: Donation,
    val student: Student?
)

class DonationDashboardViewModel : ViewModel() {

    private val _allDonations = MutableStateFlow<List<DonationListItemData>>(emptyList())
    val allDonations: StateFlow<List<DonationListItemData>> = _allDonations

    private val database = Firebase.database
    private val donationsRef = database.getReference("donations")

    init {
        donationsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val donationList = snapshot.children.mapNotNull { it.getValue(Donation::class.java) }
                val donationListItemDataList = donationList.map { donation ->
                    DonationListItemData(donation, null) // We'll handle fetching the student later if needed
                }
                _allDonations.value = donationListItemDataList
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    val totalDonations = allDonations.map {
        it.sumOf { item -> item.donation.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0.0)

}
