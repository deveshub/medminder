package com.medicinereminder.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import java.time.ZoneOffset

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(ZoneOffset.UTC)
    }

    @TypeConverter
    fun fromLocalDateTimeList(value: String?): List<LocalDateTime> {
        if (value == null) return emptyList()
        val listType = object : TypeToken<List<Long>>() {}.type
        return gson.fromJson<List<Long>>(value, listType)
            .map { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    @TypeConverter
    fun toLocalDateTimeList(list: List<LocalDateTime>?): String {
        if (list == null) return "[]"
        val timestamps = list.map { it.toEpochSecond(ZoneOffset.UTC) }
        return gson.toJson(timestamps)
    }

    @TypeConverter
    fun fromStringSet(value: String?): Set<String>? {
        if (value == null) return null
        val listType = object : TypeToken<Set<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toStringSet(set: Set<String>?): String? {
        if (set == null) return null
        return gson.toJson(set)
    }
} 