package com.snowman.neverlate.ui.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.ListItemFriendsBinding
import com.snowman.neverlate.model.types.IUser

class FriendsViewHolder(private val binding: ListItemFriendsBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(user: IUser) {
        binding.displayNameTV.text = user.displayName
        binding.memoTv.text = user.status
        Glide.with(binding.profileIV)
            .load(user.photoURL)
            .circleCrop()
            .error(R.mipmap.ic_launcher_round)
            .into(binding.profileIV)
    }
}

class FriendsListAdapter(
    private val friends: List<IUser>
) : RecyclerView.Adapter<FriendsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemFriendsBinding.inflate(inflater, parent, false)
        return FriendsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        val friend = friends[position]
        holder.bind(friend)
    }
}