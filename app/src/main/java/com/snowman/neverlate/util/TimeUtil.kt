package com.snowman.neverlate.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.google.firebase.Timestamp
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TimeUtil {

    companion object {
        val dateFormat = SimpleDateFormat("E MMM dd h:mm a", Locale.getDefault())
        fun localDateTime2Timestamp(localDateTime: LocalDateTime): Timestamp {
            val date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())

            // Return the Firebase Timestamp
            return Timestamp(date)
        }

        /***
         * @param timestamp com.google.firebase.Timestamp
         * @param pattern A valid Pattern String
         * @return A String following the given pattern
         */
        @SuppressLint("SimpleDateFormat")
        fun timestamp2FormattedString(timestamp: Timestamp, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
            val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
            val date = java.util.Date(milliseconds)
            val dateFormat = SimpleDateFormat(pattern)
            dateFormat.timeZone = TimeZone.getDefault()
            return dateFormat.format(date)
        }

        fun timeStamp2LocalDateTime(timestamp: Timestamp): LocalDateTime {
            val instant = Instant.ofEpochSecond(timestamp.seconds, timestamp.nanoseconds.toLong())
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        }

        fun localDateTime2FormattedString(localDateTime: LocalDateTime, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            return localDateTime.format(formatter)
        }

        fun duration2FormattedString(duration: Duration, pattern: String = "%+d days %d:%02d:%02d"): String {
            val isNegative = duration.isNegative
            val absDuration = duration.abs()

            val days = absDuration.toDays()
            val hours = absDuration.toHours() % 24 // Calculate remaining hours after days
            val minutes = (absDuration.toMinutes() % 60) // Calculate remaining minutes after hours
            val seconds = (absDuration.seconds % 60) // Calculate remaining seconds after minutes

            val sign = if (isNegative) "-" else "+"
            return if (days > 0)
                String.format("$sign%d days %d:%02d:%02d", days, hours, minutes, seconds)
            else
                String.format("$sign%d:%02d:%02d", hours, minutes, seconds)
        }

        fun convertMillisToDateTime(millis: Long): String {
            dateFormat.timeZone = TimeZone.getDefault()

            val dateTime = Date(millis)
            return dateFormat.format(dateTime)
        }
    }


}