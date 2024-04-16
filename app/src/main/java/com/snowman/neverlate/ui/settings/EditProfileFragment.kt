package com.snowman.neverlate.ui.settings


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.snowman.neverlate.databinding.FragmentEditProfileBinding
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.shared.UserViewModel
import com.snowman.neverlate.model.types.IUser

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null

    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        observeSettings()
        binding.btnSave.setOnClickListener {
            updateUserDetails()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeSettings() {
        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                initProfileData(it)
            }
        }
    }

    private fun initProfileData(me: IUser) {
        binding.usernameET.setText(me.displayName)
        binding.addressET.setText(me.address)
        binding.phoneNumberET.setText(me.phoneNumber)
        binding.AboutMeET.setText(me.status)
    }

    private fun updateUserDetails() {
        val username = binding.usernameET.text.toString().trim()
        val address = binding.addressET.text.toString().trim()
        val phoneNumber = binding.phoneNumberET.text.toString().trim()
        val aboutMe = binding.AboutMeET.text.toString().trim()

        val updatedUserData = mapOf(
            "displayName" to username,
            "address" to address,
            "phoneNumber" to phoneNumber,
            "status" to aboutMe,
        )

        FirebaseManager.getInstance().editUserProfile(updatedUserData,
            onSuccess = {
                Toast.makeText(context, "User details updated successfully.", Toast.LENGTH_SHORT).show()
                it.updateUserViewModel(userViewModel)
            },
            onFailure = { exception ->
                Toast.makeText(context, "Failed to update user details: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        )
    }
}