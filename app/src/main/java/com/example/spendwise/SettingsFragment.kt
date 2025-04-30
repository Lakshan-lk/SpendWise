package com.example.spendwise

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.spendwise.databinding.DialogEditProfileBinding
import com.example.spendwise.databinding.FragmentSettingsBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize shared preferences
        sharedPreferences = requireActivity().getSharedPreferences("coinomy_prefs", Context.MODE_PRIVATE)
        
        // Set up preferences fragment
        childFragmentManager.beginTransaction()
            .replace(R.id.settings_container, SettingsPreferenceFragment())
            .commit()
        
        // Load user profile data
        loadUserProfile()
        
        // Set up edit profile button
        binding.editProfileButton.setOnClickListener {
            showEditProfileDialog()
        }
    }
    
    private fun loadUserProfile() {
        val name = sharedPreferences.getString("user_name", "User Name") ?: "User Name"
        val email = sharedPreferences.getString("user_email", "email@example.com") ?: "email@example.com"
        
        binding.profileName.text = name
        binding.profileEmail.text = email
    }
    
    private fun showEditProfileDialog() {
        val dialogBinding = DialogEditProfileBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
            
        // Pre-populate fields with existing data
        dialogBinding.nameInput.setText(sharedPreferences.getString("user_name", ""))
        dialogBinding.emailInput.setText(sharedPreferences.getString("user_email", ""))
        
        dialogBinding.cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        
        dialogBinding.saveButton.setOnClickListener {
            val name = dialogBinding.nameInput.text.toString().trim()
            val email = dialogBinding.emailInput.text.toString().trim()
            
            // Validate inputs
            var isValid = true
            
            if (name.isEmpty()) {
                dialogBinding.nameInputLayout.error = "Name cannot be empty"
                isValid = false
            } else {
                dialogBinding.nameInputLayout.error = null
            }
            
            if (email.isNotEmpty() && !isValidEmail(email)) {
                dialogBinding.emailInputLayout.error = "Please enter a valid email"
                isValid = false
            } else {
                dialogBinding.emailInputLayout.error = null
            }
            
            if (isValid) {
                // Save to SharedPreferences
                sharedPreferences.edit().apply {
                    putString("user_name", name)
                    putString("user_email", email)
                    apply()
                }
                
                // Update UI
                loadUserProfile()
                
                dialog.dismiss()
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
        }
        
        dialog.show()
    }
    
    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    class SettingsPreferenceFragment : PreferenceFragmentCompat() {
        private lateinit var sharedPreferences: SharedPreferences
        
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            sharedPreferences = requireActivity().getSharedPreferences("coinomy_prefs", Context.MODE_PRIVATE)
            
            // Set up preference click listeners
            setupBackupRestorePreferences()
            setupLogoutPreference()
        }
        
        private fun setupBackupRestorePreferences() {
            findPreference<Preference>("backup_data")?.setOnPreferenceClickListener {
                backupUserData()
                true
            }
            
            findPreference<Preference>("restore_data")?.setOnPreferenceClickListener {
                restoreUserData()
                true
            }
        }
        
        private fun setupLogoutPreference() {
            findPreference<Preference>("logout")?.setOnPreferenceClickListener {
                // Show confirmation dialog
                AlertDialog.Builder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes") { _, _ ->
                        // Clear login state
                        sharedPreferences.edit().putBoolean("is_logged_in", false).apply()
                        // Navigate to login (this would depend on your app navigation)
                        // For example: findNavController().navigate(R.id.action_settings_to_login)
                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("No", null)
                    .show()
                true
            }
        }
        
        private fun backupUserData() {
            try {
                val file = File(requireContext().filesDir, "coinomy_backup.txt")
                val fos = FileOutputStream(file)
                
                // Get all preferences to back up
                val userData = mapOf(
                    "user_name" to sharedPreferences.getString("user_name", ""),
                    "user_email" to sharedPreferences.getString("user_email", ""),
                    "currency" to PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .getString("currency", "LKR"),
                    "theme_mode" to PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .getBoolean("theme_mode", false).toString(),
                    "notifications" to PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .getBoolean("notifications", true).toString()
                )
                
                // Serialize to simple format
                val dataString = userData.entries.joinToString("\n") { "${it.key}=${it.value}" }
                fos.write(dataString.toByteArray())
                fos.close()
                
                Toast.makeText(context, "Data backed up successfully", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(context, "Error backing up data: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
        
        private fun restoreUserData() {
            try {
                val file = File(requireContext().filesDir, "coinomy_backup.txt")
                if (!file.exists()) {
                    Toast.makeText(context, "No backup found", Toast.LENGTH_SHORT).show()
                    return
                }
                
                val fis = FileInputStream(file)
                val size = fis.available()
                val buffer = ByteArray(size)
                fis.read(buffer)
                fis.close()
                
                val dataString = String(buffer)
                val dataMap = mutableMapOf<String, String>()
                
                // Parse the data
                dataString.split("\n").forEach { line ->
                    val parts = line.split("=", limit = 2)
                    if (parts.size == 2) {
                        dataMap[parts[0]] = parts[1]
                    }
                }
                
                // Restore to SharedPreferences
                sharedPreferences.edit().apply {
                    putString("user_name", dataMap["user_name"])
                    putString("user_email", dataMap["user_email"])
                    apply()
                }
                
                val prefEdit = PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
                prefEdit.putString("currency", dataMap["currency"])
                prefEdit.putBoolean("theme_mode", dataMap["theme_mode"]?.toBoolean() ?: false)
                prefEdit.putBoolean("notifications", dataMap["notifications"]?.toBoolean() ?: true)
                prefEdit.apply()
                
                // Update parent fragment UI
                (parentFragment as? SettingsFragment)?.loadUserProfile()
                
                Toast.makeText(context, "Data restored successfully", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(context, "Error restoring data: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}
