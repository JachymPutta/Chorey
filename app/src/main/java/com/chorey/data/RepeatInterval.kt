package com.chorey.data

enum class RepeatInterval(private val s : String) {
    None("Doesn't Repeat"),
    Year("Every Year"),
    Month("Every Month"),
    Day("Daily"),
    Hour("Hourly");
    override fun toString(): String {
        return s
    }
}
