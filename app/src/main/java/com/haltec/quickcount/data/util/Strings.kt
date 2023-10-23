package com.haltec.quickcount.data.util

fun capitalizeWords(text: String): String{
    val texts = text.split(" ")
    return texts.joinToString(" ") { it.replaceFirstChar { it.uppercase() } }
}