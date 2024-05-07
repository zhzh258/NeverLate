package com.snowman.neverlate.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.FragmentProfileTabBadgesBinding
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.shared.SharedUserViewModel

class ProfileTabBadgesFragment : Fragment() {
    private var _binding: FragmentProfileTabBadgesBinding? = null
    private val binding get() = _binding!!
    private val sharedUserViewModel: SharedUserViewModel by activityViewModels()
    private val firebaseManager = FirebaseManager.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentProfileTabBadgesBinding.inflate(inflater, container, false)
        setUpUI()
        return binding.root
    }

    private fun setUpUI() {
        sharedUserViewModel.userData.value?.userId?.let { userId ->
            firebaseManager.fetchAverageArrivalTimeForCurrentUser { averageTime, error ->
                error?.let { err: Exception ->
                    Log.e("ProfileFragment", "Error fetching average arrive time: $err")
                } ?: run {
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
        }
    }
}