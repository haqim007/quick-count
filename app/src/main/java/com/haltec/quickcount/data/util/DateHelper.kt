package com.haltec.quickcount.data.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun convertDate(dateInput: String, format: String ="yyyy-MM-dd'T'HH:mm:ss"): String {
    val inputFormat = SimpleDateFormat(format, Locale.getDefault())
    val date = inputFormat.parse(dateInput)
    val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return date?.let { outputFormat.format(it) } ?: "Jan 1, 1999"
}

fun stringToDate(dateInput: String, format: String ="yyyy-MM-dd HH:mm:ss"): Date? {
    val inputFormat = SimpleDateFormat(format, Locale.getDefault())
    return inputFormat.parse(dateInput)
}

fun dateToTimestamp(dateInput: Date): Long{
    return dateInput.time / 1000
}

fun stringToTimestamp(dateInput: String, format: String ="yyyy-MM-dd HH:mm:ss"): Long?{
    val date = stringToDate(dateInput, format)
    return date?.let { dateToTimestamp(it) }
}

fun currentTimestamp() = System.currentTimeMillis() / 1000
