package com.snowman.neverlate.ui.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.snowman.neverlate.databinding.ListItemNotificationBinding
import com.snowman.neverlate.model.types.Message

class NotificationViewHolder(
    private val binding: ListItemNotificationBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(message: Message) {
        binding.senderTV.text = message.senderUid // should be replaced w actual info later but lazy
        binding.msgTV.text = message.messageText
        binding.timeTV.text = message.timestamp.toString() // will need to convert this time too lol
    }
}

class NotificationsAdapter(
    private var messages: MutableList<Message>
) : RecyclerView.Adapter<NotificationViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemNotificationBinding.inflate(inflater, parent, false)
        return NotificationViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = messages[position]
        holder.bind(notification)
    }
}