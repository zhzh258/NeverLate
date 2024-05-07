package com.snowman.neverlate.ui.profile
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.FragmentProfileTabFriendsBinding
import com.snowman.neverlate.ui.friends.FriendsFragment

class ProfileTabFriendsFragment: Fragment() {
    private var _binding: FragmentProfileTabFriendsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentProfileTabFriendsBinding.inflate(inflater, container, false)

        val friendsFragment = FriendsFragment()
        Log.d("MY_DEBUG", "AAA")
        childFragmentManager.beginTransaction()
            .replace(R.id.friends_container, friendsFragment)
            .commit()
        childFragmentManager.executePendingTransactions()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}