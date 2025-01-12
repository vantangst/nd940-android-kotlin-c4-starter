package com.udacity.project4.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.udacity.project4.base.BaseRecyclerViewAdapter
import java.io.Serializable

/**
 * Extension function to setup the RecyclerView.
 */
fun <T> RecyclerView.setup(
    adapter: BaseRecyclerViewAdapter<T>
) {
    this.apply {
        layoutManager = LinearLayoutManager(this.context)
        this.adapter = adapter
    }
}

fun Fragment.setTitle(title: String) {
    if (activity is AppCompatActivity) {
        (activity as AppCompatActivity).supportActionBar?.title = title
    }
}

fun Fragment.setDisplayHomeAsUpEnabled(bool: Boolean) {
    if (activity is AppCompatActivity) {
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(
            bool
        )
    }
}

/**
 * Animate changing the view visibility.
 */
fun View.fadeIn() {
    this.visibility = View.VISIBLE
    this.alpha = 0f
    this.animate().alpha(1f).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            this@fadeIn.alpha = 1f
        }
    })
}

/**
 * Animate changing the view visibility.
 */
fun View.fadeOut() {
    this.animate().alpha(0f).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            this@fadeOut.alpha = 1f
            this@fadeOut.visibility = View.GONE
        }
    })
}

fun <T : Serializable?> getSerializable(activity: Activity, name: String, clazz: Class<T>): T {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        activity.intent.getSerializableExtra(name, clazz)!!
    else
        activity.intent.getSerializableExtra(name) as T
}

fun Geocoder.getAddress(
    latitude: Double,
    longitude: Double,
    address: (Address?) -> Unit
) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getFromLocation(latitude, longitude, 2) { address(it.firstOrNull()) }
        return
    }

    try {
        address(getFromLocation(latitude, longitude, 1)?.firstOrNull())
    } catch(e: Exception) {
        //will catch if there is an internet problem
        address(null)
    }
}
