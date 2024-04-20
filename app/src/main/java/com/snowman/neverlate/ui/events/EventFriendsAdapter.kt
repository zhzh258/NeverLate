package com.snowman.neverlate.ui.events

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.ListItemAttendeeBinding
import com.snowman.neverlate.model.types.IUser
import com.snowman.neverlate.model.types.User

class EventFriendsViewHolder(
    private val binding: ListItemAttendeeBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: IUser) {
        binding.friendTV.text = user.displayName
        Glide.with(binding.friendIV)
            .load(user.photoURL)
            .circleCrop()
            .error(R.mipmap.ic_launcher_round)
            .into(binding.friendIV)
    }

}

class EventFriendsAdapter(
    private val friends: MutableList<User>
) : RecyclerView.Adapter<EventFriendsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventFriendsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemAttendeeBinding.inflate(inflater, parent, false)
        return EventFriendsViewHolder(binding)
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