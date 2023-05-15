package com.chorey.data

enum class RepeatInterval(private val s : String) {
    None("Doesn't Repeat"),
    AsNeeded("As needed"),
    Year("Annually"),
    Month("Monthly"),
    Week("Weekly"),
    Day("Daily"),
    Hour("Hourly");
    override fun toString(): String {
        return s
    }
}
