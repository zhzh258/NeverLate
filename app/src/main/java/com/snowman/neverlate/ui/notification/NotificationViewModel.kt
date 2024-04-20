package com.snowman.neverlate.ui.notification

import androidx.lifecycle.ViewModel
import com.snowman.neverlate.model.types.Message

class NotificationViewModel : ViewModel() {
    var notificationsList = mutableListOf<Message>()

    fun updateMessages(messages: List<Message>) {
        notificationsList.addAll(messages)
    }
}