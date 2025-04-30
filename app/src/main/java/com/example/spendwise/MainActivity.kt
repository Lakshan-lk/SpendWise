package com.example.spendwise

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize database and preferences
        initDatabase()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        loadFragment(HomeFragment())

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_transactions -> {
                    loadFragment(TransactionsFragment())
                    true
                }
                R.id.nav_analysis -> {
                    loadFragment(AnalysisFragment())
                    true
                }
                R.id.nav_budget -> {
                    loadFragment(BudgetFragment())
                    true
                }
                R.id.nav_settings -> {
                    // Choose either SettingsFragment or SettingsPreferenceFragment
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun initDatabase() {
        // Initialize repositories - this ensures SharedPreferences and storage files are created
        val transactionRepository = TransactionRepository(applicationContext)

        // Check if it's first run to populate with sample data
        val prefs = PreferenceManager.getInstance(applicationContext)
        if (prefs.isFirstRun()) {
            // Add sample data if needed
            prefs.setFirstRun(false)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}