package m.kampukter.homeweatherstation

import android.util.Log
import androidx.lifecycle.*
import m.kampukter.homeweatherstation.data.RequestPeriod
import m.kampukter.homeweatherstation.data.ResultInfoSensor
import m.kampukter.homeweatherstation.data.SensorInf
import m.kampukter.homeweatherstation.data.dto.DeviceInteractionApi
import m.kampukter.homeweatherstation.data.repository.InfoSensorRepository
import m.kampukter.homeweatherstation.data.repository.WebSocketRepository
import java.net.URL

class MyViewModel(
    private val webSocketRepository: WebSocketRepository,
    private val infoSensorRepository: InfoSensorRepository
) : ViewModel() {
    // Локально или нет
    private val _firstURL = MutableLiveData<URL>()
    private val _secondURL = MutableLiveData<URL>()
    val firstURL: LiveData<URL> get() = _firstURL
    val secondURL: LiveData<URL> get() = _secondURL
    fun setFirstURL(url: URL) {
        _firstURL.postValue(url)
    }

    fun setSecondURL(url: URL) {
        _secondURL.postValue(url)
    }

    // New все что касается WS
    fun connectWS(url: URL) = webSocketRepository.webSocketConnect(url)

    fun disconnectWS(url: URL) = webSocketRepository.webSocketDisconnect(url)
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
    private val searchData = MutableLiveData<RequestPeriod>()
    val infoSensor: LiveData<ResultInfoSensor> = Transformations.switchMap(searchData) { question ->
        infoSensorRepository.getDataPeriod(question)
    }

    fun getQuestionInfoSensor(setPeriod: RequestPeriod) = searchData.postValue(setPeriod)

    fun getInfoBySensor(sensorId: String): SensorInf? =
        infoSensorRepository.getInfoBySensor(sensorId)

}
