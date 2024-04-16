package com.snowman.neverlate.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.snowman.neverlate.R
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.types.IEvent

class EventFragment : Fragment() {
    private val TAG = "eventdetailsfragment"

//    private val firebaseManager = FirebaseManager.getInstance()
    private lateinit var addEventsAdapter: AddEventsAdapter


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
    }

}