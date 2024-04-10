package com.snowman.neverlate.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)


        initProfileData()
        setProfileUpdateListener()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initProfileData() {
        binding.displayNameET.setText(profileViewModel.me.displayName)
        binding.phoneNumberET.setText(profileViewModel.me.phoneNumber.toString())
        binding.emailTV.text = (profileViewModel.me.email)
        binding.profileStatusET.setText(profileViewModel.me.status)
        Glide.with(binding.profileIV)
            .load(profileViewModel.me.profilePicture)
            .circleCrop()
            .error(R.mipmap.ic_launcher_round)
            .into(binding.profileIV)
    }

    private fun setProfileUpdateListener() {
        binding.profileEdit.setOnClickListener {
            if(!profileViewModel.isEdit){
                profileViewModel.isEdit = true
                binding.profileEdit.setImageResource(R.drawable.baseline_exit_to_app_24)
                binding.displayNameET.isEnabled = true
                binding.phoneNumberET.isEnabled = true
                binding.profileStatusET.isEnabled = true
            } else {
                profileViewModel.isEdit = false
                binding.profileEdit.setImageResource(R.drawable.ic_edit_24)
                binding.displayNameET.isEnabled = false
                binding.phoneNumberET.isEnabled = false
                binding.profileStatusET.isEnabled = false
                Toast.makeText(requireContext(), "TODO: save data to db", Toast.LENGTH_SHORT).show()
            }
        }
    }
}