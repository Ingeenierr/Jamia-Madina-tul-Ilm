package com.jamia.madinatulilm.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jamia.madinatulilm.data.finance.TransactionItem

class Converters {
    @TypeConverter
    fun fromTransactionItemList(value: List<TransactionItem>?): String? {
        val gson = Gson()
        val type = object : TypeToken<List<TransactionItem>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toTransactionItemList(value: String?): List<TransactionItem>? {
        val gson = Gson()
        val type = object : TypeToken<List<TransactionItem>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
