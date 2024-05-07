package com.snowman.neverlate.ui.friends

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.FragmentFriendsBinding
import com.snowman.neverlate.model.shared.SharedFriendsViewModel
import com.snowman.neverlate.model.types.IUser

class FriendsFragment : Fragment() {

    private var _binding: FragmentFriendsBinding? = null
    private lateinit var adapter: FriendsListAdapter
    private val binding get() = _binding!!
    private val friendsViewModel: SharedFriendsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("MY_DEBUG", "FriendsFragment: onCreateView")
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MY_DEBUG", "FriendsFragment: onViewCreated")

        initViews(view)

        binding.addFriendsBtn.setOnClickListener {
            findNavController().navigate(R.id.action_friendsFragment_to_addFriendsFragment)
        }

        searchFriendsListener()
    }



    private fun initViews(view: View) {
        binding.friendsListRv.layoutManager = LinearLayoutManager(context)
        adapter = FriendsListAdapter(mutableListOf())
        binding.friendsListRv.adapter = adapter
        // observe friends changes
        friendsViewModel.friends.observe(viewLifecycleOwner) { friends ->
            // update adapter
            adapter.updateData(friends)
        }
    }

    private fun searchFriendsListener() {
        binding.searchFriendsSV.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredFriends = friendsViewModel.friends.value?.filter { friend ->
                    friend.displayName.contains(newText.orEmpty(), ignoreCase = true)
                }
                filteredFriends?.let { adapter.updateData(it) }
                return true
            }
        })
    }

    // This method is reserved for usage in ProfileFragment - FriendsTab
    fun setButtonVisibility(visible: Boolean) {
        binding.addFriendsBtn.visibility = if (visible) View.VISIBLE else View.GONE
    }

}