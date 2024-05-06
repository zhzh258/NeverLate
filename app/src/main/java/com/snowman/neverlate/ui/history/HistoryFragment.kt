package com.snowman.neverlate.ui.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SearchView

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.snowman.neverlate.R
import com.snowman.neverlate.model.types.IEvent
import com.snowman.neverlate.ui.events.EventsListAdapter
import com.snowman.neverlate.ui.events.EventsViewModel

class HistoryFragment : Fragment()  {
    private lateinit var eventsListRv: RecyclerView
    private lateinit var addEventBtn: Button
    private val eventsViewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: HistoryListAdapter
    private lateinit var searchEventsSV: SearchView
    private lateinit var events: MutableLiveData<List<IEvent>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("MY_DEBUG", "HistoryFragment: onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        observeEvents()

//        addEventBtn.setOnClickListener {
//            findNavController().navigate(R.id.nav_addEvent)
//        }

        eventsViewModel.navigateToAnotherPage.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                navigateToAnotherPage()
                // Reset the value to false to avoid triggering navigation multiple times
                eventsViewModel.onButtonClicked()
            }
        }

        val searchView: SearchView = view.findViewById(R.id.searchEventsSV)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Perform search when user submits query (e.g., by pressing Enter)
                searchEvents(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Perform search as user types
                searchEvents(newText)
                return true
            }
        })

        view.findViewById<Button>(R.id.filterAllBtn).setOnClickListener {
            filterEvents(null) // Passing null to show all events
        }
        view.findViewById<Button>(R.id.filterDiningBtn).setOnClickListener {
            filterEvents("Dining")
        }
        view.findViewById<Button>(R.id.filterStudyBtn).setOnClickListener {
            filterEvents("Study")
        }
        view.findViewById<Button>(R.id.filterMeetingBtn).setOnClickListener {
            filterEvents("Meeting")
        }
        view.findViewById<Button>(R.id.filterTravelBtn).setOnClickListener {
            filterEvents("Travel")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("MY_DEBUG", "HistoryFragment: onDestroyView")
    }
    private fun searchEvents(query: String?) {
        // Filter the list of events based on the query
        val filteredEvents = events.value?.filter { event ->
            event.name.contains(query ?: "", ignoreCase = true)
        }
        // Update the RecyclerView with filtered events
        filteredEvents?.let { updateEventsList(it, adapter) }
    }

    private fun filterEvents(eventType: String?) {
        val filteredEvents = if (eventType.isNullOrEmpty()) {
            eventsViewModel.events.value // Show all events if eventType is null or empty
        } else {
            eventsViewModel.events.value?.filter { it.category == eventType }
        }
        filteredEvents?.let {
            updateEventsList(it, adapter)
        }
    }

    private fun navigateToAnotherPage() {
//        val action = EventsFragmentDirections.actionEventFragmentToEventFragment(eventId)
//        findNavController().navigate(action)
        findNavController().navigate(R.id.nav_eventDetails)
    }

    private fun initViews(view: View) {
        eventsListRv = view.findViewById(R.id.eventsListRv)
        eventsListRv.layoutManager = LinearLayoutManager(context)
        adapter = HistoryListAdapter(mutableListOf()) {
            navigateToAnotherPage()
        }
        eventsListRv.adapter = adapter
        searchEventsSV = view.findViewById(R.id.searchEventsSV)
        events = eventsViewModel.events
    }

    private fun observeEvents() {
        eventsViewModel.events.observe(viewLifecycleOwner) { event ->
            updateEventsList(event, adapter)
        }
        eventsViewModel.fetchEventsData()
        this.events = eventsViewModel.events
    }

    private fun updateEventsList(events: List<IEvent>, adapter: HistoryListAdapter) {
        adapter.updateData(events)
    }

}