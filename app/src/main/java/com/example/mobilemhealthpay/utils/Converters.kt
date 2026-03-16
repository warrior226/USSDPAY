package com.example.mobilemhealthpay.utils

import androidx.room.TypeConverter
import com.example.mobilemhealthpay.data.entity.BeneficiaireEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {

    // Convert Date to Long (timestamp)
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // Convert List<String> to JSON string and vice versa
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return if (value == null) null else Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return if (value == null) null else {
            val listType = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(value, listType)
        }
    }

    // Convert List<Int> to JSON string and vice versa
    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        return if (value == null) null else Gson().toJson(value)
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        return if (value == null) null else {
            val listType = object : TypeToken<List<Int>>() {}.type
            Gson().fromJson(value, listType)
        }
    }

    // Convert custom object to JSON string (if needed)
    // Example: If you need to store complex objects as JSON
    @TypeConverter
    fun fromBeneficiaireList(value: List<BeneficiaireEntity>?): String? {
        return if (value == null) null else Gson().toJson(value)
    }

    @TypeConverter
    fun toBeneficiaireList(value: String?): List<BeneficiaireEntity>? {
        return if (value == null) null else {
            val listType = object : TypeToken<List<BeneficiaireEntity>>() {}.type
            Gson().fromJson(value, listType)
        }
    }

    // Convert Map<String, Any> to JSON string and vice versa
    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? {
        return if (value == null) null else Gson().toJson(value)
    }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? {
        return if (value == null) null else {
            val mapType = object : TypeToken<Map<String, String>>() {}.type
            Gson().fromJson(value, mapType)
        }
    }

    // Convert Boolean to Int (SQLite doesn't have native Boolean)
    @TypeConverter
    fun fromBoolean(value: Boolean?): Int? {
        return when (value) {
            true -> 1
            false -> 0
            null -> null
        }
    }

    @TypeConverter
    fun toBoolean(value: Int?): Boolean? {
        return when (value) {
            1 -> true
            0 -> false
            else -> null
        }
    }

}