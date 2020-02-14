package m.kampukter.homeweatherstation.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.main_activity.*
import m.kampukter.homeweatherstation.Constants.FIRST_LOCAL_URL
import m.kampukter.homeweatherstation.Constants.FIRST_URL
import m.kampukter.homeweatherstation.Constants.LOCAL_SSID
import m.kampukter.homeweatherstation.Constants.SECOND_LOCAL_URL
import m.kampukter.homeweatherstation.Constants.SECOND_URL
import m.kampukter.homeweatherstation.MyViewModel
import m.kampukter.homeweatherstation.NetworkLiveData
import m.kampukter.homeweatherstation.R
import m.kampukter.homeweatherstation.data.dto.DeviceInteractionApi
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModel<MyViewModel>()

    lateinit var siteFirstURL: URL
    private lateinit var siteSecondURL: URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Smart House"
        }

        NetworkLiveData.init(application)
        if (NetworkLiveData.getSSID() == LOCAL_SSID) {
            siteFirstURL = URL(FIRST_LOCAL_URL)
            siteSecondURL = URL(SECOND_LOCAL_URL)
        } else {
            siteFirstURL = URL(FIRST_URL)
            siteSecondURL = URL(SECOND_URL)
        }
        viewModel.setFirstURL(siteFirstURL)
        viewModel.setSecondURL(siteSecondURL)
        /*NetworkLiveData.observe(this, Observer {
            Log.d("blablabla", "NetworkLiveData.observe $it")
            if (it == LOCAL_SSID) {
                siteFirstURL = URL(FIRST_LOCAL_URL)
                siteSecondURL = URL(SECOND_LOCAL_URL)
                Log.d("blablabla", "LAN")
            } else {
                Log.d("blablabla", "WAN")
                siteFirstURL = URL(FIRST_URL)
                siteSecondURL = URL(SECOND_URL)
            }
            viewModel.urlSet(siteFirstURL)
            viewModel.urlSet(siteSecondURL)

            viewModel.connectWS(siteFirstURL)
            viewModel.connectWS(siteSecondURL)

            viewModel.setFirstURL(siteFirstURL)
             viewModel.setSecondURL(siteSecondURL)

        })*/


        viewPager.adapter = DeviceWSPagerAdapter(supportFragmentManager)

        lateinit var currentURL: URL
        viewModel.urlWS.observe(this, Observer {
            currentURL = it
        })

        viewModel.connectStatusWS.observe(this, Observer { status ->
            statusTextView.visibility = View.INVISIBLE
            failedTextView.visibility = View.INVISIBLE
            failedMsgTextView.visibility = View.INVISIBLE
            materialIconButton.visibility = View.INVISIBLE
            BottomSheetBehavior.from(statusConstraint).state = BottomSheetBehavior.STATE_COLLAPSED
            when (status) {
                is DeviceInteractionApi.ConnectionStatus.Connected -> {
                    statusTextView.visibility = View.VISIBLE
                    statusTextView.text =
                        getString(R.string.connection_changed_message, "Connected")
                }
                is DeviceInteractionApi.ConnectionStatus.Disconnected -> {
                    statusTextView.visibility = View.VISIBLE
                    statusTextView.text =
                        getString(R.string.connection_changed_message, "Disconnected")
                }
                is DeviceInteractionApi.ConnectionStatus.Connecting -> {
                    statusTextView.visibility = View.VISIBLE
                    statusTextView.text = getString(R.string.connect_msg_connecting)
                }
                is DeviceInteractionApi.ConnectionStatus.Closing -> {
                    statusTextView.visibility = View.VISIBLE
                    statusTextView.text = getString(R.string.connect_msg_closing)
                }
                is DeviceInteractionApi.ConnectionStatus.Failed -> {
                    failedTextView.visibility = View.VISIBLE
                    failedMsgTextView.visibility = View.VISIBLE
                    materialIconButton.visibility = View.VISIBLE
                    failedTextView.text = getString(R.string.connect_msg_failed)
                    failedMsgTextView.text = status.reason

                    materialIconButton.setOnClickListener {
                        materialIconButton.visibility = View.INVISIBLE
                        viewModel.connectWS(currentURL)
                    }

                }
            }

        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.connectWS(siteFirstURL)
        viewModel.connectWS(siteSecondURL)
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnectWS(siteFirstURL)
        viewModel.disconnectWS(siteSecondURL)
    }
}