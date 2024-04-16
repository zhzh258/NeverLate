package com.snowman.neverlate.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.FragmentSettingBinding
import com.snowman.neverlate.model.shared.UserViewModel
import com.snowman.neverlate.model.types.IUser
import com.snowman.neverlate.model.FirebaseManager
import android.util.Log

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()
    private val firebaseManager = FirebaseManager.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        binding.btnEditProfile.setOnClickListener(){
            findNavController().navigate(R.id.settingsFragment_to_editProfileFragment)
        }
        binding.btnRateUs.setOnClickListener(){
            findNavController().navigate(R.id.settingsFragment_to_rateUsFragment)
        }
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                activity?.setTheme(R.style.Theme_NeverLate)
            } else {
                activity?.setTheme(R.style.Theme_NeverLate_Dark)
            }
            activity?.recreate()  // 重新创建Activity以应用主题
        }
        observeUserData()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeUserData() {
        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                loadUserData()
                initProfileData(it)
            }
        }
    }

    private fun initProfileData(me: IUser) {
        binding.displayNameTV.text = me.displayName
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