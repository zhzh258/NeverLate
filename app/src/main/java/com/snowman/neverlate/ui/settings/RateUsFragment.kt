package com.snowman.neverlate.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.FragmentRateusBinding
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.types.User

class RateUsFragment : Fragment() {

    private var _binding: FragmentRateusBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRateusBinding.inflate(inflater, container, false)

        binding.star1.setOnClickListener {
            binding.star1.setImageResource(R.drawable.ic_star)
            binding.star2.setImageResource(R.drawable.ic_empty_star)
            binding.star3.setImageResource(R.drawable.ic_empty_star)
            binding.star4.setImageResource(R.drawable.ic_empty_star)
            binding.star5.setImageResource(R.drawable.ic_empty_star)
        }

        binding.star2.setOnClickListener {
            binding.star1.setImageResource(R.drawable.ic_star)
            binding.star2.setImageResource(R.drawable.ic_star)
            binding.star3.setImageResource(R.drawable.ic_empty_star)
            binding.star4.setImageResource(R.drawable.ic_empty_star)
            binding.star5.setImageResource(R.drawable.ic_empty_star)
        }

        binding.star3.setOnClickListener {
            binding.star1.setImageResource(R.drawable.ic_star)
            binding.star2.setImageResource(R.drawable.ic_star)
            binding.star3.setImageResource(R.drawable.ic_star)
            binding.star4.setImageResource(R.drawable.ic_empty_star)
            binding.star5.setImageResource(R.drawable.ic_empty_star)
        }

        binding.star4.setOnClickListener {
            binding.star1.setImageResource(R.drawable.ic_star)
            binding.star2.setImageResource(R.drawable.ic_star)
            binding.star3.setImageResource(R.drawable.ic_star)
            binding.star4.setImageResource(R.drawable.ic_star)
            binding.star5.setImageResource(R.drawable.ic_empty_star)
        }

        binding.star5.setOnClickListener {
            binding.star1.setImageResource(R.drawable.ic_star)
            binding.star2.setImageResource(R.drawable.ic_star)
            binding.star3.setImageResource(R.drawable.ic_star)
            binding.star4.setImageResource(R.drawable.ic_star)
            binding.star5.setImageResource(R.drawable.ic_star)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}