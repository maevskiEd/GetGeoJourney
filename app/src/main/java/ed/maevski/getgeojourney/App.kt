package ed.maevski.getgeojourney

import android.app.Application
import androidx.room.Room
import ed.maevski.getgeojourney.data.ApiConstants.BASE_URL
import ed.maevski.getgeojourney.data.LocationApi
import ed.maevski.getgeojourney.data.db.GeoJourneyDatabase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class App : Application() {

    companion object {
        private lateinit var database: GeoJourneyDatabase
        private lateinit var retrofit: Retrofit
        lateinit var locationApi: LocationApi
            private set

        fun getLocationApiInstance(): LocationApi {
            return locationApi
        }

        fun getDatabaseInstance(): GeoJourneyDatabase {
            return database
        }

    }

    override fun onCreate() {
        super.onCreate()

        // Инициализация базы данных
        database = Room.databaseBuilder(
            applicationContext,
            GeoJourneyDatabase::class.java,
            "geoJourneyDatabase"
        )
            .build()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                if (BuildConfig.DEBUG) {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            })
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        locationApi = retrofit.create(LocationApi::class.java)
    }
}