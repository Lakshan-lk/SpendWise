package com.example.spendwise

    import android.content.Intent
    import android.os.Bundle
    import android.widget.CheckBox
    import android.widget.TextView
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import com.google.android.material.button.MaterialButton
    import com.google.android.material.textfield.TextInputEditText

    class login : AppCompatActivity() {

        private lateinit var etUsername: TextInputEditText
        private lateinit var etPassword: TextInputEditText
        private lateinit var cbRememberMe: CheckBox
        private lateinit var btnLogin: MaterialButton
        private lateinit var tvRegister: TextView

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_login)

            // Initialize views
            etUsername = findViewById(R.id.et_username)
            etPassword = findViewById(R.id.et_password)
            cbRememberMe = findViewById(R.id.cb_remember_me)
            btnLogin = findViewById(R.id.btn_login)
            tvRegister = findViewById(R.id.tv_register)

            // Load saved credentials if remember me was checked
            loadSavedCredentials()

            // Setup listeners
            btnLogin.setOnClickListener {
                loginUser()
            }

            tvRegister.setOnClickListener {
                // Navigate to registration screen
                startActivity(Intent(this, RegisterActivity::class.java))
            }
        }

        private fun loadSavedCredentials() {
            val prefs = PreferenceManager.getInstance(this)

            // If remember me was checked and user is logged in
            if (prefs.getRememberMe() && prefs.isLoggedIn()) {
                etUsername.setText(prefs.getUserName())
                etPassword.setText(prefs.getUserPassword())
                cbRememberMe.isChecked = true
            }
        }

        private fun loginUser() {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val rememberMe = cbRememberMe.isChecked

            // Validate inputs
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
                return
            }

            // Check credentials
            val prefs = PreferenceManager.getInstance(this)
            val savedPassword = prefs.getUserPassword()

            if (savedPassword != null && password == savedPassword && username == prefs.getUserName()) {
                // Save login state and preferences
                prefs.setLoggedIn(true)
                prefs.setRememberMe(rememberMe)

                // Navigate to main screen
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }
    }