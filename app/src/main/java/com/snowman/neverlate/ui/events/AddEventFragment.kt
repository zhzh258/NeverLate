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
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.firestore.FirebaseFirestore
import com.snowman.neverlate.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.shared.SharedFriendsViewModel
import com.snowman.neverlate.model.types.Event
import com.snowman.neverlate.model.types.IEvent
import com.snowman.neverlate.model.types.IUser
import com.snowman.neverlate.model.types.MemberStatus
import com.snowman.neverlate.ui.addressSelection.AddressSelectionActivity
import com.snowman.neverlate.util.TimeUtil
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar
import java.util.Date
import java.util.UUID

class AddEventFragment : Fragment() {
    private val TAG = "AddEventFragment"
    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val STORAGE_PERMISSION_CODE = 1001
        private const val ADDRESS_SELECTION_CODE = 2000
    }

    // UI
    private lateinit var addressSelectorButton: Button
    private lateinit var attendeeRV: RecyclerView
    private lateinit var addAttendeeCV: MaterialCardView
    private lateinit var attendeesAdapter: EventAttendeesAdapter
    private lateinit var addEventsAdapter: AddEventsAdapter
    private lateinit var event_image: ImageView



    // Firebase
    private val firebaseManager = FirebaseManager.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val auth = com.google.firebase.ktx.Firebase.auth
    private val currentUser = auth.currentUser

    private val sharedFriendsViewModel: SharedFriendsViewModel by activityViewModels()
//
    private val hashedIdToUserMap = mutableMapOf<Int, IUser>()
