package com.example.spendwise

            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import android.widget.ImageButton
            import android.widget.TextView
            import androidx.core.content.ContextCompat
            import androidx.recyclerview.widget.RecyclerView
            import com.example.spendwise.utils.CurrencyFormatter
            import java.text.SimpleDateFormat
            import java.util.Locale

            class TransactionsAdapter(
                private val transactions: List<TransactionData>,
                private val listener: OnTransactionClickListener? = null
            ) : RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

                interface OnTransactionClickListener {
                    fun onTransactionClick(transaction: TransactionData, position: Int)
                    fun onTransactionEdit(transaction: TransactionData, position: Int)
                    fun onTransactionDelete(transaction: TransactionData, position: Int)
                }

                inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                    val tvTitle: TextView = itemView.findViewById(R.id.tv_transaction_title)
                    val tvAmount: TextView = itemView.findViewById(R.id.tv_transaction_amount)
                    val tvDate: TextView = itemView.findViewById(R.id.tv_transaction_date)
                    val tvCategory: TextView = itemView.findViewById(R.id.tv_transaction_category)
                    val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit_transaction)
                    val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_transaction)

                    init {
                        itemView.setOnClickListener {
                            val position = adapterPosition
                            if (position != RecyclerView.NO_POSITION) {
                                listener?.onTransactionClick(transactions[position], position)
                            }
                        }

                        btnEdit.setOnClickListener {
                            val position = adapterPosition
                            if (position != RecyclerView.NO_POSITION) {
                                listener?.onTransactionEdit(transactions[position], position)
                            }
                        }

                        btnDelete.setOnClickListener {
                            val position = adapterPosition
                            if (position != RecyclerView.NO_POSITION) {
                                listener?.onTransactionDelete(transactions[position], position)
                            }
                        }
                    }
                }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_transaction, parent, false)
                    return TransactionViewHolder(view)
                }

                override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
                    val transaction = transactions[position]

                    holder.tvTitle.text = transaction.title
                    holder.tvAmount.text = CurrencyFormatter.format(transaction.amount)

                    // Format date
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    holder.tvDate.text = dateFormat.format(transaction.date)

                    // Set category
                    holder.tvCategory.text = transaction.category ?: "Uncategorized"

                    // Style based on transaction type
                    val context = holder.itemView.context
                    if (transaction.type == TransactionType.INCOME) {
                        holder.tvAmount.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
                    } else {
                        holder.tvAmount.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
                    }

                    // Show/hide action buttons based on whether a listener is provided
                    val buttonsVisibility = if (listener == null) View.GONE else View.VISIBLE
                    holder.btnEdit.visibility = buttonsVisibility
                    holder.btnDelete.visibility = buttonsVisibility
                }

                override fun getItemCount(): Int = transactions.size
            }