package com.example.trippie.Package
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.example.trippie.R

/**
 * Represents a packageItem
 */
data class PackageItem(
    val packageItemID: String = "",
    val userID: String = "",
    val description: String = "",
    val count: Int = 0,
    var isChecked: Int = 0
) : Parcelable {

    //Constructor to create an instance from a Parcel, allowing easy construction from passed data.
    constructor(parcel: Parcel) : this(
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readInt()?: 0,
        parcel.readInt()?: 0
    )

    //Writes the object's properties to the specified Parcel.
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(packageItemID)
        parcel.writeString(userID)
        parcel.writeString(description)
        parcel.writeInt(count)
        parcel.writeInt(isChecked)
    }

    override fun describeContents(): Int {
        return 0
    }

    // Determines the appropriate image based on the isChecked status.
    fun checkedImage(): Int {
        return if (isChecked == 1) R.drawable.check_icon else R.drawable.uncheck_icon
    }
    companion object CREATOR : Parcelable.Creator<PackageItem> {
        override fun createFromParcel(parcel: Parcel): PackageItem {
            return PackageItem(parcel)
        }

        override fun newArray(size: Int): Array<PackageItem?> {
            return arrayOfNulls(size)
        }
    }
}
