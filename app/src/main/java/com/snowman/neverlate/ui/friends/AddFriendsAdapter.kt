package com.snowman.neverlate.ui.friends

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.ListItemAddFriendsBinding
import com.snowman.neverlate.databinding.ListItemFriendRequestsBinding
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

class FriendRequestsViewHolder(
    private val binding: ListItemFriendRequestsBinding,
    private val adapter: FriendRequestsAdapter
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(user: IUser) {
        binding.displayNameTV.text = user.displayName
        binding.emailTv.text = user.email
        Glide.with(binding.profileIV)
            .load(user.photoURL)
            .circleCrop()
            .error(R.mipmap.ic_launcher_round)
            .into(binding.profileIV)

        val firebaseManager = FirebaseManager.getInstance()
        val currentUserID = firebaseManager.firebaseAuth().currentUser?.uid
        val friendUserID = user.userId

        binding.acceptReqBtn.setOnClickListener {
            if (currentUserID != null) {
                firebaseManager.addFriend(
                    currentUserID,
                    user.userId,
                    { onSuccessAccept(firebaseManager, currentUserID, friendUserID) },
                    { e -> onFailureDecline(e) }
                )
            }
        }

        binding.declineReqBtn.setOnClickListener {
            if (currentUserID != null) {
                firebaseManager.removeFriendRequest(
                    currentUserID,
                    friendUserID,
                    { onSuccessDecline() },
                    { e -> onFailDecline(e) }
                )
            }
        }
    }

    private fun onSuccessAccept(
        firebaseManager: FirebaseManager,
        currentUserID: String,
        friendUserID: String
    ) {
        firebaseManager.removeFriendRequest(
            currentUserID,
            friendUserID,
            { removeRequest() },
            { e -> onFailDecline(e) }
        )
    }

    private fun onFailureDecline(e: Exception) {
        Log.e("FriendReqAdapter", "Error accepting friend request: $e")
    }

    private fun onSuccessDecline() {
        removeRequest()
    }

    private fun onFailDecline(e: Exception) {
        Log.e("FriendReqAdapter", "Error removing friend request: $e")
    }

    private fun removeRequest() {
        val position = adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            adapter.removeItem(position)
        }
    }
}

class FriendRequestsAdapter(private val requests: MutableList<IUser>) :
    RecyclerView.Adapter<FriendRequestsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemFriendRequestsBinding.inflate(inflater, parent, false)
        return FriendRequestsViewHolder(binding, this)
    }

    override fun getItemCount(): Int {
        return requests.size
    }

    override fun onBindViewHolder(holder: FriendRequestsViewHolder, position: Int) {
        val request = requests[position]
        holder.bind(request)
    }

    fun removeItem(position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            requests.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}