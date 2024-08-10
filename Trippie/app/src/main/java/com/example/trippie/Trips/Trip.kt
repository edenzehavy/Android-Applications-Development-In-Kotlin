package com.example.trippie.Trips
import android.os.Parcel
import android.os.Parcelable

/**
 * Represents a trip with all its details.
 */
data class Trip(
    val tripID: String = "",
    val userID: String = "",
    val country: String = "",
    val city: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val activities: String = "",
    val hotelDetails: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readString() as String?,
        parcel.readString()?: "",
        parcel.readString()?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(tripID)
        parcel.writeString(userID)
        parcel.writeString(country)
        parcel.writeString(city)
        parcel.writeString(startDate)
        parcel.writeString(endDate)
        parcel.writeString(description)
        parcel.writeString(imageUrl?: "")
        parcel.writeString(activities)
        parcel.writeString(hotelDetails)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Trip> {
        override fun createFromParcel(parcel: Parcel): Trip {
            return Trip(parcel)
        }

        override fun newArray(size: Int): Array<Trip?> {
            return arrayOfNulls(size)
        }
    }
}
