package com.snowman.neverlate.model

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.snowman.neverlate.model.types.IUser
import com.snowman.neverlate.model.types.User
import com.snowman.neverlate.model.types.IEvent
import com.snowman.neverlate.model.types.Event
import com.snowman.neverlate.model.types.Message
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class FirebaseManager {
    private val TAG = "Firebase Manager"
    val db = FirebaseFirestore.getInstance()
    val auth = Firebase.auth

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

    fun saveUserDataToFirestore(
        firebaseUser: FirebaseUser,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

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
                    onSuccess()
                } else {
                    userRef.set(user)
                        .addOnSuccessListener {
                            Log.d(TAG, "User data saved successfully to Firestore")
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error saving user data to Firestore: $e")
                            onFailure(e)
                        }
                }
            }
    }

    fun loadUserData(callback: (IUser?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val currentUserId = currentUser.uid
            db.collection("users").document(currentUserId)
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
    }

    fun getUserDataForId(userId: String, callback: (IUser?) -> Unit) {
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

    fun getUsersDataForIds(userIds: List<String>, callback: (List<User>) -> Unit) {
        val usersData = mutableListOf<User>()
        val usersCollectionRef = db.collection("users")

        for (userId in userIds) {
            usersCollectionRef.document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val user = document?.toObject(User::class.java)
                    user?.let {
                        usersData.add(user)
                        if (usersData.size == userIds.size) {
                            callback(usersData)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseManager", "Failed to retrieve user document for ID: $userId")
                    callback(usersData)
                }
        }
    }

    fun editUserProfile(
        updatedUserData: Map<String, Any>,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val currentUserId = currentUser.uid
            val userRef = db.collection("users").document(currentUserId)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userRef.update(updatedUserData)
                            .addOnSuccessListener {
                                Log.d(TAG, "User details updated successfully")
                                userRef.get()
                                    .addOnSuccessListener { updatedDocument ->
                                        if (updatedDocument.exists()) {
                                            val updatedUser =
                                                updatedDocument.toObject(User::class.java)
                                            onSuccess(updatedUser!!)
                                        } else {
                                            onFailure(Exception("Updated user data not found"))
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(TAG, "Error fetching updated user document: $e")
                                        onFailure(e)
                                    }
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error updating user details: $e")
                                onFailure(e)
                            }
                    } else {
                        Log.e(TAG, "User not found")
                        onFailure(Exception("User not found"))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error fetching user document: $e")
                    onFailure(e)
                }
        }
    }

    fun sendMessage(message: Message, onSuccess: () -> Unit) {
        val receiverMessagesRef =
            db.collection("users").document(message.receiverUid).collection("messages")

        val messageDocRef = receiverMessagesRef.document(message.messageId)
        messageDocRef.set(message)
            .addOnSuccessListener {
                Log.i(TAG, "Message sent successfully! :D")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error sending message: $e")
            }
    }

    fun messageListener(listener: (List<Message>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userMessagesRef = db.collection("users").document(userId).collection("messages")
            userMessagesRef.get()
                .addOnSuccessListener { snapshot ->

                    userMessagesRef.addSnapshotListener { snapshots, exception ->
                        if (exception != null) {
                            Log.e(TAG, "Error listening for messages: $exception")
                            return@addSnapshotListener
                        }

                        val messages = mutableListOf<Message>()
                        for (dc in snapshots!!.documentChanges) {
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    val message = dc.document.toObject(Message::class.java)
                                    if (!messages.contains(message)) {
                                        messages.add(message)
                                    }
                                }

                                else -> {
                                    // we are not doing modifications or deletions
                                    Log.i(TAG, "Message was removed or edited")
                                }
                            }
                        }
                        listener(messages)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting initial messages: $exception")
                }
        }
    }

    fun searchUsersByEmail(email: String, callback: (List<User>?, Exception?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").get()
                .addOnSuccessListener { querySnapshot ->
                    val usersList = mutableListOf<User>()
                    for (document in querySnapshot) {
                        val user = document.toObject(User::class.java)
                        if (user.email.startsWith(
                                email,
                                ignoreCase = true
                            ) && user.email != currentUser.email // lol not friends w self
                            && !user.friends.contains(currentUser.uid) // not already friends
                        ) {
                            usersList.add(user)
                        }
                    }
                    callback(usersList, null)
                }
                .addOnFailureListener { exception ->
                    callback(null, exception)
                }
        }
    }

    fun sendFriendRequest(
        user: IUser,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
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
                            // we want to check if the other user sent us a req already
                            val currentUserRef = db.collection("users").document(currentUser.uid)
                            currentUserRef.get()
                                .addOnSuccessListener { currentUserDocument ->
                                    val otherUserFriendRequests =
                                        currentUserDocument.get("friendRequests") as? MutableList<String>
                                            ?: mutableListOf()

                                    if (otherUserFriendRequests.contains(user.userId)) {
                                        onFailure("User already sent you a friend request, please accept it")
                                    } else {
                                        // add friend req
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
                                }
                                .addOnFailureListener { e ->
                                    onFailure("Error checking friend requests: ${e.message}")
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
    }

    fun getFriendRequests(callback: (List<User>?, Exception?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val currentUserId = currentUser.uid
            val currentUserRef = db.collection("users").document(currentUserId)

            currentUserRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val friendRequests = documentSnapshot.get("friendRequests") as? List<String>

                        if (friendRequests.isNullOrEmpty()) {
                            callback(emptyList(), null)
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
    }

    fun removeFriendRequest(
        friendUserId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val currentUserId = currentUser.uid
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
    }

    fun addFriend(
        friendId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val currentUserId = currentUser.uid
            val currentUserRef = db.collection("users").document(currentUserId)
            val friendUserRef = db.collection("users").document(friendId)

            currentUserRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val currentUser = documentSnapshot.toObject(User::class.java)
                        val currentFriends =
                            currentUser?.friends?.toMutableList() ?: mutableListOf()

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
                                                val friendFriends =
                                                    friendUser?.friends?.toMutableList()
                                                        ?: mutableListOf()

                                                if (!friendFriends.contains(currentUserId)) {
                                                    friendFriends.add(currentUserId)
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
    }

    fun fetchFriendsDataForCurrentUser(
        callback: (List<User>?, Exception?) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val currentUserId = currentUser.uid
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
                                        val friend =
                                            friendDocumentSnapshot.toObject(User::class.java)
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


    /**
     * @param active The active status of the event you want to fetch. 'null' to fetch all.
     * @param category The category of the event you want to fetch. 'null' or 'All' to fetch all.
     * @param callback The callback function that will be called using the List<IEvent> fetched as argument
     */
    fun fetchEventsDataForCurrentUser(
        active: Boolean?,
        category: String?,
        callback: (List<IEvent>?, Exception?) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val currentUserId = currentUser.uid
            Log.d(TAG, "currentUserId=${currentUserId}")
            Log.d(TAG, "fetchEventsDataForCurrentUser()... with currentUserId=${currentUserId}")
            val ref = when (category) {
                null, "All" -> {
                    db.collection("events").get()
                }

                "Dining", "Meeting", "Study", "Travel" -> {
                    db.collection("events").whereEqualTo("category", category).get()
                }

                else -> {
                    callback(
                        null,
                        Exception("category=$category is illegal. category must be All (null), Dining, Meeting, Study, or Travel")
                    )
                    return@fetchEventsDataForCurrentUser
                }
            }

            val eventsList = mutableListOf<IEvent>()
            ref.addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.d(TAG, "Query returned no results.")
                } else {
                    Log.d(TAG, "Processing ${result.size()} documents.")
                    for (document in result) {
                        val event = document.toObject(Event::class.java)
                        Log.d(TAG, "Processing event: ${event.id} ${event.name}")
                        if (active != null && event.active != active) { // if you fetch active=true or active=false
                            continue
                        }
                        val memberList = document.get("members") as List<*>
                        val isUserAMember = memberList.mapNotNull { it as? Map<*, *> }
                            .any { it["id"] == currentUserId }
                        if (isUserAMember) {
                            Log.d(TAG, "User is a member of the event: ${event.id}")
                            eventsList.add(event)
                        }
                    }
                    callback(eventsList, null)
                }
            }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Query failed with exception: $exception")
                    callback(null, exception)
                }

        }
    }

    fun fetchAverageArrivalTimeForCurrentUser(callback: (Double?, Exception?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val currentUserId = currentUser.uid
            Log.d(TAG, "currentUserId=$currentUserId")
            val eventsList = mutableListOf<IEvent>()
            db.collection("events")
                .whereEqualTo("active", false)
                .get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        Log.d(TAG, "Query returned no results.")
                        callback(0.0, null)
                    } else {
                        Log.d(TAG, "Processing ${result.size()} documents.")
                        val allArriveTimes = mutableListOf<Long>()
                        for (document in result) {
                            val event = document.toObject(Event::class.java)
                            Log.d(TAG, "Processing event: ${event.id}")
                            val memberList = document.get("members") as List<*>
                            memberList.mapNotNull { it as? Map<String, Any> }
                                .filter { it["id"] == currentUserId }
                                .forEach {
                                    allArriveTimes.add(it["arriveTime"] as Long)
                                    eventsList.add(event)
                                }
                        }

                        val averageArriveTime = if (allArriveTimes.isNotEmpty()) {
                            allArriveTimes.average()
                        } else {
                            null
                        }
                        Log.d(
                            TAG,
                            "Average arrive time for user $currentUserId is: $averageArriveTime"
                        )
                        callback(averageArriveTime, null)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Query failed with exception: $exception")
                    callback(null, exception)
                }
        } else {
            Log.d(TAG, "Current user not found")
            callback(null, Exception("Current user not found"))
        }
    }

    fun saveImageToStorage(
        imageUri: Uri,
        filename: String,
        callback: (String?) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val currentUserId = currentUser.uid
            val uploadName = "images/${currentUserId}_${filename}.jpg"
            val storageRef = FirebaseStorage.getInstance().reference.child(uploadName)
            val uploadTask: UploadTask = storageRef.putFile(imageUri)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    callback(imageUrl)
                }
            }.addOnFailureListener { e ->
                Log.i(TAG, "could not upload image $e")
                callback(null)
            }
        }
    }


}


