package com.snowman.neverlate.ui.friends

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.snowman.neverlate.R

class FriendsFragment : Fragment() {

    private lateinit var friendsListRv: RecyclerView
    private val friendsViewModel: FriendsListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        friendsListRv = view.findViewById(R.id.friendsListRv)
        friendsListRv.layoutManager = LinearLayoutManager(context)
        val friends = friendsViewModel.friends
        val adapter = FriendsListAdapter(friends)
        friendsListRv.adapter = adapter

    }

}