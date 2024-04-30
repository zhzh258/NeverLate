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
import com.snowman.neverlate.model.shared.SharedUserViewModel
import com.snowman.neverlate.model.types.IUser
import com.snowman.neverlate.model.FirebaseManager
import android.util.Log

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileV2Binding? = null
    private val binding get() = _binding!!
    private val sharedUserViewModel: SharedUserViewModel by activityViewModels()
    private val firebaseManager = FirebaseManager.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileV2Binding.inflate(inflater, container, false)
        sharedUserViewModel.userData.value?.let {
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
        sharedUserViewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                loadUserData()
                initProfileData(it)
            }
        }
    }

    private fun initProfileData(me: IUser) {
        loadAverageArriveTime()
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
                user.updateUserViewModel(sharedUserViewModel)
            } else {
                Log.e("FirebaseManager", "Failed to retrieve user data")
            }
        }
    }

    // ProfileFragment
    private fun loadAverageArriveTime() {
//        sharedUserViewModel.userData.value?.id?.let { userId ->
            firebaseManager.fetchAverageArrivalTimeForCurrentUser{ averageTime, error ->
                error?.let {
                    Log.e("ProfileFragment", "Error fetching average arrive time: $it")
                } ?: run {
                    //binding.timeTVV.text = String.format("%.2f", averageTime)
                    averageTime?.let {
                        binding.badgeImg.setImageResource(
                            when {
                                averageTime > 30 -> R.drawable.badge_10
                                averageTime > 20 -> R.drawable.badge_9
                                averageTime > 15 -> R.drawable.badge_8
                                averageTime > 5 -> R.drawable.badge_7
                                averageTime > 0 -> R.drawable.badge_6
                                averageTime > -5 -> R.drawable.badge_5
                                averageTime > -15 -> R.drawable.badge_4
                                averageTime > -20 -> R.drawable.badge_3
                                averageTime > -30 -> R.drawable.badge_2
                                else -> R.drawable.badge_1
                            }
                        )

                        binding.badgeName.text =
                            when {
                                averageTime > 30 -> getString(R.string.badge_10)
                                averageTime > 20 -> getString(R.string.badge_9)
                                averageTime > 15 -> getString(R.string.badge_8)
                                averageTime > 5 -> getString(R.string.badge_7)
                                averageTime > 0 -> getString(R.string.badge_6)
                                averageTime > -5 -> getString(R.string.badge_5)
                                averageTime > -15 -> getString(R.string.badge_4)
                                averageTime > -20 -> getString(R.string.badge_3)
                                averageTime > -30 -> getString(R.string.badge_2)
                                else -> getString(R.string.badge_1)
                            }

                        binding.badgeContext.text =
                            when {
                                averageTime > 30 -> getString(R.string.badge_10s)
                                averageTime > 20 -> getString(R.string.badge_9s)
                                averageTime > 15 -> getString(R.string.badge_8s)
                                averageTime > 5 -> getString(R.string.badge_7s)
                                averageTime > 0 -> getString(R.string.badge_6s)
                                averageTime > -5 -> getString(R.string.badge_5s)
                                averageTime > -15 -> getString(R.string.badge_4s)
                                averageTime > -20 -> getString(R.string.badge_3s)
                                averageTime > -30 -> getString(R.string.badge_2s)
                                else -> getString(R.string.badge_1s)
                            }


                        }
                    } ?: run {
                    binding.badgeImg.setImageResource(R.drawable.badge_5)
                }
            }
//        }
    }


}