package m.kampukter.homeweatherstation.data

sealed class Sensor(open val id: String) {

    data class Humidity(override val id: String, val percentage: Float): Sensor(id)
    data class Temperature(override val id: String, val degrees: Float): Sensor(id)
    data class Pressure(override val id: String, val mmHg: Float): Sensor(id)
    data class Voltage(override val id: String, val volts: Float): Sensor(id)
    data class Amperage(override val id: String, val amperes: Float): Sensor(id)
    data class Relay(override val id: String, val state: Boolean): Sensor(id)

}

data class Device(
    val deviceId: String,
    val sensorsData: List<Sensor>
)
