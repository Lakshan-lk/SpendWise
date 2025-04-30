package com.example.spendwise

    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.LinearLayout
    import androidx.fragment.app.Fragment
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.google.android.material.floatingactionbutton.FloatingActionButton
    import androidx.appcompat.app.AlertDialog

    class TransactionsFragment : Fragment(), AddTransactionDialogFragment.TransactionDialogListener,
        TransactionsAdapter.OnTransactionClickListener {

        private lateinit var recyclerView: RecyclerView
        private lateinit var fabAddTransaction: FloatingActionButton
        private lateinit var transactionRepository: TransactionRepository

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.fragment_transactions, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            transactionRepository = TransactionRepository(requireContext())

            recyclerView = view.findViewById(R.id.rv_all_transactions)
            fabAddTransaction = view.findViewById(R.id.fab_add_transaction)

            setupRecyclerView()
            setupFab()
        }

        override fun onResume() {
            super.onResume()
            // Refresh data when returning to this fragment
            loadTransactions()
        }

        private fun setupRecyclerView() {
            recyclerView.layoutManager = LinearLayoutManager(context)
            loadTransactions()
        }

        private fun loadTransactions() {
            // Get real transactions from repository
            val transactions = transactionRepository.getAllTransactions()
                .sortedByDescending { it.date } // Most recent first

            // Update empty state visibility
            view?.findViewById<LinearLayout>(R.id.empty_state)?.visibility =
                if (transactions.isEmpty()) View.VISIBLE else View.GONE
            recyclerView.visibility = if (transactions.isEmpty()) View.GONE else View.VISIBLE

            recyclerView.adapter = TransactionsAdapter(transactions, this)
        }

        private fun setupFab() {
            fabAddTransaction.setOnClickListener {
                // Show add transaction dialog
                AddTransactionDialogFragment.newInstance(TransactionType.EXPENSE)
                    .show(childFragmentManager, AddTransactionDialogFragment.TAG)
            }
        }

        override fun onTransactionAdded(transaction: TransactionData) {
            // Refresh list after adding a transaction
            loadTransactions()
        }

        override fun onTransactionClick(transaction: TransactionData, position: Int) {
            // Show transaction details or edit dialog
            showEditTransactionDialog(transaction)
        }

        override fun onTransactionEdit(transaction: TransactionData, position: Int) {
            showEditTransactionDialog(transaction)
        }

        override fun onTransactionDelete(transaction: TransactionData, position: Int) {
            showDeleteConfirmationDialog(transaction)
        }

        private fun showEditTransactionDialog(transaction: TransactionData) {
            // Create and show edit dialog
            val dialog = AddTransactionDialogFragment.newInstance(transaction.type, transaction)
            dialog.show(childFragmentManager, AddTransactionDialogFragment.TAG)
        }

        private fun showDeleteConfirmationDialog(transaction: TransactionData) {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteTransaction(transaction)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        private fun deleteTransaction(transaction: TransactionData) {
            transactionRepository.deleteTransaction(transaction.id)
            loadTransactions()
        }

        fun onTransactionUpdated(transaction: TransactionData) {
            loadTransactions()
        }
    }
