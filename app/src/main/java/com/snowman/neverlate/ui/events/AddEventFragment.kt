package com.snowman.neverlate.ui.events

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import com.snowman.neverlate.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.shared.SharedFriendsViewModel
import com.snowman.neverlate.model.types.Event
import com.snowman.neverlate.model.types.IEvent
import com.snowman.neverlate.model.types.IUser
import com.snowman.neverlate.model.types.MemberStatus
import com.snowman.neverlate.ui.addressSelection.AddressSelectionActivity
import java.util.Calendar
import java.util.Date
import java.util.UUID

class AddEventFragment : Fragment() {
    private val TAG = "AddEventFragment"

    private lateinit var addressSelectorButton: Button
    private lateinit var attendeeRV: RecyclerView
    private lateinit var addAttendeeCV: MaterialCardView
    private lateinit var attendeesAdapter: EventAttendeesAdapter
    private val event = Event()
    private var eventYear = 0
    private var eventMonth = 0
    private var eventDay = 0
    private var attendees = mutableListOf<IUser>()
    private val friendsViewModel: SharedFriendsViewModel by activityViewModels()
    private val hashedIdToUserMap = mutableMapOf<Int, IUser>()
    private val firebaseManager = FirebaseManager.getInstance()
    private val searchList = mutableListOf<IEvent>()
    private lateinit var addEventsAdapter: AddEventsAdapter
    private val db = FirebaseFirestore.getInstance()
    val auth = com.google.firebase.ktx.Firebase.auth
    private val currentUser = auth.currentUser
    private var profileUri: Uri? = null
    private lateinit var event_image: ImageView
    private lateinit var latLng: LatLng

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinner: Spinner = view.findViewById(R.id.spinner1)
        val eventTypes = resources.getStringArray(R.array.EventType)

