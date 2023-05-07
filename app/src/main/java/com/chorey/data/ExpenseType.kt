package com.chorey.data

enum class ExpenseType(private val s: String) {
    Rent("rent"),
    Utilities("utilities"),
    Groceries("groceries"),
    Household("household"),
    Fun("fun");

    override fun toString(): String {
        return s
    }
}