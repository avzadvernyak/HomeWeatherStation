package m.kampukter.homeweatherstation.repository

import android.text.format.DateFormat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import m.kampukter.homeweatherstation.data.UnitSensorInfo
import m.kampukter.homeweatherstation.data.SiteAddress
import okhttp3.*
import java.util.*
import java.util.concurrent.TimeUnit

class WebSocketRepository {

    private val siteAddressList = listOf(
        SiteAddress(1, "ws://109.254.66.131:81"),
        SiteAddress(2, "ws://192.168.0.82:81")
    )

    private val _isConnectToWS = MutableLiveData<Boolean>()
    val isConnectToWS: LiveData<Boolean>
        get() = _isConnectToWS

    var isConnect: Boolean = false
        set(value) {
            _isConnectToWS.postValue(value)
        }

    private val _currentSiteInfo = MutableLiveData<SiteAddress>()
    val currentSiteInfo: LiveData<SiteAddress>
        get() = _currentSiteInfo

    private val _sensorInfo = MutableLiveData<UnitSensorInfo>()
    val sensorInfo: LiveData<UnitSensorInfo>
        get() = _sensorInfo

    private var ws: WebSocket? = null

    private fun getSiteById(siteId: Int): SiteAddress? {
        var retValue: SiteAddress? = null
        siteAddressList.find { it.id == siteId }?.let {
            retValue = it
        }
        return retValue
    }

    fun setCurrentSite(siteId: Int?) {
        var retSiteId = 1
        _currentSiteInfo.value?.let { retSiteId =it.id }
        siteId?.let { id -> retSiteId = id }
        getSiteById(retSiteId)?.let { _currentSiteInfo.postValue(it) }
    }

    fun startWebsocked(siteId: Int) {
        if (!isConnect) {
            getSiteById(siteId)?.let {
                myWebSocket(it.url)
                Log.d("blablabla", "Start WS")
            }
        }
    }

    fun sendCommandToWS(commandString: String) {
        ws?.send(commandString)
    }

    fun stopWebsocked() {
        Log.d("blablabla", "Stop WS")
        ws?.close(1000, null)
    }

    private fun myWebSocket(workUrl: String) {

        val client: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url(workUrl)
            .build()
        val wsListener = EchoWebSocketListener()

        client.newWebSocket(request, wsListener).run {
            ws = this
        }
    }

    inner class EchoWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            isConnect = true
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            var errorMessage: String
            t.let { errorMessage = it.toString() }

            isConnect = false
            Log.d("blablabla", "Connection Error - $errorMessage")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            if (text == "Connected") {
                Log.d("blablabla", "Connected : $text")
                isConnect = true
            } else {
                Log.d("blablabla", "result - $text")
                val result = Gson().fromJson(text, UnitSensorInfo::class.java)
                Log.d("blablabla", "result - $result IP - ")
                _sensorInfo.postValue(result)
                Log.d(
                    "blablabla", "Latest receipt from ${result.unit} in ${DateFormat.format(
                        "HH:mm:ss",
                        Date()
                    )}"
                )
            }
        }

        override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
            webSocket?.close(1000, null)
            isConnect = false
        }
    }
}