package com.rad4m.eventdo.utils

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rad4m.eventdo.models.DataItem
import com.rad4m.eventdo.networking.VendorLogoNetworking.Companion.downloadLogo
import com.rad4m.eventdo.ui.mainfragment.EventsAdapter
import de.hdodenhof.circleimageview.CircleImageView
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
        val targetFormat = SimpleDateFormat("EEEE dd MMMM")
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

@BindingAdapter("setHintAs")
fun EditText.setHintAs(value: String) {
    hint = value
}

@BindingAdapter("loadLogo")
fun CircleImageView.loadLogo(vendorId: Int) {
    downloadLogo(vendorId.toString(), this)
}