package m.kampukter.homeweatherstation.data.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import m.kampukter.homeweatherstation.data.Converters
import m.kampukter.homeweatherstation.data.SensorInformation

@Database(
    version = 1,exportSchema = false, entities = [
        SensorInformation::class
    ]
)
@TypeConverters(Converters::class)

abstract class MyDatabase: RoomDatabase() {
    abstract fun sensorInformationDao(): SensorInformationDao
}