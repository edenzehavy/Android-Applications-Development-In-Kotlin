package com.example.trippie.Diaries

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.trippie.Repository.AuthRepository
import com.example.trippie.Repository.DiaryRepository
import com.example.trippie.Repository.TripsRepository
import com.example.trippie.Trips.TripViewModel


//Factory class for creating instances of DiaryViewModel.
class DiaryViewModelFactory( private val app: Application, private val authRepo: AuthRepository, private val diaryRepository: DiaryRepository) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DiaryViewModel(app, authRepo, diaryRepository) as T
        }
    }
