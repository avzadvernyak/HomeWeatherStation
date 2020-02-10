package m.kampukter.homeweatherstation.data.dto

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import m.kampukter.homeweatherstation.data.Device
import m.kampukter.homeweatherstation.data.Sensor

class DeviceJsonAdapter : TypeAdapter<Device>() {
    override fun read(reader: JsonReader): Device {
        var fieldName: String? = null
        var deviceIdValue = ""
        val intermediateSensors = mutableListOf<Sensor>()
        reader.beginObject()
        while (reader.hasNext()) {
            if (JsonToken.NAME == reader.peek()) {
                fieldName = reader.nextName()
            }
            if (fieldName == "deviceId") {
                //
                //Log.d("blabla", "peek - ${reader.peek()}")
                deviceIdValue = reader.nextString()
            }
            if (fieldName == "sensorsData") {
                //
                //Log.d("blabla", "peek - ${reader.peek()}")
                reader.beginArray()
                while (reader.hasNext()) {
                    reader.beginObject()
                    //-----
                    var idValue = ""
                    var myField = ""
                    var myFieldValue = 0.0
                    var myStateFieldValue = false
                    while (reader.hasNext()) {
                        when(val name = reader.nextName()) {
                            "id" -> idValue = reader.nextString()
                            "state" -> {
                                myField = name
                                myStateFieldValue = reader.nextBoolean()
                            }
                            else -> {
                                myField = name
                                myFieldValue = reader.nextDouble()
                            }
                        }
                    }
                    when (myField) {
                        "mmHg" -> intermediateSensors.add(
                            Sensor.Pressure(
                                idValue,
                                myFieldValue.toFloat()
                            )
                        )
                        "percentage" -> intermediateSensors.add(
                            Sensor.Humidity(
                                idValue,
                                myFieldValue.toFloat()
                            )
                        )
                        "degrees" -> intermediateSensors.add(
                            Sensor.Temperature(
                                idValue,
                                myFieldValue.toFloat()
                            )
                        )
                        "volts" -> intermediateSensors.add(
                            Sensor.Voltage(
                                idValue,
                                myFieldValue.toFloat()
                            )
                        )
                        "amperes" -> intermediateSensors.add(
                            Sensor.Amperage(
                                idValue,
                                myFieldValue.toFloat()
                            )
                        )
                        "state" -> intermediateSensors.add(
                            Sensor.Relay(
                                idValue,
                                myStateFieldValue
                            )
                        )
                    }
                    //-----
                    reader.endObject()
                }
                reader.endArray()
            }
        }
        reader.endObject()
        return Device(
            deviceId = deviceIdValue,
            sensorsData = intermediateSensors
        )
    }

    override fun write(out: JsonWriter?, value: Device?) {
    }

}