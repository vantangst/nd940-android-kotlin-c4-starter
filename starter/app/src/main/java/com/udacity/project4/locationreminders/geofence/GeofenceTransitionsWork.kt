package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.*
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.Int
import kotlin.String
import kotlin.getValue
import kotlin.let
import com.udacity.project4.locationreminders.data.dto.Result as ReminderResult

class GeofenceTransitionsWork(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters), KoinComponent {

    companion object {
        const val KEY_INPUT_URL = "KEY_INPUT_REQUEST_ID"
        private const val TAG = "GeofenceTransitionsWork"

        fun enqueueWork(context: Context, intent: Intent) {
            if (intent.action == SaveReminderFragment.ACTION_GEOFENCE_EVENT) {
                GeofencingEvent.fromIntent(intent)?.let { geofencingEvent ->
                    Log.i(TAG, "enqueueWork unzip data: ${geofencingEvent.triggeringGeofences}")

                    if (geofencingEvent.hasError()) {
                        val errorMessage = errorMessage(context, geofencingEvent.errorCode)
                        Log.e(TAG, errorMessage)
                        return
                    }

                    if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                        Log.v(TAG, context.getString(R.string.geofence_entered))

                        geofencingEvent.triggeringGeofences?.let { triggeringGeofences ->
                            val fenceId = when {
                                triggeringGeofences.isNotEmpty() ->
                                    triggeringGeofences[0].requestId
                                else -> {
                                    Log.e(TAG, "No Geofence Trigger Found! Abort mission!")
                                    return
                                }
                            }
                            val inputData = Data.Builder()
                            inputData.putString(KEY_INPUT_URL, fenceId)
                            val request =
                                OneTimeWorkRequest.Builder(GeofenceTransitionsWork::class.java)
                                    .setInputData(inputData.build())
                                    .build()
                            WorkManager.getInstance(context).enqueue(request)
                        }
                    }
                } ?: Log.e(TAG, "Not found GeofencingEvent")
            }
        }

        /**
         * Returns the error string for a geofencing error code.
         */
        private fun errorMessage(context: Context, errorCode: Int): String {
            val resources = context.resources
            return when (errorCode) {
                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
                    R.string.geofence_not_available
                )
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
                    R.string.geofence_too_many_geofences
                )
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
                    R.string.geofence_too_many_pending_intents
                )
                else -> resources.getString(R.string.geofence_unknown_error)
            }
        }
    }

    override suspend fun doWork(): Result {
        inputData.getString(KEY_INPUT_URL)?.let { requestId ->
            sendNotification(requestId)
            return Result.success()
        }
        return Result.failure()
    }

    private suspend fun sendNotification(requestId: String) {
        //Get the local repository instance
        val remindersLocalRepository: ReminderDataSource by inject()
//        Interaction to the repository has to be through a coroutine scope
        coroutineScope {
            launch(SupervisorJob()) {
                //get the reminder with the request id
                val result = remindersLocalRepository.getReminder(requestId)
                if (result is ReminderResult.Success<ReminderDTO>) {
                    val reminderDTO = result.data
                    //send a notification to the user with the reminder details
                    sendNotification(
                        applicationContext, ReminderDataItem(
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude,
                            reminderDTO.id
                        )
                    )
                }
            }
        }
    }
}