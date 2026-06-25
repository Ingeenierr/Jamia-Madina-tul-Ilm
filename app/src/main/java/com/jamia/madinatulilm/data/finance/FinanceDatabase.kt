package com.jamia.madinatulilm.data.finance

import android.content.Context
import androidx.room.*
import com.jamia.madinatulilm.data.Converters

@Database(
    entities = [
        InventoryItem::class,
        Transaction::class,
        DonationRecord::class,
        PayrollRecord::class,
        LibraryBook::class,
        LendingRecord::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getDatabase(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
