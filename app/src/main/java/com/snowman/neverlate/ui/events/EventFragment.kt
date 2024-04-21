package com.snowman.neverlate.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.snowman.neverlate.R
import com.snowman.neverlate.model.FirebaseManager

class EventFragment : Fragment() {
    private val TAG = "eventdetailsfragment"

    private val firebaseManager = FirebaseManager.getInstance()
    private lateinit var addEventsAdapter: AddEventsAdapter
    private lateinit var friendsRV: RecyclerView
    private lateinit var friendsAdapter: EventFriendsAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_one_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val args: EventDetailsFragmentArgs by navArgs()
//        val eventId = args.eventId

        setUpFriends(view)
    }

    // TODO: REPLACE THIS WITH EVENTS MEMBERS LIST
    private val MOCK_DATA_REMOVE_LATER = listOf("7G8aYM2nCQYee2Ty2gOF6WfJfZi2", "kqrDkWba3dVMLEUnGFjX4gjqmmF3",
    "rPzbvBIau8OLR9yenHZDyhVNcVX2", "uoFb8MuJOAeAaL2wZqE8PTmrS8M2")

    private fun setUpFriends(view: View) {
        friendsRV = view.findViewById(R.id.friendsRV)
        friendsRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        friendsAdapter = EventFriendsAdapter(mutableListOf(), requireContext())
        firebaseManager.getUsersDataForIds(MOCK_DATA_REMOVE_LATER) { users ->
            friendsAdapter.setData(users)
            friendsAdapter.notifyDataSetChanged()
        }
        friendsRV.adapter = friendsAdapter
        PagerSnapHelper().attachToRecyclerView(friendsRV)
    }
}