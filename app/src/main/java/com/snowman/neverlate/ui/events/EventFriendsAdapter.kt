package com.snowman.neverlate.ui.events

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.ListItemAttendeeBinding
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.types.IUser
import com.snowman.neverlate.model.types.Message
import com.snowman.neverlate.model.types.User
import java.util.UUID

class EventFriendsViewHolder(
    private val binding: ListItemAttendeeBinding,
    private val context: Context
) : RecyclerView.ViewHolder(binding.root) {

    private val firebaseManager = FirebaseManager.getInstance()

    fun bind(user: IUser) {
        binding.friendTV.text = user.displayName
        Glide.with(binding.friendIV)
            .load(user.photoURL)
            .circleCrop()
            .error(R.mipmap.ic_launcher_round)
            .into(binding.friendIV)
        binding.hurryBtn.setOnClickListener {
            if (binding.messageET.text.toString() == "") {
                hurryFriend(user, "Hurry up!!")
            } else {
                hurryFriend(user, binding.messageET.text.toString())
            }
        }
    }

    private fun hurryFriend(user: IUser, messageText: String) {
        firebaseManager.auth.uid?.let {
            val msg = Message(
                messageId = UUID.randomUUID().toString(),
                senderUid = it,
                receiverUid = user.userId,
                messageText = messageText
            )
            firebaseManager.sendMessage(msg) {
                Toast.makeText(
                    context,
                    "Successfully hurried ${user.displayName}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun setItemWidth(width: Int) {
        val layoutParams = binding.root.layoutParams
        layoutParams.width = width
        binding.root.layoutParams = layoutParams
    }

}

class EventFriendsAdapter(
    private val friends: MutableList<User>,
    private val context: Context
) : RecyclerView.Adapter<EventFriendsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventFriendsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemAttendeeBinding.inflate(inflater, parent, false)

        val recyclerViewWidth = parent.measuredWidth
        val itemWidth = (recyclerViewWidth * 0.9).toInt()

        val viewHolder = EventFriendsViewHolder(binding, context)
        viewHolder.setItemWidth(itemWidth)

        return viewHolder
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    override fun onBindViewHolder(holder: EventFriendsViewHolder, position: Int) {
        val friend = friends[position]
        holder.bind(friend)
    }

    fun setData(friendsList: List<User>) {
        friends.clear()
        friends.addAll(friendsList)
    }
}