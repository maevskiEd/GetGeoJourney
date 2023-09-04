package ed.maevski.getgeojourney.data

import ed.maevski.getgeojourney.data.entity.GeoData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header

import retrofit2.http.POST

interface LocationApi {
    @POST("api/upload")
    fun sendLocationData(
        @Header("api_key") apiKey: String,
        @Header("api_secret") apiSecret: String,
        @Body geoData: GeoData
    ): Call<Void>
}