package com.rad4m.eventdo.utils

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.rad4m.eventdo.R
import com.rad4m.eventdo.models.DataItem
import com.rad4m.eventdo.networking.VendorLogoNetworking.Companion.downloadLogo
import com.rad4m.eventdo.ui.mainfragment.EventsAdapter
import de.hdodenhof.circleimageview.CircleImageView
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
        val formattedDate = targetFormat.format(newDate).capitalize()
        val formattedAgainDate = formattedDate.split(" ")
        val month = formattedAgainDate.get(2).capitalize()
        val finalDate = "${formattedAgainDate[0]} ${formattedAgainDate[1]} ${month}"
        this.text = finalDate
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

@BindingAdapter("setBackgroundTint")
fun MaterialButton.setBackgroundTint(value: Int) {
    backgroundTintList = ContextCompat.getColorStateList(this.context, value)
}

@BindingAdapter("vendorId", "codedImage")
fun CircleImageView.loadLogo(vendorId: Int, codedImage: String?) {
    if(!codedImage.isNullOrEmpty() && !codedImage.equals("1")){
        val decodedString = Base64.decode(codedImage, Base64.DEFAULT)
        val bitmap2 = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        this.setImageBitmap(bitmap2)
    } else if (codedImage.equals("1")){
        this.setImageResource(R.drawable.icon_logo)
    } else {
        downloadLogo(vendorId.toString(), this)
    }
}