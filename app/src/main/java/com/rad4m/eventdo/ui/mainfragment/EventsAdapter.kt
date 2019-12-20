package com.rad4m.eventdo.ui.mainfragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rad4m.eventdo.databinding.ListItemBinding
import com.rad4m.eventdo.models.DataItem
import com.rad4m.eventdo.models.EventModel
import com.rad4m.eventdo.utils.HeaderViewHolder
import com.rad4m.eventdo.utils.Utilities.Companion.ITEM_VIEW_TYPE_HEADER
import com.rad4m.eventdo.utils.Utilities.Companion.ITEM_VIEW_TYPE_ITEM

class EventsAdapter(
    private val clickListener: EventListener
) :
    ListAdapter<DataItem, RecyclerView.ViewHolder>(OfferDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            else -> throw IllegalArgumentException("no supported item id")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val item = getItem(position) as DataItem.DataItemEventModel
                holder.bind(item.eventModel, clickListener)
            }
            is HeaderViewHolder -> {
                val item = getItem(position) as DataItem.DataItemHeader
                holder.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).viewType
    }

    class ViewHolder private constructor(private val binding: ListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: EventModel,
            clickListener: EventListener
        ) {
            binding.viewModel = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    class EventListener(val clickListener: (eventModel: EventModel) -> Unit) {
        fun onClick(event: EventModel) = clickListener(event)
    }
}

class OfferDiffCallback : DiffUtil.ItemCallback<DataItem>() {

    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.viewType == newItem.viewType
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}