package com.snowman.neverlate.model.types

data class Message(
    val messageId: String = "",
    val senderUid: String = "",
    val messageText: String = "",
    val timestamp: Long = System.currentTimeMillis() // timestamp for when the message was sent
)
