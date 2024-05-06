package com.snowman.neverlate.ui.friends

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.ListItemFriendsBinding
import com.snowman.neverlate.model.FirebaseManager
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

        setUpBadge(user)
    }

    private fun setUpBadge(user: IUser) {
        user.userId.let {
            FirebaseManager.getInstance().fetchAverageArrivalTimeForUser(user){ averageTime, error ->
                error?.let { err: Exception ->
                    Log.e("ProfileFragment", "Error fetching average arrive time: $err")
                } ?: run {
                    //binding.timeTVV.text = String.format("%.2f", averageTime)
                    averageTime?.let {
                        binding.badgeImg.setImageResource(
                            when {
                                averageTime > 30 -> R.drawable.badge_10
                                averageTime > 20 -> R.drawable.badge_9
                                averageTime > 15 -> R.drawable.badge_8
                                averageTime > 5 -> R.drawable.badge_7
                                averageTime > 0 -> R.drawable.badge_6
                                averageTime > -5 -> R.drawable.badge_5
                                averageTime > -15 -> R.drawable.badge_4
                                averageTime > -20 -> R.drawable.badge_3
                                averageTime > -30 -> R.drawable.badge_2
                                else -> R.drawable.badge_1
                            }
                        )
                    }
                } ?: run {
                    binding.badgeImg.setImageResource(R.drawable.badge_5)
                }
            }
        }
    }
}

class FriendsListAdapter(
    private var friends: MutableList<IUser>
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

    fun updateData(newFriends: List<IUser>) {
        friends.clear()
        friends.addAll(newFriends)
        notifyDataSetChanged()
    }
}