package com.snowman.neverlate.ui.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.FragmentNotificationBinding

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationRV: RecyclerView
    private lateinit var adapter: NotificationsAdapter
    private val notificationsViewModel: NotificationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpRecyclerView(view: View) {
        notificationRV = view.findViewById(R.id.notificationRV)
        notificationRV.layoutManager = LinearLayoutManager(context)
        adapter = NotificationsAdapter(notificationsViewModel.notificationsList)
        notificationRV.adapter = adapter
    }
}