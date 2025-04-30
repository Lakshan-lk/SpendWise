package com.example.spendwise

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

import com.example.spendwise.utils.CurrencyFormatter

class AnalysisFragment : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var spinnerMonth: Spinner
    private lateinit var tvTotalSpent: TextView
    private lateinit var tvTotalEarned: TextView
    private lateinit var tvSavings: TextView
    private lateinit var tvBudgetLeft: TextView
    private lateinit var tvMonthSubtitle: TextView
    private lateinit var tvSmartInsight1: TextView
    private lateinit var tvSmartInsight2: TextView
    private lateinit var rvCategories: RecyclerView

    private lateinit var transactionRepository: TransactionRepository
    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analysis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionRepository = TransactionRepository(requireContext())

        initViews(view)
        setupMonthSpinner()
        refreshData()
    }

    private fun initViews(view: View) {
        pieChart = view.findViewById(R.id.pie_chart)
        barChart = view.findViewById(R.id.bar_chart)
        spinnerMonth = view.findViewById(R.id.spinner_month)
        tvTotalSpent = view.findViewById(R.id.tv_total_spent)
        tvTotalEarned = view.findViewById(R.id.tv_total_earned)
        tvSavings = view.findViewById(R.id.tv_savings)
        tvBudgetLeft = view.findViewById(R.id.tv_budget_left)
        tvMonthSubtitle = view.findViewById(R.id.tv_month_subtitle)
        tvSmartInsight1 = view.findViewById(R.id.tv_smart_insight1)
        tvSmartInsight2 = view.findViewById(R.id.tv_smart_insight2)
        rvCategories = view.findViewById(R.id.rv_categories)

        rvCategories.layoutManager = LinearLayoutManager(context)
    }

    private fun setupMonthSpinner() {
        val months = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = adapter

        spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH))

        spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMonth = position
                refreshData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun refreshData() {
        val transactions = getTransactionsForSelectedMonth()
        updateMonthSubtitle()
        calculateAndUpdateSummary(transactions)
        setupPieChart(transactions)
        setupBarChart(transactions)
        generateInsights(transactions)
        updateCategoriesBreakdown(transactions)
    }

    private fun getTransactionsForSelectedMonth(): List<TransactionData> {
        val allTransactions = transactionRepository.getAllTransactions()

        return allTransactions.filter { transaction ->
            val calendar = Calendar.getInstance()
            calendar.time = transaction.date

            calendar.get(Calendar.MONTH) == selectedMonth &&
                    calendar.get(Calendar.YEAR) == selectedYear
        }
    }

    private fun updateMonthSubtitle() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, selectedMonth)
        calendar.set(Calendar.YEAR, selectedYear)

        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonthSubtitle.text = dateFormat.format(calendar.time)
    }


    private fun calculateAndUpdateSummary(transactions: List<TransactionData>) {
        val totalSpent = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { abs(it.amount) }

        val totalEarned = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        val savings = totalEarned - totalSpent

        val prefs = PreferenceManager.getInstance(requireContext())
        val monthlyBudget = prefs.getMonthlyBudget()
        val budgetLeft = monthlyBudget - totalSpent

        tvTotalSpent.text = CurrencyFormatter.format(totalSpent)
        tvTotalEarned.text = CurrencyFormatter.format(totalEarned)
        tvSavings.text = CurrencyFormatter.format(savings)
        tvBudgetLeft.text = CurrencyFormatter.format(budgetLeft)
    }



    private fun setupPieChart(transactions: List<TransactionData>) {
        val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
        val categoryMap = mutableMapOf<String, Double>()

        for (transaction in expenseTransactions) {
            val category = transaction.category ?: "Uncategorized"
            categoryMap[category] = (categoryMap[category] ?: 0.0) + abs(transaction.amount)
        }

        val entries = mutableListOf<PieEntry>()
        for ((category, amount) in categoryMap) {
            entries.add(PieEntry(amount.toFloat(), category))
        }

        if (entries.isEmpty()) {
            entries.add(PieEntry(100f, "No Data"))
        }

        val dataSet = PieDataSet(entries, "Expense Categories")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))

        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "Expenses"
        pieChart.setUsePercentValues(true)
        pieChart.legend.isEnabled = true
        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    private fun setupBarChart(transactions: List<TransactionData>) {
        val calendar = Calendar.getInstance()
        val weeklyExpenses = Array(5) { 0f } // Max 5 weeks in a month
        val weeklyIncome = Array(5) { 0f }

        for (transaction in transactions) {
            calendar.time = transaction.date
            val weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH) - 1

            if (weekOfMonth >= 0 && weekOfMonth < 5) {
                if (transaction.type == TransactionType.EXPENSE) {
                    weeklyExpenses[weekOfMonth] += abs(transaction.amount).toFloat()
                } else {
                    weeklyIncome[weekOfMonth] += transaction.amount.toFloat()
                }
            }
        }

        val expenseEntries = mutableListOf<BarEntry>()
        val incomeEntries = mutableListOf<BarEntry>()

        for (i in weeklyExpenses.indices) {
            expenseEntries.add(BarEntry(i.toFloat(), weeklyExpenses[i]))
            incomeEntries.add(BarEntry(i.toFloat(), weeklyIncome[i]))
        }

        val expenseDataSet = BarDataSet(expenseEntries, "Expenses")
        expenseDataSet.color = Color.rgb(255, 111, 97) // #FF6F61

        val incomeDataSet = BarDataSet(incomeEntries, "Income")
        incomeDataSet.color = Color.rgb(76, 175, 80) // #4CAF50

        val barData = BarData(expenseDataSet, incomeDataSet)
        barData.barWidth = 0.3f

        barChart.data = barData
        barChart.groupBars(0f, 0.4f, 0.05f)
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.legend.isEnabled = true
        barChart.xAxis.labelCount = 5
        barChart.xAxis.valueFormatter = WeekAxisValueFormatter()
        barChart.animateY(1000)
        barChart.invalidate()
    }

    private fun generateInsights(transactions: List<TransactionData>) {
        val totalSpent = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { abs(it.amount) }

        val totalEarned = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        val savings = totalEarned - totalSpent

        val categories = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category ?: "Uncategorized" }
            .mapValues { (_, transactions) -> transactions.sumOf { abs(it.amount) } }

        val topCategory = categories.maxByOrNull { it.value }

        if (savings > 0) {
            tvSmartInsight1.text = "You saved ${CurrencyFormatter.format(savings)} this month. Great job!"
        } else {
            tvSmartInsight1.text = "You spent ${CurrencyFormatter.format(abs(savings))} more than you earned this month."
        }

        if (topCategory != null) {
            tvSmartInsight2.text = "${topCategory.key} is your top spending category (${CurrencyFormatter.format(topCategory.value)})."
        } else {
            tvSmartInsight2.text = "You have no expense transactions this month."
        }
    }

    private fun updateCategoriesBreakdown(transactions: List<TransactionData>) {
        val categoryBreakdown = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category ?: "Uncategorized" }
            .mapValues { (_, transactions) -> transactions.sumOf { abs(it.amount) } }
            .toList()
            .sortedByDescending { it.second }

        val categoryItems = categoryBreakdown.map { (category, amount) ->
            CategoryItem(category, amount, CurrencyFormatter.format(amount))
        }

        rvCategories.adapter = CategoriesAdapter(categoryItems)
    }







    inner class WeekAxisValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return "Week ${value.toInt() + 1}"
        }
    }

    data class CategoryItem(val name: String, val amount: Double, val formattedAmount: String)

    inner class CategoriesAdapter(private val categories: List<CategoryItem>) :
            RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {

        inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val colorDot: View = view.findViewById(R.id.view_color_dot)
            val categoryName: TextView = view.findViewById(R.id.tv_category_name)
            val categoryAmount: TextView = view.findViewById(R.id.tv_category_amount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category, parent, false)
            return CategoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
            val category = categories[position]
            holder.categoryName.text = category.name
            holder.categoryAmount.text = category.formattedAmount

            val colorId = when (position % 5) {
                0 -> Color.rgb(255, 111, 97) // #FF6F61 (coral)
                1 -> Color.rgb(76, 175, 80)  // #4CAF50 (green)
                2 -> Color.rgb(63, 81, 181)  // #3F51B5 (indigo)
                3 -> Color.rgb(255, 193, 7)  // #FFC107 (amber)
                else -> Color.rgb(156, 39, 176) // #9C27B0 (purple)
            }

            holder.colorDot.setBackgroundColor(colorId)
        }

        override fun getItemCount() = categories.size
    }

    companion object {
        fun newInstance() = AnalysisFragment()
    }
}
