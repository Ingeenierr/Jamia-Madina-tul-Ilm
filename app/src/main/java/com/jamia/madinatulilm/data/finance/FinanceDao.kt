package com.jamia.madinatulilm.data.finance

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    // --- Dashboard & Reporting ---
    @Query("SELECT SUM(profit) FROM transactions WHERE monthYear = :currentMonth")
    fun getMonthlyProfit(currentMonth: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM donation_records WHERE monthYear = :currentMonth")
    fun getMonthlyDonations(currentMonth: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM payroll_records WHERE monthYear = :currentMonth AND isPaid = 1")
    fun getPaidPayroll(currentMonth: String): Flow<Double?>

    // --- Inventory Management ---
    @Query("SELECT * FROM inventory_items WHERE category = :category")
    fun getInventoryByCategory(category: String): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getInventoryItemById(id: String): InventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItems(items: List<InventoryItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItem)

    @Update
    suspend fun updateStock(item: InventoryItem)

    @Delete
    suspend fun deleteInventoryItem(item: InventoryItem)

    // --- Transactions (POS) ---
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    // --- Donations ---
    @Query("SELECT * FROM donation_records ORDER BY timestamp DESC")
    fun getAllDonations(): Flow<List<DonationRecord>>

    @Query("SELECT * FROM donation_records WHERE studentId = :studentId OR teacherId = :teacherId")
    fun getDonationsForEntity(studentId: String?, teacherId: String?): Flow<List<DonationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonations(records: List<DonationRecord>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonation(record: DonationRecord)

    @Update
    suspend fun updateDonation(record: DonationRecord)

    @Delete
    suspend fun deleteDonation(record: DonationRecord)

    // --- Payroll ---
    @Query("SELECT * FROM payroll_records WHERE monthYear = :monthYear")
    fun getPayrollByMonth(monthYear: String): Flow<List<PayrollRecord>>

    @Query("SELECT * FROM payroll_records WHERE teacherId = :teacherId")
    fun getPayrollForTeacher(teacherId: String): Flow<List<PayrollRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayrolls(records: List<PayrollRecord>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayroll(record: PayrollRecord)

    @Update
    suspend fun updatePayrollStatus(record: PayrollRecord)

    @Delete
    suspend fun deletePayroll(record: PayrollRecord)

    // --- Library ---
    @Query("SELECT * FROM library_books")
    fun getAllBooks(): Flow<List<LibraryBook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: LibraryBook)

    @Update
    suspend fun updateBook(book: LibraryBook)

    @Delete
    suspend fun deleteBook(book: LibraryBook)

    @Query("SELECT * FROM lending_records ORDER BY borrowDate DESC")
    fun getAllLendingRecords(): Flow<List<LendingRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLendingRecord(record: LendingRecord)

    @Update
    suspend fun updateLendingRecord(record: LendingRecord)

    @Delete
    suspend fun deleteLendingRecord(record: LendingRecord)

    // --- Clearing Data ---
    @Query("DELETE FROM transactions WHERE monthYear = :monthYear")
    suspend fun clearTransactionsForMonth(monthYear: String)

    @Query("DELETE FROM donation_records WHERE monthYear = :monthYear")
    suspend fun clearDonationsForMonth(monthYear: String)

    @Query("DELETE FROM payroll_records WHERE monthYear = :monthYear")
    suspend fun clearPayrollForMonth(monthYear: String)
}
