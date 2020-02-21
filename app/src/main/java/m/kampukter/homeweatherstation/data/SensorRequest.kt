package m.kampukter.homeweatherstation.data


data class SensorRequest (
    val sensorName: String,
    val dateBegin: String,
    val dateEnd: String
)