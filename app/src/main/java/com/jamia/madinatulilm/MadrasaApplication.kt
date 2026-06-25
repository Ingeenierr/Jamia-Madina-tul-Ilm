package com.jamia.madinatulilm

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MadrasaApplication : Application() {
    companion object {
        private var isPersistenceSet = false
    }

    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            if (!isPersistenceSet) {
                Firebase.database.setPersistenceEnabled(true)
                isPersistenceSet = true
            }
        } catch (e: Exception) {
            android.util.Log.e("MadrasaApplication", "Firebase initialization failed", e)
        }
    }
}
