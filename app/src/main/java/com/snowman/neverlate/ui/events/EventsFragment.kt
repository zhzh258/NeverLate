package com.snowman.neverlate.ui.events

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ToggleButton
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.snowman.neverlate.R
import com.snowman.neverlate.model.types.IEvent
import com.snowman.neverlate.model.shared.SharedOneEventViewModel

class EventsFragment : Fragment() {
    private lateinit var eventsListRv: RecyclerView
    private lateinit var addEventFab: FloatingActionButton
    private val eventsViewModel: EventsViewModel by viewModels()
    private lateinit var adapter: EventsListAdapter
    private lateinit var searchEventsSV: SearchView
    private lateinit var events: MutableLiveData<List<IEvent>>
    private val sharedOneEventViewModel: SharedOneEventViewModel by activityViewModels()
    private lateinit var selectedBtn: ToggleButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("MY_DEBUG", "EventFragment: onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        observeEvents()

        addEventFab.setOnClickListener {
            findNavController().navigate(R.id.nav_addEvent)
        }

        eventsViewModel.navigateToAnotherPage.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                navigateToAnotherPage()
                // Reset the value to false to avoid triggering navigation multiple times
                eventsViewModel.onButtonClicked()
            }
        }

        performSearchAndFilter(view)
    }

    private fun performSearchAndFilter(view: View) {
        val searchView: SearchView = view.findViewById(R.id.searchEventsSV)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchAndFilterEvents(query, getSelectedEventType(view))
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchAndFilterEvents(newText, getSelectedEventType(view))
                return true
            }
        })

        view.findViewById<Button>(R.id.filterAllBtn).setOnClickListener {
            searchAndFilterEvents(
                searchView.query.toString(),
                null
            ) // Passing null to show all events
            selectBtn(it as ToggleButton)
        }
        view.findViewById<Button>(R.id.filterDiningBtn).setOnClickListener {
            searchAndFilterEvents(searchView.query.toString(), "Dining")
            selectBtn(it as ToggleButton)
        }
        view.findViewById<Button>(R.id.filterStudyBtn).setOnClickListener {
            searchAndFilterEvents(searchView.query.toString(), "Study")
            selectBtn(it as ToggleButton)
        }
        view.findViewById<Button>(R.id.filterMeetingBtn).setOnClickListener {
            searchAndFilterEvents(searchView.query.toString(), "Meeting")
            selectBtn(it as ToggleButton)
        }
        view.findViewById<Button>(R.id.filterTravelBtn).setOnClickListener {
            searchAndFilterEvents(searchView.query.toString(), "Travel")
            selectBtn(it as ToggleButton)
        }
    }

    override fun onDestroyView() {
        Log.d("MY_DEBUG", "EventsFragment: onDestroyView")
        super.onDestroyView()
    }

    private fun searchAndFilterEvents(query: String?, eventType: String?) {
        val filteredEvents = events.value?.filter { event ->
            val matchesSearch = event.name.contains(query ?: "", ignoreCase = true) //search part
            val matchesFilter =
                eventType.isNullOrEmpty() || event.category == eventType //filtering pt
            matchesSearch && matchesFilter
        }
        filteredEvents?.let { updateEventsList(it, adapter) }
    }

    private fun getSelectedEventType(view: View): String? {
        return when (selectedBtn) {
            view.findViewById<ToggleButton>(R.id.filterAllBtn) -> null
            view.findViewById<ToggleButton>(R.id.filterDiningBtn) -> "Dining"
            view.findViewById<ToggleButton>(R.id.filterStudyBtn) -> "Study"
            view.findViewById<ToggleButton>(R.id.filterMeetingBtn) -> "Meeting"
            view.findViewById<ToggleButton>(R.id.filterTravelBtn) -> "Travel"
            else -> null
        }
    }

    private fun navigateToAnotherPage() {
        findNavController().navigate(R.id.nav_eventDetails)
    }

    private fun initViews(view: View) {
        addEventFab = view.findViewById(R.id.addEventFab)
        eventsListRv = view.findViewById(R.id.eventsListRv)
        eventsListRv.layoutManager = LinearLayoutManager(context)
        adapter = EventsListAdapter(mutableListOf()) { event ->
            sharedOneEventViewModel.setSelectedEvent(event)
            findNavController().navigate(R.id.nav_eventDetails)
        }
        eventsListRv.adapter = adapter
        searchEventsSV = view.findViewById(R.id.searchEventsSV)
        events = eventsViewModel.events

        // the all btn will always be selected first
        selectedBtn = view.findViewById(R.id.filterAllBtn)
        selectedBtn.setBackgroundDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.rounded_selected_btn,
                null
            )
        )
        selectedBtn.isChecked = true
    }

    private fun selectBtn(btn: ToggleButton) {
        // deselect btn color
        selectedBtn.setBackgroundDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.rounded_btn,
                null
            )
        )
        selectedBtn.isChecked = false
        // select new button
        selectedBtn = btn
        selectedBtn.isChecked = true
        selectedBtn.setBackgroundDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.rounded_selected_btn,
                null
            )
        )
    }


    private fun observeEvents() {
        eventsViewModel.events.observe(viewLifecycleOwner) { event ->
            updateEventsList(event, adapter)
        }
        eventsViewModel.fetchEventsData()
        this.events = eventsViewModel.events
    }

    private fun updateEventsList(events: List<IEvent>, adapter: EventsListAdapter) {
        adapter.updateData(events)
    }

}