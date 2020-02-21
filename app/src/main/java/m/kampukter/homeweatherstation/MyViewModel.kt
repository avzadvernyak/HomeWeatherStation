package m.kampukter.homeweatherstation

import androidx.lifecycle.*
import m.kampukter.homeweatherstation.data.SensorRequest
import m.kampukter.homeweatherstation.data.ResultSensorValue
import m.kampukter.homeweatherstation.data.SensorInformation
import m.kampukter.homeweatherstation.data.dto.DeviceInteractionApi
import m.kampukter.homeweatherstation.data.repository.SensorRepository
import m.kampukter.homeweatherstation.data.repository.WebSocketRepository
import java.net.URL

class MyViewModel(
    private val webSocketRepository: WebSocketRepository,
    private val sensorRepository: SensorRepository
) : ViewModel() {
    // New все что касается WS
    fun connectWS(url: URL) = webSocketRepository.webSocketConnect(url)
    fun connectToAllDevices(){webSocketRepository.connectToAllDevices()}
    fun disconnectToAllDevices(){webSocketRepository.disconnectToAllDevices()}

    fun commandSend(url: URL, command: String) = webSocketRepository.commandSend(url, command)

    private val _urlWS = MutableLiveData<URL>()
    val urlWS: LiveData<URL> get() = _urlWS
    val connectStatusWS: LiveData<DeviceInteractionApi.ConnectionStatus> =
        Transformations.switchMap(_urlWS) { url -> webSocketRepository.webSocketStatus(url) }
    val messageWS: LiveData<DeviceInteractionApi.Message> =
        Transformations.switchMap(_urlWS) { url ->
            webSocketRepository.getMessage(url)
        }

    fun urlSet(url: URL) {
        _urlWS.postValue(url)
    }

    // Это все что касается сохраненных на сайте данных
    private val searchData = MutableLiveData<SensorRequest>()
    private val resultSensorValue: LiveData<ResultSensorValue> = Transformations.switchMap(searchData) { question ->
        sensorRepository.getDataPeriod(question)
    }
    private val sensorInformation: LiveData<SensorInformation> =
        Transformations.switchMap(searchData) { sensorRepository.getInfoBySensor(it) }

    fun setQuestionSensorValue(setSensorRequest: SensorRequest) = searchData.postValue(setSensorRequest)

    val sensorLiveData: LiveData<Sensor> = MediatorLiveData<Sensor>().apply {

        var lastSensorParams: SensorInformation? = null
        var lastSensorData: ResultSensorValue? = null

        fun update() {
            val sensorParams = lastSensorParams
            val sensorData = lastSensorData
            if (sensorParams == null || sensorData == null) return

            postValue(Sensor(sensorParams, sensorData))
        }

        addSource(sensorInformation) { sensorInformation ->
            lastSensorParams = sensorInformation
            update()
        }

        addSource(resultSensorValue) { sensorData ->
            lastSensorData = sensorData
            update()
        }

    }

    data class Sensor(
        val sensorParams: SensorInformation,
        val sensorData: ResultSensorValue
    )
}
