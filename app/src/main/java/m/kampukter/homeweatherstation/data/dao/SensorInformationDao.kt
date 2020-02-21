package m.kampukter.homeweatherstation.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import m.kampukter.homeweatherstation.data.SensorInformation
import java.net.URL

@Dao
interface SensorInformationDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sensorInformation: List<SensorInformation>)

    @Query("select DISTINCT deviceUrl from sensor_information")
    suspend fun getAllUrl(): List<URL>

    @Query("select * from sensor_information where sensorName = :name")
    fun getInfoBySensor(name: String):LiveData<SensorInformation>
}