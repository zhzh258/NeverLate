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
            "photoURL" to firebaseUser.photoUrl.toString()
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
                    val user = document.toObject(User::class.java)
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
                    usersList.add(user)
                }
                callback(usersList, null)
            }
            .addOnFailureListener { exception ->
                callback(null, exception)
            }
    }

    fun sendFriendRequest(
        currentUser: FirebaseUser,
        user: IUser,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val otherUserRef = db.collection("users").document(user.userId)
        val friendRequestsRef = otherUserRef.collection("friendRequests")

        friendRequestsRef.document(currentUser.uid).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    onFailure("You already sent a friend request")
                } else {
                    val newFriendRequest = hashMapOf(
                        "userId" to currentUser.uid,
                        "displayName" to currentUser.displayName,
                        "email" to currentUser.email,
                        "photoURL" to currentUser.photoUrl
                    )
                    friendRequestsRef.document(currentUser.uid).set(newFriendRequest)
                        .addOnSuccessListener {
                            Log.i(TAG, "Friend request created successfully")
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error creating friend request: $e")
                            onFailure("Error creating friend request")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting friend request document: $e")
                onFailure("Error creating friend request")
            }
    }

    fun getFriendRequests(currentUserId: String, callback: (List<User>?, Exception?) -> Unit) {
        val currentUserRef = db.collection("users").document(currentUserId)
        val friendRequestsRef = currentUserRef.collection("friendRequests")

        friendRequestsRef.get()
            .addOnSuccessListener { querySnapshot ->
                val usersList = mutableListOf<User>()
                for (document in querySnapshot) {
                    val user = document.toObject(User::class.java)
                    usersList.add(user)
                }
                callback(usersList, null)
            }
            .addOnFailureListener { exception ->
                callback(null, exception)
            }
    }

    fun removeFriendRequest(currentUserId: String, friendUserId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val currentUserRef = db.collection("users").document(currentUserId)
        val friendRequestsRef = currentUserRef.collection("friendRequests")

        friendRequestsRef.document(friendUserId)
            .delete()
            .addOnSuccessListener {
                onSuccess() // idk if we want to do anything on success
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}