package com.snowman.neverlate.model

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseManager {
    // TODO: set up FireBase Realtime database for online storage

    private val TAG = "Firebase Manager"

    fun saveUserDataToFirestore(firebaseUser: FirebaseUser) {
        val db = FirebaseFirestore.getInstance()

        val user = hashMapOf(
            "userId" to firebaseUser.uid,
            "email" to firebaseUser.email,
            "displayName" to firebaseUser.displayName,
            "photoURL" to firebaseUser.photoUrl.toString() // Convert PhotoUrl to String
        )

        val userRef = db.collection("users").document(firebaseUser.uid)

        userRef.set(user)
            .addOnSuccessListener {
                Log.d(TAG, "User data saved successfully to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving user data to Firestore: $e")
            }
    }
}