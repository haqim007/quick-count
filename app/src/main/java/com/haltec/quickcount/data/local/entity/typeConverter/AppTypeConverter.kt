package com.haltec.quickcount.data.local.entity.typeConverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.haltec.quickcount.data.local.entity.table.CandidateEntity
import com.haltec.quickcount.data.local.entity.table.PartyEntity
import com.haltec.quickcount.data.local.entity.table.TempVoteCandidateEntity
import com.haltec.quickcount.data.local.entity.table.TempVotePartyEntity
import java.io.File

class AppTypeConverter {
    @TypeConverter
    fun fromTempVoteCandidateString(value: String): List<TempVoteCandidateEntity>? {
        val listType = object : TypeToken<List<TempVoteCandidateEntity>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromTempVoteCandidateList(list: List<TempVoteCandidateEntity>?): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromTempVotePartyString(value: String): List<TempVotePartyEntity>? {
        val listType = object : TypeToken<List<TempVotePartyEntity>>() {}.type
        return Gson().fromJson(value, listType)
    }
    @TypeConverter
    fun fromTempVotePartyList(list: List<TempVotePartyEntity>?): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromPartyString(value: String): List<PartyEntity> {
        val listType = object : TypeToken<List<PartyEntity>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromTempVotePartyArrayList(list: ArrayList<TempVotePartyEntity>?): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromPartyStringToArrayList(value: String): ArrayList<PartyEntity> {
        val listType = object : TypeToken<ArrayList<PartyEntity>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromPartyList(list: List<PartyEntity>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromCandidateString(value: String): List<CandidateEntity> {
        val listType = object : TypeToken<List<CandidateEntity>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromCandidateList(list: List<CandidateEntity>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromFile(file: File): String? {
        return file.absolutePath
    }

    @TypeConverter
    fun fromFileNullable(file: File?): String? {
        return file?.absolutePath
    }

    @TypeConverter
    fun toFile(path: String): File? {
        return if (path.isEmpty()) null else File(path)
    }
}