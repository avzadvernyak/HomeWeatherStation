package m.kampukter.homeweatherstation.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import m.kampukter.homeweatherstation.data.InfoSensor
import m.kampukter.homeweatherstation.data.RequestPeriod
import m.kampukter.homeweatherstation.data.ResultInfoSensor
import m.kampukter.homeweatherstation.data.SensorInf
import retrofit2.Call
import retrofit2.Callback

class InfoSensorRepository {

    private val apiInfoSensor = ApiInfoSensor.create()
    val result = MutableLiveData<ResultInfoSensor>()
    fun getDataPeriod(request: RequestPeriod): LiveData<ResultInfoSensor> {
        val call = apiInfoSensor.getInfoSensorPeriod(request.unit, request.dateBegin, request.dateEnd)
        call.enqueue(object : Callback<List<InfoSensor>> {
            override fun onResponse(call: Call<List<InfoSensor>>, response: retrofit2.Response<List<InfoSensor>>) {
                //response?.body().let { result.postValue(response?.body()) }
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.first()?.date != 0L)
                        body?.let { result.postValue(ResultInfoSensor.Success(it)) }
                    else result.postValue(ResultInfoSensor.EmptyResponse)
                } else result.postValue(ResultInfoSensor.OtherError("isSuccessful is false"))
            }

            override fun onFailure(call: Call<List<InfoSensor>>, t: Throwable) {
                t.message?.let {
                    result.postValue(ResultInfoSensor.OtherError(it))
                }
            }
        })
        return result
    }

    fun getInfoBySensor(sensorId: String): SensorInf? {
        var retValue: SensorInf? = null
        val sensors: List<SensorInf> = listOf(
            SensorInf("TempOutdoor", "°C", "ESP8266-10", 50.0, -35.0),
            SensorInf("TempIndoor", "°C", "ESP8266-11",50.0,-5.0),
            SensorInf("TempGuestRoom", "°C", "ESP8266-21",50.0,5.0),
            SensorInf("Voltage", "°mV", "ESP8266-22",30000.0,0.0),
            SensorInf("Amperage", "°mA", "ESP8266-23",4000.0,0.0),
            SensorInf("Pressure", "mm Hg", "ESP8266-12",770.0,720.0),
            SensorInf("Humidity", "%", "ESP8266-13",100.0,0.0)
        )

        sensors.find { it.id == sensorId }?.let {
            retValue = it
        }
        return retValue
    }
}