package com.udacity.project4.locationreminders.geofence

import java.util.concurrent.TimeUnit

object GeofencingConstants {
    const val GEOFENCE_RADIUS_IN_METERS = 200f
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)
}