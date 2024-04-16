package com.snowman.neverlate.ui.settings

//import com.snowman.neverlate.ui.settings.SettingsViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.FragmentEditProfileBinding
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.types.User

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null

    private val binding get() = _binding!!
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        binding.btnSave.setOnClickListener {
            updateUserDetails()
        }
        //observeSettings()
        //setSettingsUpdateListener()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun updateUserDetails() {
        val username = binding.usernameET.text.toString().trim()
        val address = binding.addressET.text.toString().trim()
        val phoneNumber = binding.phoneNumberET.text.toString().trim()
        val aboutMe = binding.AboutMeET.text.toString().trim()
        val personalSignature = binding.PersonalSignatureET.text.toString().trim()

        val phoneNumberLong = phoneNumber.toLong()
        val updatedUserData = mapOf(
            "displayName" to username,
            "address" to address,
            "phoneNumber" to phoneNumber,
            "status" to aboutMe,
            "personalSignature" to personalSignature
        )

        FirebaseManager.getInstance().editUserProfile(updatedUserData,
            onSuccess = {
                Toast.makeText(context, "User details updated successfully.", Toast.LENGTH_SHORT).show()
            },
            onFailure = { exception ->
                Toast.makeText(context, "Failed to update user details: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        )
    }


    private fun observeSettings() {
        settingsViewModel.me.observe(viewLifecycleOwner) { user ->
            user?.let {
                initProfileData(it)
            }
        }
    }

    private fun initProfileData(me: User) {
//        binding.profileStatusET.setText(me.status)
//        Glide.with(binding.profileIV)
//            .load(me.photoURL)
//            .circleCrop()
//            .error(R.mipmap.ic_launcher_round)
//            .into(binding.profileIV)
    }

    private fun setSettingsUpdateListener() {

    }
}