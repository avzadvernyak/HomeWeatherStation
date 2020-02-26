package m.kampukter.homeweatherstation.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import m.kampukter.homeweatherstation.Constants.LAN
import m.kampukter.homeweatherstation.data.dto.DeviceInteractionApi
import java.net.URL

class WebSocketRepository(
    private val webSocketDto: DeviceInteractionApi
) {
    private var network: Int = LAN
    private val devices = mapOf(
        "ESP8266-1" to listOf(URL("http://192.168.0.82:81/"),URL("http://109.254.66.131:81/")),
        "ESP8266-2" to listOf(URL("http://192.168.0.83:81/"),URL("http://109.254.66.131:83/"))
    )
    private val devicesLiveData = MutableLiveData<List<String>>().apply {
        postValue(devices.keys.toList())
    }

    fun getDevices(): LiveData<List<String>> {
        return devicesLiveData
    }

    fun connectToDevices() {
        devices.forEach { webSocketDto.connect(it.value[network]) }
    }

    fun disconnectToAllDevices() {
        devices.forEach { webSocketDto.disconnect(it.value[network]) }
    }

    fun webSocketConnect(name: String) = webSocketDto.connect(getUrlByName(name)[network])
    fun webSocketStatus(name: String) = webSocketDto.getConnectStatusLiveData(getUrlByName(name)[network])

    fun getMessage(name: String) = webSocketDto.getMessageLiveData(getUrlByName(name)[network])

    fun commandSend(name: String, command: String) =
        webSocketDto.commandSend(getUrlByName(name)[network], command)

    private fun getUrlByName(name: String): List<URL> = devices[name] ?: emptyList()
    fun setNetwork(_network: Int) {
        network = _network
    }
}