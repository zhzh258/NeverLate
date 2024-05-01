package com.snowman.neverlate.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.snowman.neverlate.R
import com.snowman.neverlate.model.shared.SharedUserViewModel
import com.snowman.neverlate.model.types.IUser
import com.snowman.neverlate.model.FirebaseManager
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.snowman.neverlate.databinding.FragmentProfileBinding
import com.snowman.neverlate.model.shared.SharedFriendsViewModel

class ProfileFragment : Fragment() {
    private val TAG = "Profile Fragment"
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val sharedUserViewModel: SharedUserViewModel by activityViewModels()
    private val sharedFriendsViewModel: SharedFriendsViewModel by activityViewModels()
    private val firebaseManager = FirebaseManager.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        Log.d(TAG, "ProfileFragment created")
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        setUpBodyUI()
        sharedUserViewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                setUpHeaderUI(it)
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpHeaderUI(me: IUser) {
        binding.displayNameTV.text = me.displayName
        binding.friendCountTV.text = "${sharedFriendsViewModel.friends.value?.size ?: 0} friends"
        binding.editProfileMb.setOnClickListener {
            findNavController().navigate(R.id.nav_editProfile)
        }
        Glide.with(binding.profileIV)
            .load(me.photoURL)
            .circleCrop()
            .error(R.mipmap.ic_launcher_round)
            .into(binding.profileIV)
    }
    private fun setUpBodyUI() {
        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        binding.viewPager.adapter = viewPagerAdapter
        Log.d(TAG, "new ViewPagerAdapter created")
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }
}