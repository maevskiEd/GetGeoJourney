package ed.maevski.getgeojourney.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ed.maevski.getgeojourney.data.dao.GeoDataDao
import ed.maevski.getgeojourney.data.entity.GeoData

@Database(entities = [GeoData::class], version = 1, exportSchema = false)
abstract class GeoJourneyDatabase : RoomDatabase() {
    abstract fun geoDataDao(): GeoDataDao
}