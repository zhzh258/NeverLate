package com.snowman.neverlate.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.FragmentProfileV2Binding
import com.snowman.neverlate.model.shared.UserViewModel
import com.snowman.neverlate.model.types.IUser
import com.snowman.neverlate.model.FirebaseManager
import android.util.Log

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileV2Binding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()
    private val firebaseManager = FirebaseManager.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileV2Binding.inflate(inflater, container, false)
        userViewModel.userData.value?.let {
            initProfileData(it)
        }
        observeProfile()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeProfile() {
        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                loadUserData()
                initProfileData(it)
            }
        }
    }

    private fun initProfileData(me: IUser) {
        binding.displayNameTV.text = me.displayName
        binding.phoneNumberTV.text = me.phoneNumber
        binding.addressTV.text = me.address
        binding.emailTV.text = (me.email)
        binding.aboutMeTV.text = me.status
        Glide.with(binding.profileIV)
            .load(me.photoURL)
            .circleCrop()
            .error(R.mipmap.ic_launcher_round)
            .into(binding.profileIV)
    }

    private fun loadUserData() {
        firebaseManager.loadUserData { user ->
            if (user != null) {
                user.updateUserViewModel(userViewModel)
            } else {
                Log.e("FirebaseManager", "Failed to retrieve user data")
            }
        }
    }
}