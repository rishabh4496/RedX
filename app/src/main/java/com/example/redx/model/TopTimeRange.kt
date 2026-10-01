package com.example.redx.model

/** Time window for the "Top" sort; maps to Reddit's `t=` query parameter. */
enum class TopTimeRange(val label: String, val apiValue: String) {
    HOUR("Hour", "hour"),
    DAY("Today", "day"),
    WEEK("Week", "week"),
    MONTH("Month", "month"),
    YEAR("Year", "year"),
    ALL("All time", "all");

    companion object {
        val DEFAULT = DAY

        fun fromName(name: String?): TopTimeRange =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
