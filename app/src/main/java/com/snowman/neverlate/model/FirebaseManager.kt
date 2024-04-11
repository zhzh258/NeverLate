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

        otherUserRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val friendRequests =
                        documentSnapshot.get("friendRequests") as? MutableList<String>
                            ?: mutableListOf()

                    if (friendRequests.contains(currentUser.uid)) {
                        onFailure("You already sent a friend request")
                    } else {
                        friendRequests.add(currentUser.uid)

                        otherUserRef.update("friendRequests", friendRequests)
                            .addOnSuccessListener {
                                Log.i(TAG, "Friend request added successfully")
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error adding friend request: $e")
                                onFailure("Error adding friend request")
                            }
                    }
                } else {
                    onFailure("User not found")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting user document: $e")
                onFailure("Error sending friend request")
            }
    }

    fun getFriendRequests(currentUserId: String, callback: (List<User>?, Exception?) -> Unit) {
        val currentUserRef = db.collection("users").document(currentUserId)

        currentUserRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val friendRequests = documentSnapshot.get("friendRequests") as? List<String>

                    if (friendRequests.isNullOrEmpty()) {
                        callback(emptyList(), null) // No friend requests, return an empty list
                    } else {
                        val friendsList = mutableListOf<User>()
                        var friendCount = 0

                        for (friendId in friendRequests) {
                            val friendRef = db.collection("users").document(friendId)
                            friendRef.get()
                                .addOnSuccessListener { friendDocumentSnapshot ->
                                    if (friendDocumentSnapshot.exists()) {
                                        val friend =
                                            friendDocumentSnapshot.toObject(User::class.java)
                                        friend?.let {
                                            friendsList.add(it)
                                            friendCount++

                                            if (friendCount == friendRequests.size) {
                                                callback(friendsList, null)
                                            }
                                        }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    callback(null, exception)
                                }
                        }
                    }
                } else {
                    callback(null, Exception("User not found"))
                }
            }
            .addOnFailureListener { exception ->
                callback(null, exception)
            }
    }

    fun removeFriendRequest(
        currentUserId: String,
        friendUserId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUserRef = db.collection("users").document(currentUserId)

        currentUserRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val currentUser = documentSnapshot.toObject(User::class.java)
                    val friendRequests = currentUser?.friendRequests as MutableList

                    if (friendRequests.contains(friendUserId)) {
                        friendRequests.remove(friendUserId)

                        currentUserRef.update("friendRequests", friendRequests)
                            .addOnSuccessListener {
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                onFailure(e)
                            }
                    } else {
                        onFailure(Exception("Friend request ID not found"))
                    }
                } else {
                    onFailure(Exception("Current user not found"))
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun addFriend(
        userId: String,
        friendId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUserRef = db.collection("users").document(userId)
        val friendUserRef = db.collection("users").document(friendId)

        currentUserRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val currentUser = documentSnapshot.toObject(User::class.java)
                    val currentFriends = currentUser?.friends?.toMutableList() ?: mutableListOf()

                    if (!currentFriends.contains(friendId)) {
                        currentFriends.add(friendId)

                        currentUserRef.update("friends", currentFriends)
                            .addOnSuccessListener {
                                // Update the friend's friends list
                                friendUserRef.get()
                                    .addOnSuccessListener { friendDocumentSnapshot ->
                                        if (friendDocumentSnapshot.exists()) {
                                            val friendUser =
                                                friendDocumentSnapshot.toObject(User::class.java)
                                            val friendFriends = friendUser?.friends?.toMutableList()
                                                ?: mutableListOf()

                                            if (!friendFriends.contains(userId)) {
                                                friendFriends.add(userId)
                                                friendUserRef.update("friends", friendFriends)
                                                    .addOnSuccessListener {
                                                        onSuccess()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        onFailure(e)
                                                    }
                                            } else {
                                                onSuccess()
                                            }
                                        } else {
                                            onFailure(Exception("Friend user not found"))
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        onFailure(e)
                                    }
                            }
                            .addOnFailureListener { e ->
                                onFailure(e)
                            }
                    } else {
                        onSuccess()
                    }
                } else {
                    onFailure(Exception("Current user not found"))
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun fetchFriendsDataForCurrentUser(
        currentUserId: String,
        callback: (List<User>?, Exception?) -> Unit
    ) {
        val currentUserRef = db.collection("users").document(currentUserId)

        currentUserRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val currentUser = documentSnapshot.toObject(User::class.java)
                    val friendIds = currentUser?.friends ?: listOf()

                    val friendsList = mutableListOf<User>()
                    var friendCount = 0

                    for (friendId in friendIds) {
                        val friendRef = db.collection("users").document(friendId)
                        friendRef.get()
                            .addOnSuccessListener { friendDocumentSnapshot ->
                                if (friendDocumentSnapshot.exists()) {
                                    val friend = friendDocumentSnapshot.toObject(User::class.java)
                                    friend?.let {
                                        friendsList.add(it)
                                        friendCount++

                                        if (friendCount == friendIds.size) {
                                            callback(
                                                friendsList,
                                                null
                                            )
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                callback(null, e)
                                return@addOnFailureListener
                            }
                    }
                    callback(friendsList, null)
                } else {
                    callback(null, Exception("Current user not found"))
                }
            }
            .addOnFailureListener { e ->
                callback(null, e)
            }
    }
}