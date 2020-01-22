package m.kampukter.homeweatherstation

import androidx.lifecycle.*
import m.kampukter.homeweatherstation.data.RequestPeriod
import m.kampukter.homeweatherstation.data.ResultInfoSensor
import m.kampukter.homeweatherstation.data.Sensor
import m.kampukter.homeweatherstation.repository.InfoSensorRepository
import m.kampukter.homeweatherstation.repository.WebSocketRepository

class MyViewModel(
    private val webSocketRepository: WebSocketRepository,
    private val infoSensorRepository: InfoSensorRepository
) : ViewModel() {
    // Это все что касается WS
    fun startWS(idSite: Int) = webSocketRepository.startWebsocked(idSite)

    fun stopWS() = webSocketRepository.stopWebsocked()
    fun setCurrentSite(idSite: Int?) = webSocketRepository.setCurrentSite(idSite)
    fun sendCommandToWs(command: String) = webSocketRepository.sendCommandToWS(command)
    val sensorInfo = webSocketRepository.sensorInfo
    val isStatusWS = webSocketRepository.isConnectToWS
    val currentSiteInfo = webSocketRepository.currentSiteInfo

    // Это все что касается сохраненных на сайте данных
    private val searchData = MutableLiveData<RequestPeriod>()
    val infoSensor: LiveData<ResultInfoSensor> = Transformations.switchMap(searchData) { question ->
        infoSensorRepository.getDataPeriod(question)
    }

    fun getQuestionInfoSensor(setPeriod: RequestPeriod) = searchData.postValue(setPeriod)

    fun getInfoBySensor(sensorId: String): Sensor? = infoSensorRepository.getInfoBySensor(sensorId)

}
