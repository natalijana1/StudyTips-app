package com.natali.studytip.utils

import kotlin.math.abs

object TimeUtils {

    /**
     * Converts a timestamp to a relative time string (e.g., "about 5 hours ago")
     * @param timestamp The timestamp in milliseconds
     * @return A human-readable relative time string
     */
    fun getRelativeTimeString(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        // Handle future timestamps
        if (diff < 0) {
            return "just now"
        }

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return when {
            seconds < 60 -> "just now"
            minutes < 2 -> "about a minute ago"
            minutes < 60 -> "about $minutes minutes ago"
            hours < 2 -> "about an hour ago"
            hours < 24 -> "about $hours hours ago"
            days < 2 -> "about a day ago"
            days < 7 -> "about $days days ago"
            weeks < 2 -> "about a week ago"
            weeks < 4 -> "about $weeks weeks ago"
            months < 2 -> "about a month ago"
            months < 12 -> "about $months months ago"
            years < 2 -> "about a year ago"
            else -> "about $years years ago"
        }
    }
}
