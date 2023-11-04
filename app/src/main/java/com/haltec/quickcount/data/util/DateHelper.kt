package com.haltec.quickcount.data.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun stringToDate(dateInput: String, format: String = DATE_TIME_FORMAT): Date? {
    return try {
        val inputFormat = SimpleDateFormat(format, Locale.getDefault())
        inputFormat.parse(dateInput)
    }catch (e: Exception){
        try {
            val inputFormat = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
            inputFormat.parse(dateInput)
        }catch (e: Exception){
            null
        }
    }
}

fun stringToStringDateID(dateInput: String, format: String = DATE_TIME_FORMAT): String {
    val originalFormat = stringToDate(dateInput, format)
    val parsedDateTime = SimpleDateFormat("dd MMMM yyyy 'Pukul' HH:mm", Locale("id", "ID"))
    return originalFormat?.let { parsedDateTime.format(it) } ?: "-"
}

fun dateToTimestamp(dateInput: Date): Long{
    return dateInput.time / 1000
}
const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
const val DATE_TIME_WITH_TIMEZONE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX"
fun stringToTimestamp(dateInput: String, format: String = DATE_TIME_FORMAT): Long?{
    val date = stringToDate(dateInput, format)
    return date?.let { dateToTimestamp(it) }
}

fun currentTimestamp() = System.currentTimeMillis() / 1000
