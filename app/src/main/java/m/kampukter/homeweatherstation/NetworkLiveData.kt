package m.kampukter.homeweatherstation

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import androidx.lifecycle.LiveData

object NetworkLiveData : LiveData<String>() {

    private lateinit var application: Application
    private lateinit var networkRequest: NetworkRequest

    override fun onActive() {
        super.onActive()
        getDetails()
    }

    fun init(application: Application) {
        this.application = application
        networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = application.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    fun getSSID(): String? = if (isNetworkAvailable()) {
        (application.getSystemService(Context.WIFI_SERVICE) as WifiManager).connectionInfo.ssid
    } else null


    private fun getDetails() {

        val wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connectivityManager =
            application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(
            networkRequest,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network?) {
                    super.onAvailable(network)
                    postValue(wifiManager.connectionInfo.ssid)
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    postValue(null)
                }

                override fun onLost(network: Network?) {
                    super.onLost(network)
                    postValue(null)
                }
            })
    }

}