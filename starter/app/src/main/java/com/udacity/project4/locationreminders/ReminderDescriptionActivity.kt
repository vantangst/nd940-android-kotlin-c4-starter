package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.getSerializable
import org.koin.android.ext.android.inject

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReminderDescriptionBinding
    private lateinit var geofencingClient: GeofencingClient
    private val viewModel: SaveReminderViewModel by inject()

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"
        private const val TAG = "ReminderDescriptionActivity"

        // Receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layoutId = R.layout.activity_reminder_description
        binding = DataBindingUtil.setContentView(this, layoutId)
        geofencingClient = LocationServices.getGeofencingClient(this)
        val reminderDataItem = getSerializable(
            this,
            EXTRA_ReminderDataItem,
            ReminderDataItem::class.java
        )
        binding.reminderDataItem = reminderDataItem
        binding.btnDone.setOnClickListener {
            viewModel.removeReminder(reminderDataItem.id)
            removeGeofence(reminderDataItem.id)
        }
    }

    private fun removeGeofence(geofenceId: String) {
        geofencingClient.removeGeofences(listOf(geofenceId)).run {
            addOnSuccessListener {
                // Geofence removed
                Log.d(TAG, getString(R.string.geofences_removed))
                onBackPressedDispatcher.onBackPressed()
                Toast.makeText(applicationContext, R.string.geofences_removed, Toast.LENGTH_SHORT)
                    .show()
            }
            addOnFailureListener {
                // Failed to remove geofence
                Log.d(TAG, getString(R.string.geofences_not_removed))
            }
        }
    }
}