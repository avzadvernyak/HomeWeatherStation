package m.kampukter.homeweatherstation

import androidx.lifecycle.*
import m.kampukter.homeweatherstation.data.SensorRequest
import m.kampukter.homeweatherstation.data.ResultSensorValue
import m.kampukter.homeweatherstation.data.SensorInformation
import m.kampukter.homeweatherstation.data.dto.DeviceInteractionApi
import m.kampukter.homeweatherstation.data.repository.SensorRepository
import m.kampukter.homeweatherstation.data.repository.WebSocketRepository

class MyViewModel(
    private val webSocketRepository: WebSocketRepository,
    private val sensorRepository: SensorRepository
) : ViewModel() {
    // New все что касается WS

    val devices = webSocketRepository.getDevices()

    private val _deviceName = MutableLiveData<String>()
    fun setDevice(name: String) {
        _deviceName.postValue(name)

    }

    fun connectToDevices() {
        webSocketRepository.connectToDevices()
    }

    fun disconnectToAllDevices() {
        webSocketRepository.disconnectToAllDevices()
    }

    val connectStatusWS: LiveData<DeviceInteractionApi.ConnectionStatus> =
        Transformations.switchMap(_deviceName) { name -> webSocketRepository.webSocketStatus(name) }
    val messageWS: LiveData<DeviceInteractionApi.Message> =
        Transformations.switchMap(_deviceName) { name ->
            webSocketRepository.getMessage(name)
        }

    private val lastCommand = MutableLiveData<String>()
    fun sendCommandToWS(command: String) { lastCommand.postValue(command)}
    val command = MediatorLiveData<Any>().apply {
        var lastDeviceName: String? = null
        addSource(_deviceName) { deviceName ->
            lastDeviceName = deviceName
        }
        addSource(lastCommand) { command ->
            lastDeviceName?.let{webSocketRepository.commandSend(it, command)}
        }
    }

    fun setNetwork(network: Int) {
        webSocketRepository.setNetwork(network)
    }
    // Это все что касается сохраненных на сайте данных
    private val searchData = MutableLiveData<SensorRequest>()
    private val resultSensorValue: LiveData<ResultSensorValue> =
        Transformations.switchMap(searchData) { question ->
            sensorRepository.getDataPeriod(question)
        }
    private val sensorInformation: LiveData<SensorInformation> =
        Transformations.switchMap(searchData) { sensorRepository.getInfoBySensor(it) }

    fun setQuestionSensorValue(setSensorRequest: SensorRequest) =
        searchData.postValue(setSensorRequest)

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
