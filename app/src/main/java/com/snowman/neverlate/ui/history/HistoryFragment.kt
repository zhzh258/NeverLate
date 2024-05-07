package com.snowman.neverlate.ui.history

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.snowman.neverlate.R
import com.snowman.neverlate.model.types.IEvent

class HistoryFragment : Fragment() {
    private lateinit var eventsListRv: RecyclerView
    private val eventsViewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: HistoryListAdapter
    private lateinit var searchEventsSV: SearchView
    private lateinit var events: MutableLiveData<List<IEvent>>
    private lateinit var selectedBtn: ToggleButton
    val auth = com.google.firebase.ktx.Firebase.auth
    private val currentUserId = auth.currentUser?.uid ?: ""

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
        performSearchAndFilter(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("MY_DEBUG", "HistoryFragment: onDestroyView")
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

    private fun initViews(view: View) {
        eventsListRv = view.findViewById(R.id.eventsListRv)
        eventsListRv.layoutManager = LinearLayoutManager(context)
        adapter = HistoryListAdapter(mutableListOf(), currentUserId)
        eventsListRv.adapter = adapter
        searchEventsSV = view.findViewById(R.id.searchEventsSV)
        events = eventsViewModel.events

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

    private fun updateEventsList(events: List<IEvent>, adapter: HistoryListAdapter) {
        adapter.updateData(events)
    }
}