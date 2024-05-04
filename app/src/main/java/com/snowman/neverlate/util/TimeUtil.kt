package com.snowman.neverlate.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TimeUtil {

    companion object {
        val dateFormat = SimpleDateFormat("E MMM dd h:mm a", Locale.getDefault())
    }

    fun convertMillisToDateTime(millis: Long): String {
        dateFormat.timeZone = TimeZone.getDefault()

        val dateTime = Date(millis)
        return dateFormat.format(dateTime)
    }
}