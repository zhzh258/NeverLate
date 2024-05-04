package com.snowman.neverlate.ui.events

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.ListItemEventBinding
import com.snowman.neverlate.model.types.IEvent
import com.snowman.neverlate.util.TimeUtil

class EventsViewHolder(private val binding: ListItemEventBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(event: IEvent, onItemClick: (IEvent) -> Unit) {
        binding.root.setOnClickListener {
            onItemClick.invoke(event)
        }
        // Bind other data to the views
        binding.textEventTitle.text = event.name
        binding.textEventLocation.text = event.address
        binding.textEventTime.text = TimeUtil.dateFormat.format(event.date.toDate())
        binding.textPeopleCount.text = event.members.count().toString()
        Glide.with(binding.imageEvent)
            .load(event.photoURL)
            .circleCrop()
            .error(R.mipmap.ic_launcher_round)
            .into(binding.imageEvent)
    }
}

class EventsListAdapter(
    private var events: MutableList<IEvent>,
    private val onItemClick: (IEvent) -> Unit
) : RecyclerView.Adapter<EventsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemEventBinding.inflate(inflater, parent, false)
        return EventsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: EventsViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event) {
            onItemClick.invoke(event)
        }
    }

    fun updateData(newFriends: List<IEvent>) {
        events.clear()
        events.addAll(newFriends)
        notifyDataSetChanged()
    }


}