package ed.maevski.getgeojourney

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import ed.maevski.getgeojourney.data.entity.GeoData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationManager(private val context: Context) {

    private val locationRequest = LocationRequest.Builder(5000L)
    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
    .build()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private var geoData: GeoData? = null


    @SuppressLint("MissingPermission")
    suspend fun getGeoData(scope: CoroutineScope): GeoData {
        if (geoData != null) {
            return geoData!!
        }
        return suspendCoroutine { continuation ->
            var resumed = false
            scope.launch {
                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(p0: LocationResult) {
                        val location: Location? = p0?.lastLocation
                        location?.let {
                            geoData = GeoData(
                                latitude = it.latitude.toLong(),
                                longitude = it.longitude.toLong(),
                                altitude = it.altitude.toInt(),
                                accuracy = it.accuracy.toInt(),
                                timestampInMillis = System.currentTimeMillis(),
                                user_id = "11111",
                                isUploadToRemote = false
                            )
                            if (!resumed) {
                                resumed = true
                                continuation.resume(geoData!!)
                            }
//                            continuation.resume(geoData!!)
                        }
                    }
                }

                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)

            }
        }

/*        return suspendCancellableCoroutine { continuation ->
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    val location: Location? = p0?.lastLocation
                    location?.let {
                        geoData = GeoData(
                            latitude = it.latitude.toLong(),
                            longitude = it.longitude.toLong(),
                            altitude = it.altitude.toInt(),
                            accuracy = it.accuracy.toInt(),
                            timestampInMillis = System.currentTimeMillis(),
                            user_id = "11111",
                            isUploadToRemote = false
                        )
                        continuation.resume(geoData!!, null)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }*/
    }
}