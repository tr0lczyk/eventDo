package com.rad4m.eventdo.utils

import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rad4m.eventdo.models.DataItem
import com.rad4m.eventdo.ui.mainfragment.EventsAdapter
import java.text.SimpleDateFormat
import java.util.Locale

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
        val targetFormat = SimpleDateFormat("EEEE dd MMMM", Locale.ENGLISH)
        val formattedDate = targetFormat.format(newDate)
        this.text = formattedDate
    }
}

@BindingAdapter("convertDateTime")
fun TextView.convertDateTime(date: String) {
    date.let {
        val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val newDate = originalFormat.parse(date)
        val targetFormat = SimpleDateFormat("dd MMM yyyy HH:mm")
        val formattedDate = targetFormat.format(newDate)
        this.text = formattedDate
    }
}

@BindingAdapter("convertHoursMinutes")
fun TextView.convertHoursMinutes(date: String) {
    date.let {
        val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val newDate = originalFormat.parse(date)
        val targetFormat = SimpleDateFormat("HH:mm")
        val formattedDate = targetFormat.format(newDate)
        this.text = formattedDate
    }
}

interface ItemSelectedListener {
    fun onItemSelected(item: Any)
}

@BindingAdapter("setEnries")
fun Spinner.setEntries(entries: List<String>) {
    setEntries(entries)
}

@BindingAdapter("onSpinnerItemSelected")
fun Spinner.onSpinnerItemSelected(itemSelected: ItemSelectedListener?) {
    onItemClickListener
}

@BindingAdapter("setSpinnerValue")
fun Spinner.setSpinnerValue(value: String) {
    setSpinnerValue(value)
}

@BindingAdapter("setHintAs")
fun EditText.setHintAs(value: String) {
    hint = value
}