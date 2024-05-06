package com.snowman.neverlate.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.ZoneId

class TimeUtil {

    companion object {
        val dateFormat = SimpleDateFormat("E MMM dd h:mm a", Locale.getDefault())
        fun convertTimeAndDateToTimestamp(localDate: LocalDate, localTime: LocalTime): Timestamp {
            val localDateTime = LocalDateTime.of(localDate, localTime)

            // Convert LocalDateTime to Date
            val date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())

            // Return the Firebase Timestamp
            return Timestamp(date)
        }
        fun convertMillisToDateTime(millis: Long): String {
            dateFormat.timeZone = TimeZone.getDefault()

            val dateTime = Date(millis)
            return dateFormat.format(dateTime)
        }
    }


}