package ed.maevski.getgeojourney.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import ed.maevski.getgeojourney.data.entity.GeoData

@Dao
interface GeoDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGeoData(geoData: GeoData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<GeoData>)
}