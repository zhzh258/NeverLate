package com.snowman.neverlate.ui.friends

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.snowman.neverlate.R
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.types.IUser

class AddFriendsFragment : Fragment() {

    private val TAG = "addfriendsfragment"

    private val firebaseManager = FirebaseManager.getInstance()
    private val searchList = mutableListOf<IUser>()
    private val requestsList = mutableListOf<IUser>()
    private lateinit var addFriendsAdapter: AddFriendsAdapter
    private lateinit var friendRequestsAdapter: FriendRequestsAdapter
    private lateinit var searchFriendsSV: SearchView
    private lateinit var searchFriendsRV: RecyclerView
    private lateinit var friendReqRV: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_friends, container, false)
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
                p0?.let { email ->
                    firebaseManager.searchUsersByEmail(email) { users, exception ->
                        if (exception != null) {
                            Log.e(TAG, "Error searching for users: $exception")
                        } else {
                            users?.let {
                                searchList.clear()
                                searchList.addAll(it)
                                addFriendsAdapter.notifyItemChanged(0)
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
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }

        })
    }

    private fun setUpRequestsFeature() {
        setUpRequestsRecyclerView()
        populateRequests()
    }

    private fun setUpRequestsRecyclerView() {
        friendReqRV.layoutManager = LinearLayoutManager(context)
        friendRequestsAdapter = FriendRequestsAdapter(requestsList)
        friendReqRV.adapter = friendRequestsAdapter
    }

    private fun populateRequests() {
        val currentUserID = firebaseManager.auth.currentUser?.uid
        if (currentUserID != null) {
            FirebaseManager.getInstance().getFriendRequests(currentUserID) { usersList, exception ->
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
}