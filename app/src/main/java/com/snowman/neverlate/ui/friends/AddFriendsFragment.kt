package com.snowman.neverlate.ui.friends

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.snowman.neverlate.R
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.shared.SharedFriendsViewModel
import com.snowman.neverlate.model.types.IUser

class AddFriendsFragment : Fragment() {

    private val TAG = "addfriendsfragment"
    private val RECYCLER_VIEW_MAX_ITEMS = 4

    private val firebaseManager = FirebaseManager.getInstance()
    private val searchList = mutableListOf<IUser>()
    private val requestsList = mutableListOf<IUser>()
    private lateinit var addFriendsAdapter: AddFriendsAdapter
    private lateinit var friendRequestsAdapter: FriendRequestsAdapter
    private lateinit var searchFriendsSV: SearchView
    private lateinit var searchFriendsRV: RecyclerView
    private lateinit var friendReqRV: RecyclerView
    private lateinit var searchFriendsBtn: Button
    private var searchQuery = ""
    private val friendsViewModel: SharedFriendsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_friend_v2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setUpSearchFeature()
        setUpRequestsFeature()
    }

    private fun initViews(view: View) {
        searchFriendsSV = view.findViewById(R.id.searchFriendsSV)
        searchFriendsRV = view.findViewById(R.id.searchFriendsRV)
        friendReqRV = view.findViewById(R.id.friendReqRV)
        searchFriendsBtn = view.findViewById(R.id.searchFriendsBtn)
    }

    private fun setUpSearchFeature() {
        setUpSearchRecyclerView()
        setSearchListener()
    }

    private fun setUpSearchRecyclerView() {
        searchFriendsRV.layoutManager = LinearLayoutManager(context)
        addFriendsAdapter = AddFriendsAdapter(searchList, requireContext())
        searchFriendsRV.adapter = addFriendsAdapter
    }

    private fun setSearchListener() {
        searchFriendsSV.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                p0?.let { email -> search(email) }
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    searchQuery = p0
                }
                return true
            }
        })

        searchFriendsBtn.setOnClickListener{
            search(searchQuery)
        }
    }

    private fun search(email: String) {
        firebaseManager.searchUsersByEmail(email) { users, exception ->
            if (exception != null) {
                Log.e(TAG, "Error searching for users: $exception")
            } else {
                users?.let {
                    searchList.clear()
                    searchList.addAll(it.take(RECYCLER_VIEW_MAX_ITEMS)) // will js show top 4 searches
                    addFriendsAdapter.notifyDataSetChanged()
                }

                if (users.isNullOrEmpty()) {
                    Toast.makeText(
                        context,
                        "There are no users with that email",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setUpRequestsFeature() {
        setUpRequestsRecyclerView()
        populateRequests()
    }

    private fun setUpRequestsRecyclerView() {
        friendReqRV.layoutManager = LinearLayoutManager(context)
        friendRequestsAdapter = FriendRequestsAdapter(requestsList, friendsViewModel)
        friendReqRV.adapter = friendRequestsAdapter
    }

    private fun populateRequests() {
        FirebaseManager.getInstance().getFriendRequests { usersList, exception ->
            if (exception != null) {
                Log.e(TAG, "Error fetching friend requests: $exception")
            } else {
                // Use the usersList containing friend requests
                usersList?.let {
                    requestsList.clear()
                    requestsList.addAll(usersList)
                    friendRequestsAdapter.notifyDataSetChanged()
                }
            }
        }
    }
}