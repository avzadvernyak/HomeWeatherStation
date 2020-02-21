package m.kampukter.homeweatherstation.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import m.kampukter.homeweatherstation.data.*
import m.kampukter.homeweatherstation.data.dao.SensorInformationDao
import retrofit2.Call
import retrofit2.Callback

class SensorRepository(private val sensorInformationDao: SensorInformationDao) {

    private val apiInfoSensor = ApiInfoSensor.create()
    val result = MutableLiveData<ResultSensorValue>()
    fun getDataPeriod(request: SensorRequest): LiveData<ResultSensorValue> {
        val call =
            apiInfoSensor.getInfoSensorPeriod(request.sensorName, request.dateBegin, request.dateEnd)
        call.enqueue(object : Callback<List<SensorValue>> {
            override fun onResponse(
                call: Call<List<SensorValue>>,
                response: retrofit2.Response<List<SensorValue>>
            ) {
                //response?.body().let { result.postValue(response?.body()) }
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.first()?.date != 0L)
                        body?.let { result.postValue(ResultSensorValue.Success(it)) }
                    else result.postValue(ResultSensorValue.EmptyResponse)
                } else result.postValue(ResultSensorValue.OtherError("isSuccessful is false"))
            }

            override fun onFailure(call: Call<List<SensorValue>>, t: Throwable) {
                t.message?.let {
                    result.postValue(ResultSensorValue.OtherError(it))
                }
            }
        })
        return result
    }

    fun getInfoBySensor(sensor: SensorRequest) =
        sensorInformationDao.getInfoBySensor(sensor.sensorName)

}