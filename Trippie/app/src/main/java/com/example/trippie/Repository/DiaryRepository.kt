package com.example.trippie.Repository

import com.example.trippie.Diaries.Diary
import com.example.trippie.RoomDataBase.DiaryDatabase

/**
 * Repository class for managing diary entries in the application.
 */
class DiaryRepository(private val db: DiaryDatabase) {

    // Inserts a new diary entry into the database.
    suspend fun insertDiary(diary: Diary) = db.getDiaryDao().insertDiary(diary)

    //Deletes a diary entry from the database.
    suspend fun deleteDiary(diary: Diary) = db.getDiaryDao().deleteDiary(diary)

    //Updates an existing diary entry in the database.
    suspend fun updateDiary(diary: Diary) = db.getDiaryDao().updateDiary(diary)

    //Retrieves all diary entries from the database.
    fun getAllDiaries() = db.getDiaryDao().getAllDiaries()

    //Searches for diary entries that match a specific query.
    fun searchDiary(query: String?) = db.getDiaryDao().searchDiary(query)

}