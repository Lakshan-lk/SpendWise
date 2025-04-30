package com.example.spendwise.models

import android.graphics.Color
import java.util.UUID
import com.example.spendwise.TransactionType

data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Int = Color.GRAY,
    val iconResId: Int = android.R.drawable.ic_menu_help,
    val forTransactionType: TransactionType? = null // null means category works for both types
)
