package com.snowman.neverlate.ui.events

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.ListItemAddEventAttendeeBinding
import com.snowman.neverlate.model.types.IUser

class EventAttendeeViewHolder(
    val binding: ListItemAddEventAttendeeBinding,
    private val adapter: EventAttendeesAdapter
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(attendee: IUser) {
        Glide.with(binding.friendIV)
            .load(attendee.photoURL)
            .circleCrop()
            .error(R.mipmap.ic_launcher_round)
            .into(binding.friendIV)

        binding.deleteBtn.setOnClickListener {
            val position = adapterPosition
            adapter.removeAttendee(position)

        }
    }
}

class EventAttendeesAdapter(
    private val attendees: MutableList<IUser>
) : RecyclerView.Adapter<EventAttendeeViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventAttendeeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemAddEventAttendeeBinding.inflate(inflater, parent, false)
        return EventAttendeeViewHolder(binding, this)
    }

    override fun getItemCount(): Int {
        return attendees.size
    }

    override fun onBindViewHolder(holder: EventAttendeeViewHolder, position: Int) {
        val attendee = attendees[position]
        holder.bind(attendee)
    }

    fun removeAttendee(position: Int) {
        attendees.removeAt(position)
        notifyItemRemoved(position)
    }
}