package com.snowman.neverlate.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.FragmentProfileV2Binding
import com.snowman.neverlate.model.types.User

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileV2Binding? = null

    private val binding get() = _binding!!
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileV2Binding.inflate(inflater, container, false)
        observeProfile()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeProfile() {
        profileViewModel.me.observe(viewLifecycleOwner) { user ->
            Log.d("ProfileFragment", "Updating UI with new user data")
            user?.let {
                profileViewModel.reloadUserData()
                initProfileData(it)
                Log.d("ProfileFragment", "running initProfileData")
            }
        }
    }

    private fun initProfileData(me: User?) {
        me?.let{
            binding.displayNameTV.setText(me.displayName)
            binding.phoneNumberTV.setText(me.phoneNumber.toString())
            binding.addressTV.setText(me.address)
            binding.emailTV.text = (me.email)
            binding.aboutMeTV.setText(me.status)
//            binding.PersonalSignatureTV.setText(me.personalSignature)
            Glide.with(binding.profileIV)
                .load(me.photoURL)
                .circleCrop()
                .error(R.mipmap.ic_launcher_round)
                .into(binding.profileIV)
        }
    }

}