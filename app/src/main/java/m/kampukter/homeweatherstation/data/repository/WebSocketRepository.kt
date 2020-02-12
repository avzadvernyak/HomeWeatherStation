package m.kampukter.homeweatherstation.data.repository

import m.kampukter.homeweatherstation.data.dto.DeviceInteractionApi
import java.net.URL

class WebSocketRepository(private val webSocketDto: DeviceInteractionApi) {

    fun webSocketConnect(url: URL) = webSocketDto.connect(url)

    fun webSocketDisconnect(url: URL) = webSocketDto.disconnect(url)


    fun webSocketStatus(url: URL) = webSocketDto.getConnectStatusLiveData(url)

    fun getMessage(url: URL) = webSocketDto.getMessageLiveData(url)

    fun commandSend(url: URL, command: String) = webSocketDto.commandSend(url, command)

}