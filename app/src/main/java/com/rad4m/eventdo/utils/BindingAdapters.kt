package com.rad4m.eventdo.utils

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rad4m.eventdo.models.DataItem
import com.rad4m.eventdo.ui.mainfragment.EventsAdapter
import java.text.SimpleDateFormat

@BindingAdapter("eventList")
fun RecyclerView.eventList(list: List<DataItem>?) {
    val adapter = adapter as EventsAdapter
    adapter.submitList(list)
}

@BindingAdapter("convertDate")
fun TextView.convertDate(date: String) {
    date.let {
        val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val newDate = originalFormat.parse(date)
        val targetFormat = SimpleDateFormat("EEEE dd MMMM")
        val formattedDate = targetFormat.format(newDate)
        this.text = formattedDate
    }
}