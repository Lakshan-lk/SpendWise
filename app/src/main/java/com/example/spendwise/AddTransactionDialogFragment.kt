package com.example.spendwise

                                    import android.os.Bundle
                                    import android.view.LayoutInflater
                                    import android.view.View
                                    import android.view.ViewGroup
                                    import android.widget.Button
                                    import android.widget.EditText
                                    import android.widget.RadioButton
                                    import android.widget.RadioGroup
                                    import android.widget.TextView
                                    import androidx.fragment.app.DialogFragment

class AddTransactionDialogFragment : DialogFragment() {

                                        interface TransactionDialogListener {
                                            fun onTransactionAdded(transaction: TransactionData)
                                        }

                                        private lateinit var tvDialogTitle: TextView
                                        private lateinit var rgTransactionType: RadioGroup
                                        private lateinit var rbIncome: RadioButton
                                        private lateinit var rbExpense: RadioButton
                                        private lateinit var etTitle: EditText
                                        private lateinit var etAmount: EditText
                                        private lateinit var etCategory: EditText
                                        private lateinit var etDescription: EditText
                                        private lateinit var btnCancel: Button
                                        private lateinit var btnSave: Button

                                        private var transactionType = TransactionType.EXPENSE
                                        private var editingTransaction: TransactionData? = null
                                        private var listener: TransactionDialogListener? = null

                                        override fun onCreate(savedInstanceState: Bundle?) {
                                            super.onCreate(savedInstanceState)
                                           setStyle(STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_Material3_Dialog_Alert)

                                            arguments?.let {
                                                transactionType = TransactionType.valueOf(it.getString(ARG_TYPE, TransactionType.EXPENSE.name))
                                                if (it.containsKey(ARG_TRANSACTION)) {
                                                    editingTransaction = it.getParcelable(ARG_TRANSACTION)
                                                }
                                            }

                                            // Find the parent fragment that implements the listener
                                            parentFragment?.let {
                                                if (it is TransactionDialogListener) {
                                                    listener = it
                                                }
                                            }
                                        }

                                        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
                                            return inflater.inflate(R.layout.dialog_add_transaction, container, false)
                                        }

                                        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                                            super.onViewCreated(view, savedInstanceState)

                                            initViews(view)
                                            setupInitialValues()
                                            setupListeners()
                                        }

                                        private fun initViews(view: View) {
                                            tvDialogTitle = view.findViewById(R.id.tv_dialog_title)
                                            rgTransactionType = view.findViewById(R.id.rg_transaction_type)
                                            rbIncome = view.findViewById(R.id.rb_income)
                                            rbExpense = view.findViewById(R.id.rb_expense)
                                            etTitle = view.findViewById(R.id.et_transaction_title)
                                            etAmount = view.findViewById(R.id.et_amount)
                                            etCategory = view.findViewById(R.id.et_category)
                                            etDescription = view.findViewById(R.id.et_description)
                                            btnCancel = view.findViewById(R.id.btn_cancel)
                                            btnSave = view.findViewById(R.id.btn_save)
                                        }

                                        private fun setupInitialValues() {
                                            // Set title based on whether we're editing or adding
                                            tvDialogTitle.text = if (editingTransaction != null) "Edit Transaction" else "Add Transaction"

                                            // Set transaction type
                                            if (transactionType == TransactionType.INCOME) {
                                                rbIncome.isChecked = true
                                            } else {
                                                rbExpense.isChecked = true
                                            }

                                            // Populate fields if editing an existing transaction
                                            editingTransaction?.let { transaction ->
                                                etTitle.setText(transaction.title)

                                                // Display absolute value for amount
                                                val displayAmount = Math.abs(transaction.amount)
                                                etAmount.setText(displayAmount.toString())

                                                etCategory.setText(transaction.category)
                                                etDescription.setText(transaction.description)

                                                // Set radio button based on transaction type
                                                if (transaction.type == TransactionType.INCOME) {
                                                    rbIncome.isChecked = true
                                                } else {
                                                    rbExpense.isChecked = true
                                                }
                                            }
                                        }

                                        private fun setupListeners() {
                                            rgTransactionType.setOnCheckedChangeListener { _, checkedId ->
                                                transactionType = if (checkedId == R.id.rb_income) {
                                                    TransactionType.INCOME
                                                } else {
                                                    TransactionType.EXPENSE
                                                }
                                            }

                                            btnCancel.setOnClickListener {
                                                dismiss()
                                            }

                                            btnSave.setOnClickListener {
                                                if (validateInputs()) {
                                                    saveTransaction()
                                                }
                                            }
                                        }

                                        private fun validateInputs(): Boolean {
                                            val title = etTitle.text.toString().trim()
                                            val amountText = etAmount.text.toString().trim()

                                            if (title.isEmpty()) {
                                                etTitle.error = "Title cannot be empty"
                                                return false
                                            }

                                            if (amountText.isEmpty()) {
                                                etAmount.error = "Amount cannot be empty"
                                                return false
                                            }

                                            return true
                                        }

                                        private fun saveTransaction() {
                                            val title = etTitle.text.toString().trim()
                                            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
                                            val category = etCategory.text.toString().trim()
                                            val description = etDescription.text.toString().trim()

                                            // Adjust amount based on transaction type
                                            val signedAmount = if (transactionType == TransactionType.EXPENSE) -Math.abs(amount) else Math.abs(amount)

                                            val transaction = if (editingTransaction != null) {
                                                // Update existing transaction
                                                editingTransaction!!.copy(
                                                    title = title,
                                                    amount = signedAmount,
                                                    category = category,
                                                    description = description,
                                                    type = transactionType
                                                )
                                            } else {
                                                // Create new transaction
                                                TransactionData(
                                                    title = title,
                                                    amount = signedAmount,
                                                    category = category,
                                                    description = description,
                                                    type = transactionType
                                                )
                                            }

                                            // Save transaction using repository
                                            if (editingTransaction != null) {
                                                // Update existing transaction
                                                TransactionRepository(requireContext()).updateTransaction(transaction)
                                            } else {
                                                // Add new transaction
                                                TransactionRepository(requireContext()).addTransaction(transaction)
                                            }

                                            // Notify listener
                                            listener?.onTransactionAdded(transaction)

                                            // Close dialog
                                            dismiss()
                                        }

                                        companion object {
                                            const val TAG = "AddTransactionDialog"
                                            private const val ARG_TYPE = "transaction_type"
                                            private const val ARG_TRANSACTION = "transaction"

                                            fun newInstance(type: TransactionType, transaction: TransactionData? = null): AddTransactionDialogFragment {
                                                return AddTransactionDialogFragment().apply {
                                                    arguments = Bundle().apply {
                                                        putString(ARG_TYPE, type.name)
                                                        transaction?.let {
                                                            putParcelable(ARG_TRANSACTION, it)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }