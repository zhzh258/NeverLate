package com.snowman.neverlate.ui.events

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.ListItemEventBinding
import com.snowman.neverlate.model.types.IEvent

class AddEventsViewHolder(
    private val binding: ListItemEventBinding,
    private val context: Context
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(event: IEvent) {
        binding.textEventTitle.text = event.name
        binding.textEventLocation.text = event.address
        binding.textEventTime.text = event.date.toString()
        Glide.with(binding.imageEvent)
            .load(event.photoURL)
            .circleCrop()
            .error(R.mipmap.ic_launcher_round)
            .into(binding.imageEvent)
    }

    private fun onSuccessEventCreation() {
        Toast.makeText(context, "Event made!", Toast.LENGTH_SHORT).show()
    }

    private fun onFailureEventCreation(exception: String) {
        Toast.makeText(context, exception, Toast.LENGTH_SHORT).show()
    }
}

class AddEventsAdapter(
    private val events: List<IEvent>,
    private val context: Context
) : RecyclerView.Adapter<AddEventsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddEventsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemEventBinding.inflate(inflater, parent, false)
        return AddEventsViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: AddEventsViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event)
    }

    override fun getItemCount(): Int {
        return events.size
    }
}

class EventsCreationViewHolder(
    private val binding: ListItemEventBinding,
    private val adapter: EventsCreationAdapter
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(event: IEvent) {
        binding.textEventTitle.text = event.name
        binding.textEventLocation.text = event.address
        binding.textEventTime.text = event.date.toDate().toLocaleString()
        Glide.with(binding.imageEvent)
            .load(event.photoURL)
            .circleCrop()
            .error(R.mipmap.ic_launcher_round)
            .into(binding.imageEvent)
    }
}

class EventsCreationAdapter(private val events: MutableList<IEvent>) :
    RecyclerView.Adapter<EventsCreationViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsCreationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemEventBinding.inflate(inflater, parent, false)
        return EventsCreationViewHolder(binding, this)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: EventsCreationViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event)
    }

    fun removeItem(position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            events.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}