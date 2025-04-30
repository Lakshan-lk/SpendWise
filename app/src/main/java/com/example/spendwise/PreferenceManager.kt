package com.example.spendwise

    import android.content.Context
    import android.content.SharedPreferences

    class PreferenceManager private constructor(context: Context) {

        companion object {
            private const val PREFS_NAME = "coinomy_preferences"
            private const val KEY_FIRST_RUN = "is_first_run"
            private const val PREF_IS_LOGGED_IN = "pref_is_logged_in"
            private const val PREF_USER_PASSWORD = "pref_user_password"
            private const val PREF_REMEMBER_ME = "pref_remember_me"

            @Volatile
            private var instance: PreferenceManager? = null

            fun getInstance(context: Context): PreferenceManager {
                return instance ?: synchronized(this) {
                    instance ?: PreferenceManager(context.applicationContext).also {
                        instance = it
                    }
                }
            }
        }

        private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Check if this is the first run of the app
        fun isFirstRun(): Boolean {
            return prefs.getBoolean(KEY_FIRST_RUN, true)
        }

        // Set first run status
        fun setFirstRun(isFirstRun: Boolean) {
            prefs.edit().putBoolean(KEY_FIRST_RUN, isFirstRun).apply()
        }

        // Get user name
        fun getUserName(): String {
            return prefs.getString("user_name", "User") ?: "User"
        }

        // Save user name
        fun setUserName(name: String) {
            prefs.edit().putString("user_name", name).apply()
        }

        // Get monthly budget
        fun getMonthlyBudget(): Double {
            return prefs.getFloat("monthly_budget", 2500f).toDouble()
        }

        // Set monthly budget
        fun setMonthlyBudget(budget: Double) {
            prefs.edit().putFloat("monthly_budget", budget.toFloat()).apply()
        }

        // Login related methods
        fun isLoggedIn(): Boolean {
            return prefs.getBoolean(PREF_IS_LOGGED_IN, false)
        }

        fun setLoggedIn(isLoggedIn: Boolean) {
            prefs.edit().putBoolean(PREF_IS_LOGGED_IN, isLoggedIn).apply()
        }

        fun getUserPassword(): String? {
            return prefs.getString(PREF_USER_PASSWORD, null)
        }

        fun setUserPassword(password: String) {
            prefs.edit().putString(PREF_USER_PASSWORD, password).apply()
        }

        fun getRememberMe(): Boolean {
            return prefs.getBoolean(PREF_REMEMBER_ME, false)
        }

        fun setRememberMe(rememberMe: Boolean) {
            prefs.edit().putBoolean(PREF_REMEMBER_ME, rememberMe).apply()
        }
    }