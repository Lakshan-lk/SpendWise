package com.example.spendwise

import android.os.Parcel
import android.os.Parcelable
import java.util.Date

data class TransactionData(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val date: Date = Date(),
    val category: String? = "",
    val description: String? = "",
    val type: TransactionType = if (amount >= 0) TransactionType.INCOME else TransactionType.EXPENSE
) : Parcelable {

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        title = parcel.readString() ?: "",
        amount = parcel.readDouble(),
        date = Date(parcel.readLong()),
        category = parcel.readString(),
        description = parcel.readString(),
        type = TransactionType.valueOf(parcel.readString() ?: TransactionType.EXPENSE.name)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeDouble(amount)
        parcel.writeLong(date.time)
        parcel.writeString(category)
        parcel.writeString(description)
        parcel.writeString(type.name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TransactionData> {
        override fun createFromParcel(parcel: Parcel): TransactionData {
            return TransactionData(parcel)
        }

        override fun newArray(size: Int): Array<TransactionData?> {
            return arrayOfNulls(size)
        }
    }
}

enum class TransactionType {
    INCOME, EXPENSE
}
