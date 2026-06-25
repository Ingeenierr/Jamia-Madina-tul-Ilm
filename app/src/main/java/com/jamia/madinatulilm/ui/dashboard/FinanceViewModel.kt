package com.jamia.madinatulilm.ui.dashboard

import androidx.lifecycle.*
import com.jamia.madinatulilm.data.finance.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {

    private val currentMonth = SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(Date())

    // Hero Metric
    val monthlyBalance: StateFlow<Double> = combine(
        repository.getMonthlyBalance(currentMonth),
        repository.getDonations(currentMonth),
        repository.getPaidPayroll(currentMonth)
    ) { profit, donations, payroll ->
        (profit ?: 0.0) + (donations ?: 0.0) - (payroll ?: 0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Stable Payroll Flow with Optimistic State
    private val _localPayrollUpdates = MutableStateFlow<Map<String, PayrollRecord>>(emptyMap())
    val payroll: StateFlow<List<PayrollRecord>> = repository.getPayroll(currentMonth)
        .combine(_localPayrollUpdates) { repoList, localMap ->
            val result = repoList.toMutableList()
            localMap.forEach { (id, localRecord) ->
                val index = result.indexOfFirst { it.id == id }
                if (index != -1) {
                    result[index] = localRecord
                } else if (localRecord.monthYear == currentMonth) {
                    result.add(localRecord)
                }
            }
            result.sortedBy { it.teacherName }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI tracking for progress indicators
    private val _pendingPayrollIds = MutableStateFlow<Set<String>>(emptySet())
    val pendingPayrollUpdates = _pendingPayrollIds.asStateFlow()

    // Donation Flow
    val donations: StateFlow<List<DonationRecord>> = repository.getAllDonations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<DonationRecord>())

    // Library & Inventory
    val libraryBooks: StateFlow<List<LibraryBook>> = repository.getBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lendingRecords: StateFlow<List<LendingRecord>> = repository.getLendingRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All transactions for the current month (for reporting)
    val monthlyTransactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .map { list -> list.filter { it.monthYear == currentMonth } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart Logic
    private val _cart = MutableStateFlow<List<TransactionItem>>(emptyList())
    val cart: StateFlow<List<TransactionItem>> = _cart.asStateFlow()

    private val inventoryCache = mutableMapOf<String, StateFlow<List<InventoryItem>>>()

    fun getInventory(category: String): StateFlow<List<InventoryItem>> {
        return inventoryCache.getOrPut(category) {
            repository.getInventory(category)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }
    }

    fun addToCart(item: InventoryItem) {
        val current = _cart.value.toMutableList()
        val existing = current.find { it.itemId == item.id }
        if (existing != null) {
            val index = current.indexOf(existing)
            current[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            current.add(TransactionItem(item.id, item.name, 1, item.price))
        }
        _cart.value = current
    }

    fun removeFromCart(itemId: String) {
        _cart.value = _cart.value.filter { it.itemId != itemId }
    }

    fun deleteInventoryItem(item: InventoryItem) = viewModelScope.launch {
        repository.deleteInventoryItem(item)
    }

    fun checkout(category: String) = viewModelScope.launch {
        if (_cart.value.isEmpty()) return@launch
        val total = _cart.value.sumOf { it.price * it.quantity }
        repository.performSale(Transaction(
            type = "${category}_SALE",
            items = _cart.value,
            totalAmount = total,
            profit = total * 0.25,
            monthYear = currentMonth,
            timestamp = System.currentTimeMillis()
        ))
        _cart.value = emptyList()
    }

    fun deleteDonation(record: DonationRecord) = viewModelScope.launch {
        repository.deleteDonation(record)
    }

    fun deletePayroll(record: PayrollRecord) = viewModelScope.launch {
        repository.deletePayroll(record)
    }

    fun togglePayroll(teacherId: String, teacherName: String, amount: Double) = viewModelScope.launch {
        val payrollId = "${teacherId}_$currentMonth"
        val existing = payroll.value.find { it.id == payrollId }
        
        val updatedRecord = if (existing != null) {
            existing.copy(isPaid = !existing.isPaid, paymentDate = if (!existing.isPaid) System.currentTimeMillis() else null)
        } else {
            PayrollRecord(id = payrollId, teacherId = teacherId, teacherName = teacherName, amount = amount, monthYear = currentMonth, isPaid = true, paymentDate = System.currentTimeMillis())
        }

        _localPayrollUpdates.update { it + (payrollId to updatedRecord) }
        _pendingPayrollIds.update { it + payrollId }
        
        try {
            repository.updatePayroll(updatedRecord)
        } catch (e: Exception) {
            _localPayrollUpdates.update { it - payrollId }
        } finally {
            _pendingPayrollIds.update { it - payrollId }
        }
    }

    fun addDonation(name: String, amount: Double, type: String, isFixed: Boolean = false, studentId: String? = null, teacherId: String? = null) = viewModelScope.launch {
        repository.addDonation(DonationRecord(donorName = name, amount = amount, type = type, isFixed = isFixed, studentId = studentId, teacherId = teacherId, monthYear = currentMonth, timestamp = System.currentTimeMillis()))
    }

    fun addBook(title: String, author: String, category: String, totalCopies: Int) = viewModelScope.launch {
        repository.saveBook(LibraryBook(title = title, author = author, category = category, totalCopies = totalCopies, availableCopies = totalCopies))
    }

    fun addInventoryItem(category: String, name: String, price: Double, costPrice: Double, unitCount: Int, boxCount: Int, unitsPerBox: Int) = viewModelScope.launch {
        repository.saveInventoryItem(InventoryItem(
            name = name, 
            category = category, 
            price = price, 
            costPrice = costPrice, 
            unitCount = unitCount,
            boxCount = boxCount,
            unitsPerBox = unitsPerBox
        ))
    }

    fun deleteBook(book: LibraryBook) = viewModelScope.launch {
        repository.deleteBook(book)
    }

    fun updateBook(book: LibraryBook) = viewModelScope.launch {
        repository.saveBook(book)
    }

    fun lendBook(book: LibraryBook, borrowerId: String, borrowerName: String) = viewModelScope.launch {
        if (book.availableCopies > 0) {
            val record = LendingRecord(
                id = repository.provideLendingRef().push().key ?: "",
                bookId = book.id,
                bookTitle = book.title,
                borrowerId = borrowerId,
                borrowerName = borrowerName,
                borrowDate = System.currentTimeMillis(),
                dueDate = System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000),
                status = "BORROWED"
            )
            repository.lendBook(record)
            repository.saveBook(book.copy(availableCopies = book.availableCopies - 1))
        }
    }

    fun returnBook(record: LendingRecord) = viewModelScope.launch {
        if (record.status != "RETURNED") {
            val updatedRecord = record.copy(
                status = "RETURNED",
                returnDate = System.currentTimeMillis()
            )
            repository.updateLendingRecord(updatedRecord)
            
            // Sync with Library inventory
            val book = libraryBooks.value.find { it.id == record.bookId }
            if (book != null) {
                repository.saveBook(book.copy(availableCopies = (book.availableCopies + 1).coerceAtMost(book.totalCopies)))
            }
        }
    }

    fun resetMonthlyBalance() = viewModelScope.launch {
        repository.clearMonthlyData(currentMonth)
    }

    fun exportReport(context: android.content.Context, uri: android.net.Uri) {
        com.jamia.madinatulilm.utils.ExcelExporter.exportFinanceReportToExcel(
            context = context,
            month = currentMonth,
            donations = donations.value,
            payroll = payroll.value,
            transactions = monthlyTransactions.value,
            uri = uri
        )
    }
}

class FinanceViewModelFactory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FinanceViewModel(repository) as T
    }
}
