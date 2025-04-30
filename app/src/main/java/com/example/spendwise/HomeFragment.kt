package com.example.spendwise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.spendwise.utils.CurrencyFormatter

class HomeFragment : Fragment(), AddTransactionDialogFragment.TransactionDialogListener {

    private lateinit var tvGreeting: TextView
    private lateinit var tvDate: TextView
    private lateinit var btnAddIncome: MaterialButton
    private lateinit var btnAddExpense: MaterialButton
    private lateinit var rvTransactions: RecyclerView
    private lateinit var tvViewAll: TextView
    private lateinit var tvTotalBudgetValue: TextView
    private lateinit var tvTotalExpensesValue: TextView
    private lateinit var tvRemainingBudgetValue: TextView
    private lateinit var tvBudgetStatus: TextView

    private var totalBudget = 0.0
    private var totalExpenses = 0.0
    private var remainingBudget = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupGreetingAndDate()
        setupButtons()
        refreshData()
    }

    private fun initViews(view: View) {
        tvGreeting = view.findViewById(R.id.tv_greeting)
        tvDate = view.findViewById(R.id.tv_date)
        btnAddIncome = view.findViewById(R.id.btn_add_income)
        btnAddExpense = view.findViewById(R.id.btn_add_expense)
        rvTransactions = view.findViewById(R.id.rv_transactions)
        tvViewAll = view.findViewById(R.id.tv_view_all)
        tvTotalBudgetValue = view.findViewById(R.id.tv_total_budget_value)
        tvTotalExpensesValue = view.findViewById(R.id.tv_total_expenses_value)
        tvRemainingBudgetValue = view.findViewById(R.id.tv_remaining_budget_value)
        tvBudgetStatus = view.findViewById(R.id.tv_budget_status)
    }

    private fun setupGreetingAndDate() {
        val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hourOfDay < 12 -> "Good morning"
            hourOfDay < 18 -> "Good afternoon"
            else -> "Good evening"
        }

        val username = PreferenceManager.getInstance(requireContext()).getUserName() ?: "User"
        tvGreeting.text = "$greeting, $username 👋"

        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        tvDate.text = dateFormat.format(Date())
    }

    private fun setupButtons() {
        btnAddIncome.setOnClickListener {
            showAddTransactionDialog(TransactionType.INCOME)
        }

        btnAddExpense.setOnClickListener {
            showAddTransactionDialog(TransactionType.EXPENSE)
        }

        tvViewAll.setOnClickListener {
            navigateToAllTransactions()
        }
    }

    private fun showAddTransactionDialog(type: TransactionType) {
        AddTransactionDialogFragment.newInstance(type)
            .show(childFragmentManager, AddTransactionDialogFragment.TAG)
    }

    override fun onTransactionAdded(transaction: TransactionData) {
        refreshData()
    }

    private fun refreshData() {
        setupRecentTransactions()
        calculateBudgetSummary()
        updateBudgetSummaryUI()
    }

    private fun setupRecentTransactions() {
        val repository = TransactionRepository(requireContext())
        val recentTransactions = repository.getRecentTransactions(3)

        rvTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = TransactionsAdapter(recentTransactions)
        }
    }

    private fun calculateBudgetSummary() {
        val repository = TransactionRepository(requireContext())
        val prefs = PreferenceManager.getInstance(requireContext())

        totalBudget = prefs.getMonthlyBudget()
        totalExpenses = repository.getTotalExpenses()
        remainingBudget = repository.getRemainingBudget(totalBudget)
    }

    private fun updateBudgetSummaryUI() {
        tvTotalBudgetValue.text = CurrencyFormatter.format(totalBudget)
        tvTotalExpensesValue.text = CurrencyFormatter.format(totalExpenses)
        tvRemainingBudgetValue.text = CurrencyFormatter.format(remainingBudget)

        val percentageUsed = if (totalBudget > 0) {
            ((totalExpenses / totalBudget) * 100).toInt()
        } else {
            0
        }
        tvBudgetStatus.text = "You've used $percentageUsed% of your budget"
    }

    private fun navigateToAllTransactions() {
        try {
            // Use the same approach your MainActivity uses
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, TransactionsFragment())
            transaction.addToBackStack(null)
            transaction.commit()

            // Update bottom navigation selection
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.selectedItemId = R.id.nav_transactions
        } catch (e: Exception) {
            Log.e("HomeFragment", "Navigation failed", e)
            Toast.makeText(context, "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}