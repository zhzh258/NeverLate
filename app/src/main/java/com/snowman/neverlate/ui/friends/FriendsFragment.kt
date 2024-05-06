package com.snowman.neverlate.ui.friends

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.snowman.neverlate.R
import com.snowman.neverlate.model.shared.SharedFriendsViewModel
import com.snowman.neverlate.model.types.IUser

class FriendsFragment : Fragment() {

    private lateinit var friendsListRv: RecyclerView
    private lateinit var addFriendsBtn: Button
    private val friendsViewModel: SharedFriendsViewModel by activityViewModels()
    private lateinit var adapter: FriendsListAdapter
    private lateinit var searchFriendsSV: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        observeFriends()

        addFriendsBtn.setOnClickListener {
            findNavController().navigate(R.id.action_friendsFragment_to_addFriendsFragment)
        }

        searchFriendsListener()
    }

    private fun initViews(view: View) {
        addFriendsBtn = view.findViewById(R.id.addFriendsBtn)
        friendsListRv = view.findViewById(R.id.friendsListRv)
        friendsListRv.layoutManager = LinearLayoutManager(context)
        adapter = FriendsListAdapter(mutableListOf())
        friendsListRv.adapter = adapter
        searchFriendsSV = view.findViewById(R.id.searchFriendsSV)
    }

    private fun observeFriends() {
        friendsViewModel.friends.observe(viewLifecycleOwner) { friends ->
            updateFriendsList(friends, adapter)
        }
    }

    private fun updateFriendsList(friends: List<IUser>, adapter: FriendsListAdapter) {
        adapter.updateData(friends)
    }

    private fun searchFriendsListener() {
        searchFriendsSV.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredFriends = friendsViewModel.friends.value?.filter { friend ->
                    friend.displayName.contains(newText.orEmpty(), ignoreCase = true)
                }
                filteredFriends?.let { updateFriendsList(it, adapter) }
                return true
            }
        })
    }

}