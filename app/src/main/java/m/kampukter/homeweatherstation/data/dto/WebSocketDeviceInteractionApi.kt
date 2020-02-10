package m.kampukter.homeweatherstation.data.dto

import android.os.Build
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import m.kampukter.homeweatherstation.data.*
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

    private var webSockets = mutableListOf<WebSocket>()
    private val connectionStatusLiveDatas =
        mutableMapOf<URL, MutableLiveData<DeviceInteractionApi.ConnectionStatus>>()
    private val messageLiveDatas =
        mutableMapOf<URL, MutableLiveData<DeviceInteractionApi.Message>>()

    private val deviceJsonAdapter = DeviceJsonAdapter()

    private val webSocketListener = object : WebSocketListener() {

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            isDisconnect[webSocket.getUrl()]?.let {
                if ( !it )disconnect(webSocket.getUrl())
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            connectionStatusLiveDatas[webSocket.getUrl()]?.postValue(DeviceInteractionApi.ConnectionStatus.Disconnected)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            connectionStatusLiveDatas[webSocket.getUrl()]?.postValue(
                DeviceInteractionApi.ConnectionStatus.Failed(
                    t.message
                )
            )
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
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
        isDisconnect[url] = false
        if (webSockets.find { it.request().url().url() == url } == null) {
            webSockets.add(
                okHttpClient.newWebSocket(
                    Request.Builder().url(url).build(),
                    webSocketListener
                )
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectionStatusLiveDatas.putIfAbsent(url,
                MutableLiveData<DeviceInteractionApi.ConnectionStatus>()
                    .apply { postValue(DeviceInteractionApi.ConnectionStatus.Connecting) })
            messageLiveDatas.putIfAbsent(url, MutableLiveData())
        } else {

            if (connectionStatusLiveDatas.containsKey(url)) {
                if (connectionStatusLiveDatas[url] == null) {
                    connectionStatusLiveDatas[url] =
                        MutableLiveData<DeviceInteractionApi.ConnectionStatus>()
                            .apply { postValue(DeviceInteractionApi.ConnectionStatus.Connecting) }
                }
            } else connectionStatusLiveDatas[url] =
                MutableLiveData<DeviceInteractionApi.ConnectionStatus>()
                    .apply { postValue(DeviceInteractionApi.ConnectionStatus.Connecting) }

            if (messageLiveDatas.containsKey(url)) {
                if (messageLiveDatas[url] == null) messageLiveDatas[url] = MutableLiveData()
            } else messageLiveDatas[url] = MutableLiveData()
        }
    }

    override fun disconnect(url: URL) {
        isDisconnect[url] = true
        Handler().postDelayed({
            isDisconnect[url]?.let {
                if (it) {
                    webSockets = webSockets.filter { webSocket ->
                        if (webSocket.request().url().url() == url) {
                            connectionStatusLiveDatas[url]?.postValue(DeviceInteractionApi.ConnectionStatus.Closing)
                            webSocket.close(1000, null)
                            false
                        } else {
                            true
                        }
                    }.toMutableList()
                }
            }
        }, 3000)
    }

    override fun commandSend(url: URL, command: String) {
        webSockets.find { it.getUrl() == url }?.send(command)
        /*
        when (message) {
            is DeviceInteractionApi.Message.Text -> {
                webSockets.find { it.getUrl() == url }?.send(message.content)
            }
        }
         */
    }

    override fun getConnectStatusLiveData(url: URL): LiveData<DeviceInteractionApi.ConnectionStatus>? =
        connectionStatusLiveDatas[url]

    override fun getMessageLiveData(url: URL): LiveData<DeviceInteractionApi.Message>? =
        messageLiveDatas[url]

}