package com.snowman.neverlate.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.FragmentAboutusBinding
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.shared.SharedUserViewModel
import com.snowman.neverlate.model.types.IUser

class AboutUsFragment : Fragment() {

    private var _binding: FragmentAboutusBinding? = null

    private val binding get() = _binding!!
    private val sharedUserViewModel: SharedUserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}