package com.jamia.madinatulilm.data.finance

import androidx.room.*
import com.google.firebase.database.PropertyName

// --- INVENTORY (For Canteen & Maktab) ---
@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey val id: String = "", 
    val name: String = "",
    val category: String = "", 
    val price: Double = 0.0, // Unit price
    val costPrice: Double = 0.0,
    val unitCount: Int = 0, // Current loose units
    val boxCount: Int = 0,  // Current full boxes
    val unitsPerBox: Int = 1,
    val imageUrl: String? = null
) {
    // Helper to calculate total value based on unit price
    fun calculateTotalValue(): Double {
        val totalUnits = unitCount + (boxCount * unitsPerBox)
        return totalUnits * price
    }
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String = "",
    val type: String = "", // "CANTEEN_SALE", "MAKTAB_SALE", "COMMITTEE", "MONTHLY_DONATION"
    val sourceName: String = "", // e.g., 'Committee', 'Monthly Donation'
    val items: List<TransactionItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val profit: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val monthYear: String = "" 
)

data class TransactionItem(
    val itemId: String = "",
    val name: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0
)

// --- DONATION ---
@Entity(tableName = "donation_records")
data class DonationRecord(
    @PrimaryKey val id: String = "",
    val donorName: String = "",
    val studentId: String? = null,
    val teacherId: String? = null,
    val amount: Double = 0.0,
    val type: String = "FAMILY",
    @PropertyName("isFixed")
    val isFixed: Boolean = false,
    val monthYear: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// --- PAYROLL ---
@Entity(tableName = "payroll_records")
data class PayrollRecord(
    @PrimaryKey val id: String = "",
    val teacherId: String = "",
    val teacherName: String = "",
    val amount: Double = 0.0,
    val monthYear: String = "",
    @get:PropertyName("paid")
    @set:PropertyName("paid")
    var isPaid: Boolean = false,
    val paymentDate: Long? = null,
    val transactionId: String? = null
)

// --- LIBRARY ---
@Entity(tableName = "library_books")
data class LibraryBook(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val author: String = "",
    val category: String = "",
    val totalCopies: Int = 1,
    val availableCopies: Int = 1
)

@Entity(tableName = "lending_records")
data class LendingRecord(
    @PrimaryKey val id: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val borrowerId: String = "",
    val borrowerName: String = "",
    val borrowDate: Long = System.currentTimeMillis(),
    val dueDate: Long = 0,
    val returnDate: Long? = null,
    val status: String = "BORROWED"
)
