package com.chorey.data

enum class ExpenseType(private val s: String) {
    Rent("Rent"),
    Utilities("Utilities"),
    Groceries("Groceries"),
    Household("Household"),
    Fun("Fun"),
    Other("Other");

    override fun toString(): String {
        return s
    }
}