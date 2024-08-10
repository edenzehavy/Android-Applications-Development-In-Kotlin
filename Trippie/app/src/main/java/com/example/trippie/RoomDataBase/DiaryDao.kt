package com.example.trippie.RoomDataBase
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trippie.Diaries.Diary

@Dao
interface DiaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiary(diary: Diary)

    @Update
    suspend fun updateDiary(diary: Diary)

    @Delete
    suspend fun deleteDiary(diary: Diary)

    @Query("SELECT * FROM DIARIES ORDER BY diaryID DESC")
    fun getAllDiaries(): LiveData<List<Diary>>

    @Query ("SELECT * FROM DIARIES WHERE title LIKE :query OR content LIKE :query")
    fun searchDiary(query: String?): LiveData<List<Diary>>

}