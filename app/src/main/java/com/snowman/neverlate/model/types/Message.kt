package com.snowman.neverlate.model.types

data class Message(
    val senderUid: String = "",
    val receiverUid: String = "",
    val messageText: String = "",
    val timestamp: Long = System.currentTimeMillis() // timestamp for when the message was sent mayb
)
