package com.example.trippie.Expenses
import android.os.Parcel
import android.os.Parcelable

/**
 * Data class representing an expense entry in the application.
 */
data class Expense(
    val expenseID: String = "",
    val userID: String? = null,
    val country: String = "",
    val city: String = "",
    val date: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val currency: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readDouble()?: 0.0,
        parcel.readString()?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(expenseID)
        parcel.writeString(userID)
        parcel.writeString(country)
        parcel.writeString(city)
        parcel.writeString(date)
        parcel.writeString(description)
        parcel.writeDouble(price)
        parcel.writeString(currency)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Expense> {
        override fun createFromParcel(parcel: Parcel): Expense {
            return Expense(parcel)
        }

        override fun newArray(size: Int): Array<Expense?> {
            return arrayOfNulls(size)
        }
    }
}
