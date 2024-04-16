package com.snowman.neverlate.model.types

data class Event(
    override val id: String = "",
    override val name: String = "",
    override val date: String = "",
    override val time: String = "",
    override val location: String = "",
    override val photoURL: String = "",
    override val active: Boolean = true,
    override val address: String = "",
    override val members: List<String> = emptyList<String>(),
    override val description: String = "",
    override val duration: Long = 0L,
    override val category: String = ""
) : IEvent {
    override fun updateEventViewModel(viewModel: EventViewModel) {
        viewModel.setEventData(this)
    }
}