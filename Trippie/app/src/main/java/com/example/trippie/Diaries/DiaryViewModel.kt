package com.example.trippie.Diaries

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trippie.Repository.AuthRepository
import com.example.trippie.Repository.DiaryRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing UI-related data in a lifecycle
 */
class DiaryViewModel (app: Application, private val authRep: AuthRepository, private val diaryRepository: DiaryRepository): AndroidViewModel(app) {

    //Adds a diary entry to the repository.
    fun addDiary(diary: Diary) =
        viewModelScope.launch {
            diaryRepository.insertDiary(diary)
        }
    // Deletes a diary entry from the repository.
    fun deleteDiary(diary: Diary) =
        viewModelScope.launch {
            diaryRepository.deleteDiary(diary)
        }

    //Updates an existing diary entry in the repository.
    fun updateDiary(diary: Diary) =
        viewModelScope.launch {
            diaryRepository.updateDiary(diary)
        }

    //Retrieves all diaries from the repository.
    fun getAllDiaries() = diaryRepository.getAllDiaries()

    //Performs a search for diaries based on a query string.
    fun searchDiary(query: String?) =
        diaryRepository.searchDiary(query)

    //Signs out the current user
    fun signOut() {
        authRep.logout()
    }
}
