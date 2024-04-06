package com.snowman.neverlate.ui.friends

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.ListItemAddFriendsBinding
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.types.IUser

class AddFriendsViewHolder(
    private val binding: ListItemAddFriendsBinding,
    private val context: Context
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(user: IUser) {
        binding.displayNameTV.text = user.displayName
        binding.memoTv.text = user.status
        Glide.with(binding.profileIV)
            .load(user.photoURL)
            .circleCrop()
            .error(R.mipmap.ic_launcher_round)
            .into(binding.profileIV)
        binding.sendReqBtn.setOnClickListener {
            val firebaseManager = FirebaseManager.getInstance()
            val currentUser = firebaseManager.firebaseAuth().currentUser

            if (currentUser != null) {
                firebaseManager.sendFriendRequest(
                    currentUser,
                    user,
                    { onSuccessFriendRequest() },
                    { onFailureFriendRequest(it) }
                )
            }
        }
    }

    private fun onSuccessFriendRequest() {
        Toast.makeText(context, "Friend request sent!", Toast.LENGTH_SHORT).show()
    }

    private fun onFailureFriendRequest(exception: String) {
        Toast.makeText(context, exception, Toast.LENGTH_SHORT).show()
    }
}

class AddFriendsAdapter(
    private val users: List<IUser>,
    private val context: Context
) : RecyclerView.Adapter<AddFriendsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddFriendsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemAddFriendsBinding.inflate(inflater, parent, false)
        return AddFriendsViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: AddFriendsViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return users.size
    }
}