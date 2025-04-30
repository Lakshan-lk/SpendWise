package com.example.spendwise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*
import android.widget.ImageView
import android.widget.TextView
import com.example.spendwise.databinding.FragmentBudgetBinding
import com.example.spendwise.utils.CurrencyFormatter
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    // Change lateinit val to lateinit var for all views
    private lateinit var budgetInput: com.google.android.material.textfield.TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var progressIndicator: CircularProgressIndicator  // Renamed from progressCircular
    private lateinit var progressPercentageText: TextView
    private lateinit var usageStatusText: TextView
    private lateinit var monthlyBudgetValue: TextView
    private lateinit var totalExpensesValue: TextView
    private lateinit var remainingBudgetValue: TextView
    private lateinit var alertIcon: ImageView
    private lateinit var alertMessage: TextView
    private lateinit var currentMonthText: TextView

    private var monthlyBudget = 0.0
    private var totalExpenses = 0.0
    private var remainingBudget = 0.0
    private var usagePercentage = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews()
        setupCurrentMonth()
        loadBudgetData()
        setupSaveButton()
    }

    private fun initViews() {
        with(binding) {
            budgetInput = etBudgetAmount
            saveButton = btnSaveBudget
            progressIndicator = progressCircular  // Fixed assignment
            progressPercentageText = tvProgressPercentage
            usageStatusText = tvUsageStatus
            monthlyBudgetValue = tvMonthlyBudgetValue
            totalExpensesValue = tvTotalExpensesValue
            remainingBudgetValue = tvRemainingBudgetValue
            alertIcon = ivAlertIcon
            alertMessage = tvAlertMessage
            currentMonthText = tvCurrentMonth
        }
    }

    private fun setupCurrentMonth() {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        currentMonthText.text = dateFormat.format(Date())
    }

    private fun loadBudgetData() {
        val prefs = PreferenceManager.getInstance(requireContext())
        val repository = TransactionRepository(requireContext())
        
        // Get budget from SharedPreferences
        monthlyBudget = prefs.getMonthlyBudget()
        
        // Get expenses and calculate remaining budget
        totalExpenses = repository.getTotalExpenses()
        remainingBudget = if (monthlyBudget > 0) monthlyBudget - totalExpenses else 0.0
        
        // Calculate usage percentage
        usagePercentage = if (monthlyBudget > 0) {
            ((totalExpenses / monthlyBudget) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }
        
        // Pre-fill the budget input with current value
        if (monthlyBudget > 0) {
            budgetInput.setText(monthlyBudget.toString())
        }
        
        // Update UI with loaded data
        updateBudgetUI()
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            val budgetText = budgetInput.text.toString()
            
            if (budgetText.isBlank()) {
                Toast.makeText(requireContext(), "Please enter a budget amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            try {
                val newBudget = budgetText.toDouble()
                
                if (newBudget <= 0) {
                    Toast.makeText(requireContext(), "Budget must be greater than zero", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                // Save the new budget value
                PreferenceManager.getInstance(requireContext()).setMonthlyBudget(newBudget)
                
                // Update local variable and UI
                monthlyBudget = newBudget
                remainingBudget = monthlyBudget - totalExpenses
                
                // Recalculate percentage
                usagePercentage = if (monthlyBudget > 0) {
                    ((totalExpenses / monthlyBudget) * 100).toInt().coerceIn(0, 100)
                } else {
                    0
                }
                
                updateBudgetUI()
                
                Toast.makeText(requireContext(), "Budget saved successfully", Toast.LENGTH_SHORT).show()
            } catch (e: NumberFormatException) {
                Toast.makeText(requireContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateBudgetUI() {
        // Update budget values
        monthlyBudgetValue.text = CurrencyFormatter.format(monthlyBudget)
        totalExpensesValue.text = CurrencyFormatter.format(totalExpenses)
        remainingBudgetValue.text = CurrencyFormatter.format(remainingBudget)
        
        // Update progress indicator and text
        progressIndicator.progress = usagePercentage  // Update to use renamed variable
        progressPercentageText.text = "$usagePercentage%"
        usageStatusText.text = "You've used $usagePercentage% of your budget"
        
        // Update colors based on usage percentage
        val progressColor = when {
            usagePercentage < 70 -> resources.getColor(android.R.color.holo_green_dark, null) // Green
            usagePercentage in 70..90 -> resources.getColor(android.R.color.holo_orange_light, null) // Yellow
            else -> resources.getColor(android.R.color.holo_red_light, null) // Red/Coral
        }
        
        progressIndicator.setIndicatorColor(progressColor)  // Update to use renamed variable
        progressPercentageText.setTextColor(progressColor)
        
        // Update alert message
        when {
            usagePercentage < 70 -> {
                alertIcon.setColorFilter(resources.getColor(android.R.color.holo_green_dark, null))
                alertMessage.text = "Great job! You're spending wisely."
            }
            usagePercentage in 70..90 -> {
                alertIcon.setColorFilter(resources.getColor(android.R.color.holo_orange_light, null))
                alertMessage.text = "Be careful! You're approaching your budget limit."
            }
            else -> {
                alertIcon.setColorFilter(resources.getColor(android.R.color.holo_red_light, null))
                alertMessage.text = "⚠️ Warning: You're about to exceed your budget!"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
