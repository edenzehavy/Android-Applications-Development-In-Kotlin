# Trippie

## Table of Contents
* [General Info](#general-info)
* [Features](#features)
* [App Structure](#app-structure)
* [Setup](#setup)
* [Libraries Used](#libraries-used)
* [Collaborators](#collaborators)

## General Info
Trippie is an Android application designed for anyone who loves to travel and is looking for a platform to organize and track their trips and related information.

## Features
* **Trip Management:** Manage a list of trips, including adding, editing, and deleting trips.
* **Expense Management:** Track and manage expenses for each trip.
* **Packing List:** Maintain a checklist of items for upcoming trips.
* **Travel Diary:** Write and manage journal entries for each trip.
* **Authentication:** Users can register, log in, and manage their sessions securely.

## App Structure
* **Loading Screen:** An animated screen that displays while the app is loading. Redirects to the login screen or the trip list if the user is already logged in.
* **Login and Registration:** Allows users to log in with an email and password or register for a new account.
* **Trip List:** Displays a list of trips created by the user. Users can add new trips, view trip details, edit existing trips, or delete trips.
* **Expense List:** Manages a list of expenses associated with a specific trip. Users can add, edit, or delete expenses.
* **Packing List:** Allows users to manage items they need to pack for a trip.
* **Diaries:** Users can create, edit, and delete journal entries related to their trips.

## Setup
To run the Trippie app, follow these steps:

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/edenzehavy/Android-Applications-Development-In-Kotlin.git
   cd Trippie
2. ## Open the Project in Android Studio:
Open the project folder in Android Studio and allow it to sync and build.
3. ## Run the Application:
Connect an Android device or start an emulator, then run the app from Android Studio.

## Libraries Used
- **Firebase Auth:** Used for user authentication.
- **Firebase Firestore:** Remote database for storing trip, expense, and packing list data.
- **Room:** Local database for storing journal entries.
- **Navigation Component:** Manages navigation between different screens.
- **Google Play Services Location:** Retrieves the current location of the device and integrates with maps.
- **Coroutines & Dispatchers:** Manages asynchronous operations for smoother UI performance.
- **Glide:** Efficiently loads and caches images.

## Collaborators
- **Eden Zehavy**
- **Rotem Haim**
