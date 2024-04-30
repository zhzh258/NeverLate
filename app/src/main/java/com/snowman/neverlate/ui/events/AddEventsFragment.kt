package com.snowman.neverlate.ui.events

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.snowman.neverlate.R
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.shared.SharedFriendsViewModel
import com.snowman.neverlate.model.types.IEvent
import com.snowman.neverlate.model.types.User
import com.snowman.neverlate.ui.events.AddEventsAdapter
import java.util.Calendar

class AddEventsFragment : Fragment() {
    private val TAG = "addeventsfragment"

    private lateinit var attendeeRV: RecyclerView
    private lateinit var addAttendeeCV: CardView
    private var attendees = mutableListOf<User>()
    private val friendsViewModel: SharedFriendsViewModel by activityViewModels()
    private val firebaseManager = FirebaseManager.getInstance()
    private val searchList = mutableListOf<IEvent>()
    private lateinit var addEventsAdapter: AddEventsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinner: Spinner = view.findViewById(R.id.spinner1)
        val eventTypes = resources.getStringArray(R.array.EventType)

        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, eventTypes)

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        spinner.adapter = adapter

        val calendarIcon: ImageButton = view.findViewById(R.id.ic_calendar)
        calendarIcon.setOnClickListener {
            showCalendar()
        }

        setUpAddAttendees(view)
    }

    private fun showCalendar() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = "$dayOfMonth/${month + 1}/$year"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun setUpAddAttendees(view: View) {
        addAttendeeCV = view.findViewById(R.id.addAttendeeCV)
        attendeeRV = view.findViewById(R.id.attendeeRV)
        attendeeRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        addAttendeesClickListener()
    }

    private fun addAttendeesClickListener() {
        addAttendeeCV.setOnClickListener{
            Log.i("add attendees", friendsViewModel.friends.value.toString())
        }
    }

}