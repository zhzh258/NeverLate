package com.snowman.neverlate.ui.profile
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.snowman.neverlate.databinding.FragmentProfileTabPersonalBinding
import com.snowman.neverlate.model.shared.SharedUserViewModel
import com.snowman.neverlate.model.types.IUser

class ProfileTabPersonalFragment: Fragment() {
    private var _binding: FragmentProfileTabPersonalBinding? = null
    private val binding get() = _binding!!
    private val sharedUserViewModel: SharedUserViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentProfileTabPersonalBinding.inflate(inflater, container, false)

        sharedUserViewModel.userData.observe(viewLifecycleOwner) { user: IUser? ->
            setUpUI(user)
        }

        return binding.root
    }

    private fun setUpUI(user: IUser?) {
        if(user == null) {
            throw Exception("ProfileTabPersonalFragment: user is null in sharedUserViewModel")
        }
        binding.aboutTv.text = user.status
        binding.addressTv.text = user.address
        binding.emailTv.text = user.email
        binding.phoneNumberTv.text = user.phoneNumber
    }
}