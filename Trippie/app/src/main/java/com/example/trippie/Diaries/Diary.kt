package com.example.trippie.Diaries

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "Diaries")
@Parcelize
/**
 * Represents a diary entry within the application.
 * This class's objects are saved in the room local database.
 */
data class Diary(
    @PrimaryKey(autoGenerate = true)
    val diaryID: Int,
    val title: String,
    val location: String,
    val content: String?,
): Parcelable

