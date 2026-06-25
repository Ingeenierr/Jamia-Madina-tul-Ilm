package com.jamia.madinatulilm.data.finance

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

class FinanceRepository(private val dao: FinanceDao) {

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val db by lazy { Firebase.database.reference }
    private val inventoryRef by lazy { db.child("finance").child("inventory") }
    private val transactionsRef by lazy { db.child("finance").child("transactions") }
    private val donationsRef by lazy { db.child("finance").child("donations") }
    private val payrollRef by lazy { db.child("finance").child("payroll") }
    private val libraryRef by lazy { db.child("finance").child("library") }
    private val lendingRef by lazy { db.child("finance").child("lending") }

    // --- Library ---
    fun getBooks() = dao.getAllBooks().distinctUntilChanged()
    fun getLendingRecords() = dao.getAllLendingRecords().distinctUntilChanged()
    fun provideLendingRef() = lendingRef

    suspend fun saveBook(book: LibraryBook) {
        val key = if (book.id.isBlank()) libraryRef.push().key ?: "" else book.id
        val finalBook = book.copy(id = key)
        dao.insertBook(finalBook)
        libraryRef.child(key).setValue(finalBook).await()
    }

    suspend fun deleteBook(book: LibraryBook) {
        dao.deleteBook(book)
        libraryRef.child(book.id).removeValue().await()
    }

    suspend fun lendBook(record: LendingRecord) {
        val key = record.id.ifBlank { lendingRef.push().key ?: "" }
        val finalRecord = record.copy(id = key)
        dao.insertLendingRecord(finalRecord)
        lendingRef.child(key).setValue(finalRecord).await()
    }

    suspend fun updateLendingRecord(record: LendingRecord) {
        dao.updateLendingRecord(record)
        lendingRef.child(record.id).setValue(record).await()
    }

    // --- Inventory ---
    fun getInventory(category: String) =
        dao.getInventoryByCategory(category).distinctUntilChanged()

    suspend fun saveInventoryItem(item: InventoryItem) {
        val key = if (item.id.isBlank()) inventoryRef.push().key ?: "" else item.id
        val finalItem = item.copy(id = key)
        dao.insertInventoryItem(finalItem)
        inventoryRef.child(key).setValue(finalItem).await()
    }

    suspend fun deleteInventoryItem(item: InventoryItem) {
        dao.deleteInventoryItem(item)
        inventoryRef.child(item.id).removeValue().await()
    }

    // --- POS & Transactions ---
    fun getAllTransactions() = dao.getAllTransactions().distinctUntilChanged()

