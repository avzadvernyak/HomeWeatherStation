package m.kampukter.homeweatherstation.data

data class SensorInf(
    val id: String,
    val measure: String,
    val sensorName: String,
    val maxValue: Double,
    val minValue: Double
)