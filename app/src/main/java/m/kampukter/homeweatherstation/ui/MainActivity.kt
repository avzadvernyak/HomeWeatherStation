package m.kampukter.homeweatherstation.ui

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.main_activity.*
import m.kampukter.homeweatherstation.Constants.LAN
import m.kampukter.homeweatherstation.Constants.LOCAL_SSID
import m.kampukter.homeweatherstation.Constants.WAN
import m.kampukter.homeweatherstation.MyViewModel
import m.kampukter.homeweatherstation.NetworkLiveData
import m.kampukter.homeweatherstation.R
import m.kampukter.homeweatherstation.data.dto.DeviceInteractionApi
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModel<MyViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var isHiddenSheet: Boolean

        setContentView(R.layout.main_activity)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Smart House"
        }

        NetworkLiveData.init(application)
        viewModel.setNetwork(
            if (NetworkLiveData.getSSID() == LOCAL_SSID) LAN
            else WAN
        )

        val adapter = DeviceWSPagerAdapter(supportFragmentManager)
        viewModel.devices.observe(this, Observer {
            adapter.setListDevice(it)
            viewModel.setDevice(adapter.getListDevice(0))
        })

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                viewModel.setDevice(adapter.getListDevice(position))
            }
        })
        viewPager.adapter = adapter

        viewModel.connectStatusWS.observe(this, Observer { status ->
            statusTextView.visibility = View.INVISIBLE
            failedTextView.visibility = View.INVISIBLE
            failedMsgTextView.visibility = View.INVISIBLE
            materialIconButton.visibility = View.INVISIBLE


            when (status) {
                is DeviceInteractionApi.ConnectionStatus.Connected -> {
                    statusTextView.visibility = View.VISIBLE
                    statusTextView.text =
                        getString(R.string.connection_changed_message, "Connected")
                    isHiddenSheet = true
                    Handler().postDelayed({
                        if (isHiddenSheet) BottomSheetBehavior.from(statusConstraint).state =
                            BottomSheetBehavior.STATE_HIDDEN
                    }, 5000)

                }
                is DeviceInteractionApi.ConnectionStatus.Failed -> {
                    isHiddenSheet = false
                    BottomSheetBehavior.from(statusConstraint).state =
                        BottomSheetBehavior.STATE_COLLAPSED

                    failedTextView.visibility = View.VISIBLE
                    failedMsgTextView.visibility = View.VISIBLE
                    materialIconButton.visibility = View.VISIBLE
                    failedTextView.text = getString(R.string.connect_msg_failed)
                    failedMsgTextView.text = status.reason

                    materialIconButton.setOnClickListener {
                        materialIconButton.visibility = View.INVISIBLE
                        viewModel.connectToDevices()
                    }

                }
                is DeviceInteractionApi.ConnectionStatus.Disconnected -> {
                    isHiddenSheet =
                        showStatus(getString(R.string.connection_changed_message, "Disconnected"))
                }
                is DeviceInteractionApi.ConnectionStatus.Connecting -> {
                    isHiddenSheet = showStatus(getString(R.string.connect_msg_connecting))
                }
                is DeviceInteractionApi.ConnectionStatus.Closing -> {
                    isHiddenSheet = showStatus(getString(R.string.connect_msg_closing))
                }
            }

        })

    }

    override fun onResume() {
        super.onResume()
        viewModel.connectToDevices()
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnectToAllDevices()
    }

    private fun showStatus(msg: String): Boolean {
        statusTextView.visibility = View.VISIBLE
        statusTextView.text = msg
        BottomSheetBehavior.from(statusConstraint).state =
            BottomSheetBehavior.STATE_COLLAPSED
        return false
    }
}