    suspend fun performSale(transaction: Transaction) {
        val key = transactionsRef.push().key ?: ""
        val finalTransaction = transaction.copy(id = key)

        // 1. Update Room
        dao.insertTransaction(finalTransaction)

        // 2. Update Stock in Room
        finalTransaction.items.forEach { saleItem ->
            val item = dao.getInventoryItemById(saleItem.itemId)
            if (item != null) {
                // Subtracting from unitCount for sales
                val updatedItem =
                    item.copy(unitCount = item.unitCount - saleItem.quantity)
                dao.updateStock(updatedItem)
                // Sync updated stock to Firebase
                inventoryRef.child(item.id).child("unitCount")
                    .setValue(updatedItem.unitCount).await()
            } else {
                // Check if it's a library book
                try {
                    val books = dao.getAllBooks().first()
                    val book = books.find { it.id == saleItem.itemId }
                    if (book != null) {
                        val updatedBook = book.copy(availableCopies = book.availableCopies - saleItem.quantity)
                        dao.insertBook(updatedBook)
                        libraryRef.child(book.id).setValue(updatedBook).await()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FinanceRepository", "Book update failed during sale", e)
                }
            }
        }

        // 3. Sync to Firebase
        transactionsRef.child(key).setValue(finalTransaction).await()
    }

    // --- Donations ---
    fun getAllDonations() = dao.getAllDonations().distinctUntilChanged()

    suspend fun addDonation(record: DonationRecord) {
        val key = record.id.ifBlank { donationsRef.push().key ?: "" }
        val finalRecord = record.copy(id = key)
        dao.insertDonation(finalRecord)
        donationsRef.child(key).setValue(finalRecord).await()
    }

    suspend fun deleteDonation(record: DonationRecord) {
        dao.deleteDonation(record)
        donationsRef.child(record.id).removeValue().await()
    }

    // --- Payroll ---
    fun getPayroll(month: String) = dao.getPayrollByMonth(month).distinctUntilChanged()

    suspend fun updatePayroll(record: PayrollRecord) {
        dao.insertPayroll(record)
        payrollRef.child(record.id).setValue(record).await()
    }

    suspend fun deletePayroll(record: PayrollRecord) {
        dao.deletePayroll(record)
        payrollRef.child(record.id).removeValue().await()
    }

    // Reporting
    fun getMonthlyBalance(month: String) = dao.getMonthlyProfit(month).distinctUntilChanged()
    fun getDonations(month: String) = dao.getMonthlyDonations(month).distinctUntilChanged()
    fun getPaidPayroll(month: String) = dao.getPaidPayroll(month).distinctUntilChanged()

    suspend fun clearMonthlyData(month: String) {
        dao.clearTransactionsForMonth(month)
        dao.clearDonationsForMonth(month)
        dao.clearPayrollForMonth(month)

        transactionsRef.orderByChild("monthYear").equalTo(month).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { it.ref.removeValue() }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        donationsRef.orderByChild("monthYear").equalTo(month).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { it.ref.removeValue() }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        payrollRef.orderByChild("monthYear").equalTo(month).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { it.ref.removeValue() }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Sync from Firebase (Real-time)
    fun startRealtimeSync() {
        // Sync Inventory
        inventoryRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                repositoryScope.launch {
                    try {
                        val items = snapshot.children.mapNotNull { child ->
                            try {
                                child.getValue(InventoryItem::class.java)
                            } catch (e: Exception) {
                                InventoryItem(
                                    id = child.key ?: "",
                                    name = child.child("name").value?.toString() ?: "",
                                    category = child.child("category").value?.toString() ?: "",
                                    price = (child.child("price").value as? Number)?.toDouble() ?: 0.0,
                                    costPrice = (child.child("costPrice").value as? Number)?.toDouble() ?: 0.0,
                                    unitCount = (child.child("unitCount").value as? Number)?.toInt() ?: 0,
                                    boxCount = (child.child("boxCount").value as? Number)?.toInt() ?: 0,
                                    unitsPerBox = (child.child("unitsPerBox").value as? Number)?.toInt() ?: 1
                                )
                            }
                        }
                        dao.insertInventoryItems(items)
                    } catch (e: Exception) {
                        android.util.Log.e("FinanceRepository", "Inventory sync failed", e)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Sync Donations
        donationsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                repositoryScope.launch {
                    try {
                        val records = snapshot.children.mapNotNull { child ->
                            try {
                                child.getValue(DonationRecord::class.java)
                            } catch (e: Exception) {
                                DonationRecord(
                                    id = child.key ?: "",
                                    donorName = child.child("donorName").value?.toString() ?: "",
                                    studentId = child.child("studentId").value?.toString(),
                                    teacherId = child.child("teacherId").value?.toString(),
                                    amount = (child.child("amount").value as? Number)?.toDouble() ?: 0.0,
                                    type = child.child("type").value?.toString() ?: "GENERAL",
                                    isFixed = (child.child("isFixed").value as? Boolean) ?: false,
                                    monthYear = child.child("monthYear").value?.toString() ?: "",
                                    timestamp = (child.child("timestamp").value as? Number)?.toLong() ?: System.currentTimeMillis()
                                )
                            }
                        }
                        dao.insertDonations(records)
                    } catch (e: Exception) {
                        android.util.Log.e("FinanceRepository", "Donations sync failed", e)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Sync Payroll
        payrollRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                repositoryScope.launch {
                    try {
                        val records = snapshot.children.mapNotNull { child ->
                            try {
                                child.getValue(PayrollRecord::class.java)
                            } catch (e: Exception) {
                                PayrollRecord(
                                    id = child.key ?: "",
                                    teacherId = child.child("teacherId").value?.toString() ?: "",
                                    teacherName = child.child("teacherName").value?.toString() ?: "",
                                    amount = (child.child("amount").value as? Number)?.toDouble() ?: 0.0,
                                    monthYear = child.child("monthYear").value?.toString() ?: "",
                                    isPaid = (child.child("isPaid").value as? Boolean) ?: (child.child("paid").value as? Boolean) ?: false,
                                    paymentDate = (child.child("paymentDate").value as? Number)?.toLong(),
                                    transactionId = child.child("transactionId").value?.toString()
                                )
                            }
                        }
                        dao.insertPayrolls(records)
                    } catch (e: Exception) {
                        android.util.Log.e("FinanceRepository", "Payroll sync failed", e)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
