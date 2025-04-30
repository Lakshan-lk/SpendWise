package com.example.spendwise

import android.content.Context
import com.example.spendwise.storage.FileStorageManager
import java.util.Date

class TransactionRepository(context: Context) {
    private val storageManager = FileStorageManager.getInstance(context)

    // Get all transactions
    fun getAllTransactions(): List<TransactionData> {
        return storageManager.loadTransactions()
    }

    // Get recent transactions limited to a specific count
    fun getRecentTransactions(count: Int): List<TransactionData> {
        return getAllTransactions()
            .sortedByDescending { it.date }
            .take(count)
    }

    // Add a new transaction (updated version)
    fun addTransaction(transaction: TransactionData): TransactionData {
        val newId = getNextId()
        val finalTransaction = transaction.copy(
            id = newId,
            date = Date(),
            type = if (transaction.amount >= 0) TransactionType.INCOME else TransactionType.EXPENSE
        )

        storageManager.addTransaction(finalTransaction)
        return finalTransaction
    }

    // Keep the old version for backward compatibility
    @Deprecated("Use addTransaction(TransactionData) instead", ReplaceWith("addTransaction(TransactionData(title, amount, category, description))"))
    fun addTransaction(title: String, amount: Double, category: String = "", description: String = ""): TransactionData {
        return addTransaction(
            TransactionData(
                title = title,
                amount = amount,
                category = category,
                description = description
            )
        )
    }

    // Update an existing transaction
    fun updateTransaction(transaction: TransactionData) {
        storageManager.updateTransaction(transaction)
    }

    // Delete a transaction by ID
    fun deleteTransaction(transactionId: Long) {
        storageManager.deleteTransaction(transactionId)
    }

    // Calculate total income
    fun getTotalIncome(): Double {
        return getAllTransactions()
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
    }

    // Calculate total expenses
    fun getTotalExpenses(): Double {
        return getAllTransactions()
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { -it.amount } // Convert to positive for calculations
    }

    // Calculate remaining budget
    fun getRemainingBudget(budget: Double): Double {
        return budget - getTotalExpenses()
    }

    // Generate next ID for a new transaction
    private fun getNextId(): Long {
        val transactions = getAllTransactions()
        return if (transactions.isEmpty()) 1 else transactions.maxOf { it.id } + 1
    }
}