//    private val searchList = mutableListOf<IEvent>()
//    private var profileUri: Uri? = null
//    private lateinit var latLng: LatLng

    private val vm: AddEventViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpImageUpload(view)
        setUpTitleAndDescription(view)
        setUpDateSelector(view)
        setUpTimeSelector(view)
        setUpDurationSelector(view)
        setUpAddressSelector(view)
        setUpEventTypeSelector(view)
        setUpAddAttendees(view)

        val addEventButton: Button = view.findViewById<Button>(R.id.addEventButton)
        addEventButton.setOnClickListener {
            addNewEventToFirebase(view)
        }
    }

    private fun addNewEventToFirebase(view: View) {
        if (vm.event.name.isEmpty() || vm.event.description.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        } else if (vm.event.members.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least 1 friend", Toast.LENGTH_SHORT).show()
            return
        } else if (vm.event.category == "") {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = vm.profileUri.value
        if (uri != null) {
            firebaseManager.saveImageToStorage(uri, vm.event.id) { url ->
                if (url != null) {
                    vm.event.photoURL = url
                    saveEvent(vm.event)
                }
            }
        } else {
            vm.event.photoURL = "https://funny-business.com/wp-content/uploads/2021/08/Choose-the-Right-Event-Host.jpg"
            saveEvent(vm.event)
        }
    }

    private fun saveEvent(event: Event) {
        Log.d(TAG, vm.event.toString())
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


    private fun setUpImageUpload(view: View) {
        val chooseImageButton = view.findViewById<ImageButton>(R.id.chooseImageButton)
        event_image = view.findViewById(R.id.event_image)
        chooseImageButton.setOnClickListener {
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
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) { // pick image returns
            vm.profileUri.value = data.data

            Glide.with(this)
                .load(vm.profileUri.value)
                .into(event_image)
        } else if (requestCode == ADDRESS_SELECTION_CODE && resultCode == Activity.RESULT_OK && data != null){ // address selection returns
            val addressString: String = data.getStringExtra("addressString") ?: "No address found"
            val latitude: Double = data.getDoubleExtra("latitude", 70.0)
            val longitude: Double = data.getDoubleExtra("longitude", 70.0)
            addressSelectorButton.text=  addressString
            // update vm.event
            vm.event.address = addressString
            vm.event.location = GeoPoint(latitude, longitude)
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

    private fun setUpTitleAndDescription(view: View) {
        val title = view.findViewById<EditText>(R.id.event_title)
        val description = view.findViewById<EditText>(R.id.event_description)

        title.addTextChangedListener { it ->
            vm.event.name = it.toString()
        }

        description.addTextChangedListener {
            vm.event.description = it.toString()
        }
    }
    private fun setUpDateSelector(view: View) {
        val year = view.findViewById<TextView>(R.id.textViewYear)
        val month = view.findViewById<TextView>(R.id.textViewMonth)
        val day = view.findViewById<TextView>(R.id.textViewDay)

        vm.selectedDate.observe(viewLifecycleOwner) { localDate ->
            year.text = localDate.year.toString()
            month.text = localDate.month.toString()
            day.text = localDate.dayOfMonth.toString()
            // update vm.event
            val localDateTime = LocalDateTime.of(localDate, vm.selectedTime.value!!)
            vm.event.date = TimeUtil.localDateTime2Timestamp(localDateTime)
        }
        val dateSelector = view.findViewById<MaterialCardView>(R.id.date_selector)
        dateSelector.setOnClickListener {
            // show calendar
            val calendar = Calendar.getInstance()
            // this one requires 0-11 lol, while LocalDate requires 1-12
            calendar.set(vm.selectedDate.value!!.year, vm.selectedDate.value!!.monthValue-1, vm.selectedDate.value!!.dayOfMonth)
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    // update vm.selectedDate
                    vm.selectedDate.value = LocalDate.of(year, month+1, dayOfMonth)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

    }
    private fun setUpTimeSelector(view: View) {
        val hour = view.findViewById<TextView>(R.id.textViewHour)
        val minute = view.findViewById<TextView>(R.id.textViewMinute)
        vm.selectedTime.observe(viewLifecycleOwner) { localTime ->
            hour.setText(localTime.hour.toString())
            minute.setText(localTime.minute.toString())
            // update vm.event
            val localDateTime = LocalDateTime.of(vm.selectedDate.value!!, localTime)
            vm.event.date = TimeUtil.localDateTime2Timestamp(localDateTime)
        }

        val timeSelector = view.findViewById<MaterialCardView>(R.id.time_selector)
        timeSelector.setOnClickListener {
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(vm.selectedTime.value?.hour ?: 12)
                .setMinute(vm.selectedTime.value?.minute ?: 0)
                .setTitleText("Select Time")
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build()
            timePicker.show(childFragmentManager, "timePicker")
            timePicker.addOnPositiveButtonClickListener {
                vm.selectedTime.value = LocalTime.of(timePicker.hour, timePicker.minute)
            }
        }
    }

    private fun setUpDurationSelector(view: View) {
        val slider = view.findViewById<Slider>(R.id.duration_slider)
        slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being started
            }

            override fun onStopTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being stopped
            }
        })

        slider.addOnChangeListener { slider, value, fromUser ->
            // Responds to when slider's value is changed
            vm.event.duration = value.toLong()
        }
    }
    private fun setUpAddressSelector(view: View){
        // please see onActivityResult()
        addressSelectorButton = view.findViewById(R.id.address_selector_button)
        addressSelectorButton.setOnClickListener {
            // open AddressSelectionActivity
            val intent = Intent(requireContext(), AddressSelectionActivity::class.java)
            startActivityForResult(intent, ADDRESS_SELECTION_CODE)
        }
    }
    private fun setUpEventTypeSelector(view: View) {

        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroup)
        val eventTypes = resources.getStringArray(R.array.EventType)

        eventTypes.forEach { text ->
            val chip = Chip(requireContext()).apply {
                this.text = text
                isClickable = true
                isCheckable = true
            }
            chipGroup.addView(chip)
        }

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val chip = view.findViewById<Chip>(checkedIds[0])
            if (chip != null) {
                // Toast.makeText(requireContext(), "Selected chip: ${chip.text}", Toast.LENGTH_SHORT).show()
                vm.event.category = chip.text.toString()
            }
        }
    }
    private fun setUpAddAttendees(view: View) {
        addAttendeeCV = view.findViewById(R.id.addAttendeeCV)
        attendeeRV = view.findViewById(R.id.attendeeRV)
        attendeeRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        vm.attendees.observe(viewLifecycleOwner) {
            Log.d("AddEventFragment", "observe ${vm.attendees.value!!.size}")
            var eventMembers = mutableListOf<MemberStatus>()
            for(attendee in vm.attendees.value!!) {
                eventMembers.add(MemberStatus(attendee.userId, false, "", 0L))
            }
            // remember to add yourself
            if(currentUser != null) {
                eventMembers.add(MemberStatus(currentUser.uid, false, "", 0L))
            }
            // update vm.event
            vm.event.members = eventMembers
        }
        attendeesAdapter = EventAttendeesAdapter(
            vm
        )
        attendeeRV.adapter = attendeesAdapter
        addAttendeesClickListener()
    }

    private fun addAttendeesClickListener() {
        addAttendeeCV.setOnClickListener{
            val friendsList = sharedFriendsViewModel.friends.value ?: emptyList()
            Toast.makeText(requireContext(), "${friendsList.size}", Toast.LENGTH_SHORT).show()

            if (friendsList.isNotEmpty()) {
                val popupMenu = PopupMenu(requireContext(), it)
                for (friend in friendsList) {
                    if (vm.attendees.value?.contains(friend) == false) { // only show the friends that you haven't add
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



}