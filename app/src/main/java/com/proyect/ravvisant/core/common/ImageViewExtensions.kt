package com.proyect.ravvisant.core.common

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.proyect.ravvisant.R

fun ImageView.loadImage(url: String?) {
    if (!url.isNullOrEmpty()) {
        Glide.with(context)
            .load(url)
            .centerCrop()
            .placeholder(R.drawable.ic_launcher_background)
            .into(this)
    }
}