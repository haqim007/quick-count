package com.haltec.quickcount.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

fun capitalizeWords(text: String): String{
    val texts = text.split(" ")
    return texts.joinToString(" ") { it.replaceFirstChar { it.uppercase() } }
}

fun lowerAllWords(text: String): String{
    val texts = text.split(" ")
    return texts.joinToString(" ") { it.lowercase() } 
}

fun formatNumberWithSeparator(number: Int, groupSeparator: Char = '.', decimalSeparator: Char = ','): String {
    val numberFormat = NumberFormat.getInstance(Locale.getDefault())
    if (numberFormat is DecimalFormat) {
        val symbols = numberFormat.decimalFormatSymbols
        symbols.groupingSeparator = groupSeparator
        symbols.decimalSeparator = decimalSeparator
        numberFormat.decimalFormatSymbols = symbols
    }
    return numberFormat.format(number)
}