package com.snowman.neverlate.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.FragmentRateusBinding
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.shared.SharedUserViewModel
import com.snowman.neverlate.model.types.IUser

class RateUsFragment : Fragment() {

    private var _binding: FragmentRateusBinding? = null
    private val binding get() = _binding!!
    private val sharedUserViewModel: SharedUserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRateusBinding.inflate(inflater, container, false)
        observeSettings()
        binding.star1.setOnClickListener {
            setRating(1)
            updateUserDetails(1)
        }
        binding.star2.setOnClickListener {
            setRating(2)
            updateUserDetails(2)
        }
        binding.star3.setOnClickListener {
            setRating(3)
            updateUserDetails(3)
        }
        binding.star4.setOnClickListener {
            setRating(4)
            updateUserDetails(4)
        }
        binding.star5.setOnClickListener {
            setRating(5)
            updateUserDetails(5)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setRating(starCount: Int) {
        val stars = listOf(
            binding.star1, binding.star2, binding.star3, binding.star4, binding.star5
        )

        stars.forEachIndexed { index, imageView ->
            val imageRes =
                if (index < starCount) R.drawable.ic_widget_star else R.drawable.ic_widget_empty_star
            imageView.setImageResource(imageRes)
        }
    }


    private fun observeSettings() {
        sharedUserViewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                initProfileData(it)
            }
        }
    }

    private fun initProfileData(me: IUser) {
        setRating(me.rate)
    }

    private fun updateUserDetails(rate: Int) {

        val updatedUserData = mapOf(
            "rate" to rate
        )

        FirebaseManager.getInstance().editUserProfile(updatedUserData,
            onSuccess = {
                Toast.makeText(context, "User details updated successfully.", Toast.LENGTH_SHORT)
                    .show()
                it.updateUserViewModel(sharedUserViewModel)
            },
            onFailure = { exception ->
                Toast.makeText(
                    context,
                    "Failed to update user details: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

}