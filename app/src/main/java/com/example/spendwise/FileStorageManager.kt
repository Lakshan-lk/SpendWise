package com.example.spendwise.storage

import android.content.Context
import com.example.spendwise.TransactionData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class FileStorageManager(private val context: Context) {

    companion object {
        private const val TRANSACTIONS_FILE_NAME = "transactions.json"

        @Volatile
        private var instance: FileStorageManager? = null

        fun getInstance(context: Context): FileStorageManager {
            return instance ?: synchronized(this) {
                instance ?: FileStorageManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    private val gson = Gson()
    private val transactionsFile: File
        get() = File(context.filesDir, TRANSACTIONS_FILE_NAME)

    // Save all transactions to a file
    fun saveTransactions(transactions: List<TransactionData>) {
        try {
            if (!transactionsFile.exists()) {
                transactionsFile.createNewFile()
            }

            FileWriter(transactionsFile).use { writer ->
                gson.toJson(transactions, writer)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // In a real app, you would handle errors properly
        }
    }

    // Load all transactions from file
    fun loadTransactions(): List<TransactionData> {
        if (!transactionsFile.exists()) {
            return emptyList()
        }

        try {
            FileReader(transactionsFile).use { reader ->
                val transactionListType = object : TypeToken<List<TransactionData>>() {}.type
                return gson.fromJson(reader, transactionListType) ?: emptyList()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return emptyList()
        }
    }

    // Add a single transaction
    fun addTransaction(transaction: TransactionData) {
        val transactions = loadTransactions().toMutableList()
        transactions.add(transaction)
        saveTransactions(transactions)
    }

    // Delete a transaction by ID
    fun deleteTransaction(transactionId: Long) {
        val transactions = loadTransactions().toMutableList()
        transactions.removeAll { it.id == transactionId }
        saveTransactions(transactions)
    }

    // Update an existing transaction
    fun updateTransaction(transaction: TransactionData) {
        val transactions = loadTransactions().toMutableList()
        val index = transactions.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            transactions[index] = transaction
            saveTransactions(transactions)
        }
    }
}
