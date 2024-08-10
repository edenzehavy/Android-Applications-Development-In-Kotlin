package com.example.trippie.RoomDataBase

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.trippie.Diaries.Diary
import android.content.Context

@Database(entities = [Diary::class], version = 1)
abstract class DiaryDatabase: RoomDatabase() {
    abstract fun getDiaryDao(): DiaryDao

    companion object{
        @Volatile
        private var instance: DiaryDatabase? = null
        private  val LOCK = Any()

        operator fun invoke(context: Context) = instance ?:
        synchronized(LOCK){
            instance ?:
            createDatabase(context).also{
                instance = it
            }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                DiaryDatabase::class.java,
                "diary_db"
            ).build()
    }
}