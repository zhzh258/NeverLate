package com.snowman.neverlate.ui.settings

import com.snowman.neverlate.ui.settings.SettingsViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.FragmentSettingBinding
import com.snowman.neverlate.model.types.User
import com.snowman.neverlate.ui.profile.ProfileViewModel
import androidx.fragment.app.activityViewModels

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val settingsViewModel: ProfileViewModel by viewModels()

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
        observeSettings()
        //setSettingsUpdateListener()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeSettings() {
        settingsViewModel.me.observe(viewLifecycleOwner) { user ->
            user?.let {
                settingsViewModel.reloadUserData()
                initProfileData(it)
            }
        }
    }

    private fun initProfileData(me: User) {
        binding.displayNameTV.setText(me.displayName)
        Glide.with(binding.profileIV)
            .load(me.photoURL)
            .circleCrop()
            .error(R.mipmap.ic_launcher_round)
            .into(binding.profileIV)
    }

}