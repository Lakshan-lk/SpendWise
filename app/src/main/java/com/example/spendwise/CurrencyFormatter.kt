package com.example.spendwise.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val formatter = NumberFormat.getCurrencyInstance(Locale("en", "LK"))

    init {
        formatter.maximumFractionDigits = 2
        formatter.minimumFractionDigits = 2
    }

    fun format(amount: Double): String {
        return formatter.format(amount)
    }
}