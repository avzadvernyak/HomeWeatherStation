package m.kampukter.homeweatherstation.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.net.URL

@Entity(tableName = "sensor_information")
data class SensorInformation(
    @PrimaryKey val sensorName: String,
    val deviceUrl: URL,
    val title: String,
    val measure: String,
    val maxValue: Double,
    val minValue: Double
)