        val spinnerAMPM: Spinner = view.findViewById(R.id.spinnerAMPM)
        val ampmOptions = arrayOf("AM", "PM")
        val adapter1 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ampmOptions)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAMPM.adapter = adapter1

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, eventTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val calendarIcon: ImageButton = view.findViewById(R.id.ic_calendar)
        calendarIcon.setOnClickListener {
            showCalendar()
        }

        setUpImageUpload(view)
        setUpAddAttendees(view)
        setUpAddressSelector(view)

        val addEventButton: Button = view.findViewById<Button>(R.id.addEventButton)
        addEventButton.setOnClickListener {
            addNewEventToFirebase(view)
        }
    }

    private fun addNewEventToFirebase(view: View) {
        // Read input fields from the UI and create an Event object
        val eventName: String = view.findViewById<EditText>(R.id.event_title).text.toString()
        val eventDescription: String = view.findViewById<EditText>(R.id.event_description).text.toString()
        var eventHour: String = view.findViewById<EditText>(R.id.editTextHour).text.toString().toString()
        val eventMinute: String = view.findViewById<EditText>(R.id.editTextMinute).text.toString().toString()
        val eventType: String = view.findViewById<Spinner>(R.id.spinner1).selectedItem.toString()
        var eventMembers = mutableListOf<MemberStatus>()

        for(attendee in attendeesAdapter.getAttendees()) {
            eventMembers.add(MemberStatus(attendee.userId, false, "", 0L))
        }

        if (eventMembers.size == 0 || eventYear == 0 || eventName.isEmpty() || eventDescription.isEmpty() || eventHour.isEmpty() || eventMinute.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        var hour: Int = eventHour.toInt()
        val minute: Int = eventMinute.toInt()
        if (hour !in 0..12 || minute !in 0..59) {
            // Show a toast message if hour or minute is invalid
            Toast.makeText(requireContext(), "Invalid hour or minute", Toast.LENGTH_SHORT).show()
            return
        }
        if(view.findViewById<Spinner>(R.id.spinnerAMPM).selectedItem.toString().equals("PM")) {
            hour += 12
        }

        if(currentUser != null) {
            eventMembers.add(MemberStatus(currentUser.uid, false, "", 0L))
        }

        event.active = true
        event.id = UUID.randomUUID().toString();
        event.name = eventName
        event.description = eventDescription
        event.category = eventType
        event.date = Timestamp(Date(eventYear, eventMonth, eventDay, hour, minute))
        event.members = eventMembers.toList()
        val uri = profileUri
        if (uri != null) {
            firebaseManager.saveImageToStorage(uri, event.id) { url ->
                if (url != null) {
                    event.photoURL = url
                    saveEvent(event)
                }
            }
        } else {
            event.photoURL = "https://funny-business.com/wp-content/uploads/2021/08/Choose-the-Right-Event-Host.jpg"
            saveEvent(event)
        }
    }

    private fun saveEvent(event: Event) {
        db.collection("events")
            .add(event)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Event added with ID: ${documentReference.id}")
                findNavController().navigate(R.id.nav_events)
                // Show a success message to the user
                Toast.makeText(requireContext(), "Event added successfully!", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding event", e)
                // Show an error message to the user
                Toast.makeText(
                    requireContext(),
                    "Failed to add event. Please try again later.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showCalendar() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                eventYear = year
                eventMonth = month
                eventDay = dayOfMonth
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun setUpImageUpload(view: View) {
        val chooseImageButton = view.findViewById<ImageButton>(R.id.chooseImageButton)
        event_image = view.findViewById(R.id.event_image)
        chooseImageButton.setOnClickListener { imageUpload() }
    }

    private fun imageUpload() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openGalleryForImage()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val STORAGE_PERMISSION_CODE = 1001
        private const val ADDRESS_SELECTION_CODE = 2000
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) { // pick image returns
            profileUri = data.data

            Glide.with(this)
                .load(profileUri)
                .into(event_image)
        } else if (requestCode == ADDRESS_SELECTION_CODE && resultCode == Activity.RESULT_OK && data != null){ // address selection returns
            val addressString: String? = data?.getStringExtra("addressString")
            val latitude: Double? = data?.getDoubleExtra("latitude", 70.0)
            val longitude: Double? = data?.getDoubleExtra("longitude", 70.0)
            addressSelectorButton.text=  addressString + " " + latitude.toString() + " " + longitude.toString()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGalleryForImage()
            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {
                    showPermissionSettingsDialog()
                } else {
                    Toast.makeText(
                        context,
                        "Permission denied. Unable to change picture without permission.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun showPermissionSettingsDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Permission Required")
        builder.setMessage("This permission is needed for accessing your gallery to upload an event image. Please enable it in app settings.")
        builder.setPositiveButton("Go to Settings") { dialog, which ->
            openAppSettings()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun setUpAddAttendees(view: View) {
        addAttendeeCV = view.findViewById(R.id.addAttendeeCV)
        attendeeRV = view.findViewById(R.id.attendeeRV)
        attendeeRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        attendeesAdapter = EventAttendeesAdapter(attendees)
        attendeeRV.adapter = attendeesAdapter
        addAttendeesClickListener()
    }

    private fun addAttendeesClickListener() {
        addAttendeeCV.setOnClickListener{
            val friendsList = friendsViewModel.friends.value ?: emptyList()

            if (friendsList.isNotEmpty()) {
                val popupMenu = PopupMenu(requireContext(), it)
                for (friend in friendsList) {
                    if (!attendees.contains(friend)) {
                        val hashedId = friend.userId.hashCode()
                        hashedIdToUserMap[hashedId] = friend
                        popupMenu.menu.add(Menu.NONE, hashedId, Menu.NONE, friend.displayName)
                    }
                }
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    val selectedFriendId = menuItem.itemId
                    val selectedFriend = hashedIdToUserMap[selectedFriendId]
                    if (selectedFriend != null) {
                        attendeesAdapter.addAttendee(selectedFriend)
                        return@setOnMenuItemClickListener true
                    }
                    return@setOnMenuItemClickListener false
                }
                popupMenu.show()
            } else {
                Toast.makeText(requireContext(), "No friends to display, please add some friends first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUpAddressSelector(view: View){
        addressSelectorButton = view.findViewById(R.id.address_selector_button)
        addressSelectorButton.setOnClickListener {
            // open AddressSelectionActivity
            val intent = Intent(requireContext(), AddressSelectionActivity::class.java)
            startActivityForResult(intent, ADDRESS_SELECTION_CODE)
        }
    }

}