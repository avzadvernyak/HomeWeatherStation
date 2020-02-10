package m.kampukter.homeweatherstation.data.dto

import androidx.lifecycle.LiveData
import m.kampukter.homeweatherstation.data.Device
import java.net.URL

interface DeviceInteractionApi {
    fun connect(url: URL)

    fun disconnect(url: URL)

    fun commandSend(url: URL, command: String)

    fun getConnectStatusLiveData(url: URL): LiveData<ConnectionStatus>?

    fun getMessageLiveData(url: URL): LiveData<Message>?

    sealed class ConnectionStatus {
        object Connecting : ConnectionStatus()
        object Connected : ConnectionStatus()
        object Closing : ConnectionStatus()
        object Disconnected : ConnectionStatus()
        data class Failed(val reason: String?) : ConnectionStatus()
    }

    sealed class Message {
        data class Text(val content: String) : Message()
        data class DeviceInfo(val content: Device) : Message()
    }
}
