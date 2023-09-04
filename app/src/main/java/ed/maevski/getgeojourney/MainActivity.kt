package ed.maevski.getgeojourney

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import ed.maevski.getgeojourney.data.API.CLIENT_ID
import ed.maevski.getgeojourney.data.API.CLIENT_SECRET
import ed.maevski.getgeojourney.data.entity.GeoData
import ed.maevski.getgeojourney.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val TAG = "myLogs"

    private var count = 1
    private var status = Channel<Boolean>(Channel.CONFLATED)
    private var flagStatus = true
    private val scopeMainActivity = CoroutineScope(Dispatchers.IO)

    private lateinit var job: Job
    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val database = App.getDatabaseInstance()
    private val locationApi = App.getLocationApiInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationManager = LocationManager(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(10000L)
            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()

        locationCallback = object : LocationCallback() {
            @SuppressLint("SetTextI18n")
            override fun onLocationResult(p0: LocationResult) {
                p0 ?: return
                for (location in p0.locations) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    // Обновляем текстовые поля с координатами
                    binding.geoLocation.text = "$ Широта: $latitude; Долгота: $longitude"
                }
            }
        }

        if (checkAndRequestPermissions()) requestLocationUpdates()

        job = scopeMainActivity.launch() {
            Log.d(TAG, "запускаем скоуп на чтение канала")

            for (element in status) {
                Log.d(
                    TAG,
                    "Здесь читаем канал и запускаем бесконечный вывод координат flagStatus = $element"
                )

                flagStatus = element

                var geoData: GeoData
                while (!element) {
                    Log.d(TAG, "While: flagStatus = $element")

                    // Здесь можно снова запросить местоположение согласно вашему интервалу
                    delay(5000) // 10 секунд

                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "While: count = ${count++}")
                        geoData = locationManager.getGeoData(this)

                        Log.d(
                            TAG,
                            "geoData: latitude = ${geoData.latitude}, longitude = ${geoData.longitude}"
                        )
                    }

                    database.geoDataDao().insertGeoData(geoData)

                    locationApi.sendLocationData(CLIENT_ID, CLIENT_SECRET, geoData)
                        .enqueue(object : Callback<Void> {

                            override fun onResponse(
                                call: Call<Void>,
                                response: Response<Void>
                            ) {
                                Log.d(TAG, "locationApi.sendLocationData -> onResponse")
                            }

                            override fun onFailure(
                                call: Call<Void>,
                                t: Throwable
                            ) {
                                Log.d(TAG, "locationApi.sendLocationData -> onFailure")
                            }
                        })
                    yield()
                }


            }
        }

        binding.fabStart.setOnClickListener {
            Log.d(TAG, "fabStart.setOnClickListener")

            scopeMainActivity.launch() {
                if (flagStatus) {
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "flagStatus = $flagStatus, меняем на остановку")

                        binding.fabStart.setImageResource(R.drawable.baseline_stop_24)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "flagStatus = $flagStatus, меняем на запись")

                        binding.fabStart.setImageResource(R.drawable.baseline_play_arrow_24)
                    }
                }
                Log.d(TAG, "flagStatus = $flagStatus, отправляем в канал противоположный флаг")

                status.send(!flagStatus)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    private fun checkAndRequestPermissions(): Boolean {
        Log.d(TAG, "Функция: checkAndRequestPermissions()")
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.ACCESS_NETWORK_STATE)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.INTERNET
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.INTERNET)
        }

        if (permissions.isNotEmpty()) {
            Log.d(TAG, "разрешений нет, ждем ответа от пользователя")

            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                ACCESS_FINE_LOCATION_PERMISSION_CODE
            )
            return false
        } else {
            Log.d(TAG, "разрешения все есть")

            return true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == ACCESS_FINE_LOCATION_PERMISSION_CODE) {
            Log.d(TAG, "grantResults: ${grantResults.joinToString()}")

            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                //Разрешение на определение локации предоставлены
                Log.d(TAG, "onRequestPermissionsResult: Все разрешения предоставлены")

                requestLocationUpdates()

            } else {
                Log.d(TAG, "onRequestPermissionsResult: разрешения не предоставлены")

                // Разрешения не были предоставлены
                Toast.makeText(
                    this,
                    "Предоставьте, пожалуйста, разрешения на определения геолокации и проверка соединения Интернета",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    override fun onDestroy() {

        job.cancel()
        scopeMainActivity.cancel()

        super.onDestroy()
    }

    companion object {
        private val ACCESS_FINE_LOCATION_PERMISSION_CODE = 423
    }
}

