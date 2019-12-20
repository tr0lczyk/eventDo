package com.rad4m.eventdo.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rad4m.eventdo.databinding.RecyclerHeaderBinding
import com.rad4m.eventdo.models.DataItem

class HeaderViewHolder(val binding: RecyclerHeaderBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: DataItem.DataItemHeader) {
        binding.viewModel = item
        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): HeaderViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = RecyclerHeaderBinding.inflate(layoutInflater, parent, false)
            return HeaderViewHolder(binding)
        }
    }

}