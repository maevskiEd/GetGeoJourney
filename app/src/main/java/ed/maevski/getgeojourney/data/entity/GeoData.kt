package ed.maevski.getgeojourney.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "geoData",
    primaryKeys = ["user_id", "timestampInMillis"],
    indices = [
        Index(value = ["user_id"], unique = false),
        Index(value = ["timestampInMillis"], unique = true)
    ]
)
class GeoData (
    @ColumnInfo(name = "latitude") val latitude: Long,
    @ColumnInfo(name = "longitude") val longitude: Long,
    @ColumnInfo(name = "altitude") val altitude: Int,
    @ColumnInfo(name = "accuracy") val accuracy: Int,
    @ColumnInfo(name = "timestampInMillis") val timestampInMillis: Long,
    @ColumnInfo(name = "user_id") val user_id: String,
    @ColumnInfo(name = "isUploadToRemote") val isUploadToRemote: Boolean
)