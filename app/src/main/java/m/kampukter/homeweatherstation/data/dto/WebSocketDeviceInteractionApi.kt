package m.kampukter.homeweatherstation.data.dto

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import m.kampukter.homeweatherstation.data.Device
import okhttp3.*
import java.net.URL
import java.util.concurrent.TimeUnit

fun WebSocket.getUrl(): URL = request().url().url()

class WebSocketDeviceInteractionApi : DeviceInteractionApi {

    private var isDisconnect = mutableMapOf<URL, Boolean>()

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    private var webSockets = mutableMapOf<URL, WebSocket>()
    private val connectionStatusLiveDatas =
        mutableMapOf<URL, MutableLiveData<DeviceInteractionApi.ConnectionStatus>>()
    private val messageLiveDatas =
        mutableMapOf<URL, MutableLiveData<DeviceInteractionApi.Message>>()

    private val deviceJsonAdapter = DeviceJsonAdapter()

    private val webSocketListener = object : WebSocketListener() {

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)

            connectionStatusLiveDatas[webSocket.getUrl()]?.postValue(DeviceInteractionApi.ConnectionStatus.Disconnected)
            webSockets.remove(webSocket.getUrl())

            /*isDisconnect[webSocket.getUrl()]?.let {
                if (!it) disconnect(webSocket.getUrl())
            }*/
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {

        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            connectionStatusLiveDatas[webSocket.getUrl()]?.postValue(
                DeviceInteractionApi.ConnectionStatus.Failed(
                    t.message
                )
            )
            webSockets.remove(webSocket.getUrl())
        }

        override fun onMessage(webSocket: WebSocket, text: String) {

            connectionStatusLiveDatas[webSocket.getUrl()]?.postValue(DeviceInteractionApi.ConnectionStatus.Connected)
            if (text != "Connected") {
                val unitInfo =
                    GsonBuilder().registerTypeAdapter(Device::class.java, deviceJsonAdapter)
                        .create()
                        .fromJson(text, Device::class.java)
                messageLiveDatas[webSocket.getUrl()]?.postValue(
                    DeviceInteractionApi.Message.DeviceInfo(unitInfo)
                )
            } else messageLiveDatas[webSocket.getUrl()]?.postValue(
                DeviceInteractionApi.Message.Text(
                    text
                )
            )
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            connectionStatusLiveDatas[webSocket.getUrl()]?.postValue(DeviceInteractionApi.ConnectionStatus.Connected)
        }
    }

    override fun connect(url: URL) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectionStatusLiveDatas.putIfAbsent(url,
                MutableLiveData<DeviceInteractionApi.ConnectionStatus>()
                    .apply { postValue(DeviceInteractionApi.ConnectionStatus.Connecting) })
            messageLiveDatas.putIfAbsent(url, MutableLiveData())
        } else {
            if (connectionStatusLiveDatas.containsKey(url)) {
                if (connectionStatusLiveDatas[url] == null) connectionStatusLiveDatas[url]?.postValue(
                    DeviceInteractionApi.ConnectionStatus.Connecting
                )
            } else {
                connectionStatusLiveDatas[url] =
                    MutableLiveData<DeviceInteractionApi.ConnectionStatus>()
                        .apply {
                            postValue(DeviceInteractionApi.ConnectionStatus.Connecting)
                        }
            }

            if (messageLiveDatas.containsKey(url)) {
                if (messageLiveDatas[url] == null) messageLiveDatas[url] = MutableLiveData()
            } else messageLiveDatas[url] = MutableLiveData()
        }
        isDisconnect[url] = false
        if (!webSockets.containsKey(url)) {
            connectionStatusLiveDatas[url]?.postValue(
                DeviceInteractionApi.ConnectionStatus.Connecting
            )
            webSockets[url] = okHttpClient.newWebSocket(
                Request.Builder().url(url).build(),
                webSocketListener
            )
        }
    }

    override fun disconnect(url: URL) {
        isDisconnect[url] = true
        GlobalScope.launch {
            delay(10000)
            isDisconnect[url]?.let {
                if (it) {
                    webSockets[url]?.close(1000, null)
                    webSockets.remove(url)
                    connectionStatusLiveDatas[url]?.postValue(DeviceInteractionApi.ConnectionStatus.Closing)
                }
            }
        }
    }


    override fun commandSend(url: URL, command: String) {
        webSockets[url]?.send(command)
    }

    override fun getConnectStatusLiveData(url: URL): LiveData<DeviceInteractionApi.ConnectionStatus>? =
        connectionStatusLiveDatas[url]

    override fun getMessageLiveData(url: URL): LiveData<DeviceInteractionApi.Message>? =
        messageLiveDatas[url]

}
