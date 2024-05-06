package com.snowman.neverlate.ui.notification

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.ListItemNotificationBinding
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.types.Message
import com.snowman.neverlate.util.TimeUtil

class NotificationViewHolder(
    private val binding: ListItemNotificationBinding,
) : RecyclerView.ViewHolder(binding.root) {

    private val firebaseManager = FirebaseManager.getInstance()

    fun bind(message: Message) {
        firebaseManager.getUserDataForId(message.senderUid) { user ->
            if (user != null) {
                binding.senderTV.text = user.displayName
                binding.msgTV.text = message.messageText
                binding.timeTV.text = TimeUtil.convertMillisToDateTime(message.timestamp)
                Glide.with(binding.senderIV)
                    .load(user.photoURL)
                    .circleCrop()
                    .error(R.mipmap.ic_launcher_round)
                    .into(binding.senderIV)
            } else {
                Log.e("NotificationsAdapter", "User data is null for sender ID ${message.senderUid}")
            }
        }
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