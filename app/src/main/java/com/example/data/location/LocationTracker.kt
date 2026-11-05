package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.data.model.GeoPoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserLocationState(
    val geoPoint: GeoPoint? = null,
    val accuracyMeters: Float = 0f,
    val bearingDegrees: Float = 0f,
    val speedKmh: Float = 0f,
    val isGpsEnabled: Boolean = false,
    val hasPermission: Boolean = false,
    val lastUpdatedTimestamp: Long = 0L
)

class LocationTracker(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _locationState = MutableStateFlow(UserLocationState())
    val locationState: StateFlow<UserLocationState> = _locationState.asStateFlow()

    private var isTracking = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            handleNewLocation(location)
        }
    }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun isGpsProviderEnabled(): Boolean {
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        val hasPerm = hasLocationPermission()
        val gpsOn = isGpsProviderEnabled()

        _locationState.value = _locationState.value.copy(
            hasPermission = hasPerm,
            isGpsEnabled = gpsOn
        )

        if (!hasPerm) return
        if (isTracking) return

        try {
            // Get last known location first for immediate UI response
            fusedClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    handleNewLocation(loc)
                }
            }

            // High accuracy location requests for driving & navigation
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2500L)
                .setMinUpdateIntervalMillis(1500L)
                .setMinUpdateDistanceMeters(2f)
                .build()

            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            isTracking = true
        } catch (e: SecurityException) {
            _locationState.value = _locationState.value.copy(hasPermission = false)
        } catch (e: Exception) {
            // Log or fallback
        }
    }

    fun stopTracking() {
        if (!isTracking) return
        try {
            fusedClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            // ignore
        } finally {
            isTracking = false
        }
    }

    private fun handleNewLocation(location: Location) {
        val geo = GeoPoint(location.latitude, location.longitude)
        val speedKmh = if (location.hasSpeed()) (location.speed * 3.6f) else 0f
        val bearing = if (location.hasBearing()) location.bearing else 0f

        _locationState.value = UserLocationState(
            geoPoint = geo,
            accuracyMeters = if (location.hasAccuracy()) location.accuracy else 15f,
            bearingDegrees = bearing,
            speedKmh = speedKmh,
            isGpsEnabled = isGpsProviderEnabled(),
            hasPermission = true,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
    }
}
