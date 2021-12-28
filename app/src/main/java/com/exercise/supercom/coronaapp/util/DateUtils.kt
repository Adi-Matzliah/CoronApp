package com.exercise.supercom.coronaapp.util

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    @Synchronized
    fun formatDateToApiFormat(
        date: Date = Date(),
        patternOutput: String = "yyyy-MM-dd"
    ): String =
        DateFormat.format(patternOutput, date) as String

    @Synchronized
    fun areDatesArrangedChronologically(
        from: String,
        to: String
    ): Boolean {
        return try {
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).parse(to)!! >= SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(from)
        } catch (e: Exception) {
            false
        }
    }
}
