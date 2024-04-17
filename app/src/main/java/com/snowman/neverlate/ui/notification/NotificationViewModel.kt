package com.snowman.neverlate.ui.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.snowman.neverlate.model.types.Message

class NotificationViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is notification Fragment"
    }
    val text: LiveData<String> = _text

    val notificationsList = mutableListOf<Message>()

    init {
        notificationsList.add(
            Message(
                senderUid = "senderUid1",
                receiverUid = "receiverUid1",
                messageText = "Hello!",
                timestamp = System.currentTimeMillis() - 10000 // Subtracting time to create mock timestamps
            )
        )
        notificationsList.add(
            Message(
                senderUid = "senderUid2",
                receiverUid = "receiverUid2",
                messageText = "How are you?",
                timestamp = System.currentTimeMillis() - 20000
            )
        )
        notificationsList.add(
            Message(
                senderUid = "senderUid3",
                receiverUid = "receiverUid3",
                messageText = "Good morning!",
                timestamp = System.currentTimeMillis() - 30000
            )
        )
    }
}