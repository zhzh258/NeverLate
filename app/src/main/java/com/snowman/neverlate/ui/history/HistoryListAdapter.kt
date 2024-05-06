package com.snowman.neverlate.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.ListItemEventPastBinding
import com.snowman.neverlate.model.types.IEvent
import com.snowman.neverlate.util.TimeUtil
import androidx.core.content.ContextCompat


class HistoryViewHolder(private val binding: ListItemEventPastBinding, private val currentUserId: String) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(event: IEvent) {
        binding.textEventTitle.text = event.name
        binding.textEventLocation.text = event.address
        binding.textEventTime.text = TimeUtil.dateFormat.format(event.date.toDate())
        val memberStatus = event.members.find { it.id == currentUserId }
        val arriveStatus = memberStatus?.let {
            if (it.arriveTime > 0) "You were Punctual with ${it.arriveTime} minutes earlier"
            else "You were Late with ${-it.arriveTime} minutes late"
        } ?: "Status Unknown"
        val colorId = memberStatus?.let {
            if (it.arriveTime > 0) R.color.colorPrimary else R.color.colorLate
        } ?: R.color.colorLate

        binding.punctualityTV.setBackgroundColor(ContextCompat.getColor(binding.punctualityTV.context, colorId))
        binding.punctualityTV.text = arriveStatus
        Glide.with(binding.imageEvent)
            .load(event.photoURL)
            .circleCrop()
            .error(R.mipmap.ic_launcher_round)
            .into(binding.imageEvent)
    }
}

class HistoryListAdapter(
    private var events: MutableList<IEvent>,
    private val currentUserId: String
) : RecyclerView.Adapter<HistoryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemEventPastBinding.inflate(inflater, parent, false)
        return HistoryViewHolder(binding, currentUserId)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event)
    }

    fun updateData(newFriends: List<IEvent>) {
        events.clear()
        events.addAll(newFriends)
        notifyDataSetChanged()
    }


}