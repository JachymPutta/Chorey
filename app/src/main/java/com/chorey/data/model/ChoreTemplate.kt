package com.chorey.data.model

enum class ChoreTemplate(private val s: String) {
    Trash(""),
    Dog(""),
    Kitchen(""),
    Vacuum(""),
    Dishes("");

    override fun toString(): String {
        return s
    }
}
