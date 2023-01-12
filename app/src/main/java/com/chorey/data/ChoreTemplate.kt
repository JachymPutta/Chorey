package com.chorey.data

enum class ChoreTemplate(private val s: String) {
    Custom(""),
    Trash("Trash"),
    Dog("Dog"),
    Kitchen("Kitchen"),
    Vacuum("Vacuum"),
    Dishes("Dishes");

    override fun toString(): String {
        return s
    }
}
