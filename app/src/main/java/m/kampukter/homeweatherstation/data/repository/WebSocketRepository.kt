package m.kampukter.homeweatherstation.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import m.kampukter.homeweatherstation.data.dao.SensorInformationDao
import m.kampukter.homeweatherstation.data.dto.DeviceInteractionApi
import java.net.URL

class WebSocketRepository(
    private val webSocketDto: DeviceInteractionApi,
    private val sensorInformationDao: SensorInformationDao
) {

    fun webSocketConnect(url: URL) = webSocketDto.connect(url)
    fun connectToAllDevices() {
        GlobalScope.launch(context = Dispatchers.IO) {
            sensorInformationDao.getAllUrl().forEach { url -> webSocketDto.connect(url) }
        }
    }

    fun disconnectToAllDevices() {
        GlobalScope.launch(context = Dispatchers.IO) {
            sensorInformationDao.getAllUrl().forEach { url -> webSocketDto.disconnect(url) }
        }
    }

    fun webSocketStatus(url: URL) = webSocketDto.getConnectStatusLiveData(url)

    fun getMessage(url: URL) = webSocketDto.getMessageLiveData(url)

    fun commandSend(url: URL, command: String) = webSocketDto.commandSend(url, command)

}