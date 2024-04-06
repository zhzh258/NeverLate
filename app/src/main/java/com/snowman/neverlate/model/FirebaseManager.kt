package com.snowman.neverlate.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.snowman.neverlate.model.types.IUser
import com.snowman.neverlate.model.types.User

class FirebaseManager {
    private val TAG = "Firebase Manager"
    val db = FirebaseFirestore.getInstance()
    val auth = Firebase.auth
    private val usersCollection = db.collection("users")

    companion object {
        @Volatile
        private var instance: FirebaseManager? = null

        fun getInstance(): FirebaseManager {
            return instance ?: synchronized(this) {
                instance ?: FirebaseManager().also { instance = it }
            }
        }
    }

    fun getFirestore(): FirebaseFirestore {
        return db
    }

    fun firebaseAuth(): FirebaseAuth {
        return auth
    }

    fun saveUserDataToFirestore(firebaseUser: FirebaseUser) {

        val user = hashMapOf(
            "userId" to firebaseUser.uid,
            "email" to firebaseUser.email,
            "displayName" to firebaseUser.displayName,
            "photoURL" to firebaseUser.photoUrl.toString() // Convert PhotoUrl to String
        )

        val userRef = db.collection("users").document(firebaseUser.uid)
        userRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    Log.d(TAG, "User already exists in Firestore, skipping save operation")
                } else {
                    userRef.set(user)
                        .addOnSuccessListener {
                            Log.d(TAG, "User data saved successfully to Firestore")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error saving user data to Firestore: $e")
                        }
                }
            }
    }

    fun loadUserData(userId: String, callback: (IUser?) -> Unit) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Parse user data from document
                    val user = User(
                        id = document.getString("id") ?: "",
                        phoneNumber = document.getLong("phoneNumber") ?: 0L,
                        firstName = document.getString("firstName") ?: "",
                        lastName = document.getString("lastName") ?: "",
                        displayName = document.getString("displayName") ?: "",
                        photoURL = (document.get("photoURL") ?: "") as String,
                        email = document.getString("email") ?: "",
                        passwordHash = document.getString("passwordHash") ?: "",
                        friends = document.get("friends") as? List<String> ?: emptyList(),
                        totalLateTime = document.getLong("totalLateTime") ?: 0L,
                        totalEarlyTime = document.getLong("totalEarlyTime") ?: 0L
                    )
                    callback(user)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                callback(null)
                Log.e("FirebaseManager", "Failed to retrieve document")
            }
    }

    fun searchUsersByEmail(email: String, callback: (List<User>?, Exception?) -> Unit) {
        usersCollection.whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val usersList = mutableListOf<User>()
                for (document in querySnapshot) {
                    val user = document.toObject(User::class.java)

                    val status = document.getString("status") ?: "Unknown Status"
                    val email1 = document.getString("email") ?: "Unknown Email"
                    val photoURL = document.getString("photoURL") ?: "No Photo"

                    Log.i(TAG, "$status $email1 $photoURL")

                    Log.i(TAG, user.displayName + user.email + user.status + user.photoURL)
                    usersList.add(user)
                }
                callback(usersList, null)
            }
            .addOnFailureListener { exception ->
                callback(null, exception)
            }
    }
}