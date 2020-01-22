package m.kampukter.homeweatherstation.data

data class UnitSensorInfo (
    val unit: String,
    val sensor1: Float,
    val sensor2: Float,
    val sensor3: Float,
    val sensor4: Float,
    val relay1: Boolean,
    val relay2: Boolean
)