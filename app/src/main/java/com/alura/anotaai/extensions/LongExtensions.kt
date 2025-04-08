package com.alura.anotaai.extensions

import java.util.Calendar

fun Long.toDisplayDate(): String {
    val date = this
    val calendar = Calendar.getInstance().apply {
        timeInMillis = date
    }
    val day = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    val year = calendar.get(Calendar.YEAR)
    val hour = calendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
    val minute = calendar.get(Calendar.MINUTE).toString().padStart(2, '0')
    val second = calendar.get(Calendar.SECOND).toString().padStart(2, '0')
    return "$day/$month/$year $hour:$minute:$second"
}