package com.snowman.neverlate.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TimeUtil {

    fun convertMillisToDateTime(millis: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getDefault()

        val dateTime = Date(millis)
        return dateFormat.format(dateTime)
    }

}