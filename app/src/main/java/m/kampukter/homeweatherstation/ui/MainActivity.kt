package m.kampukter.homeweatherstation.ui

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.main_activity.*
import m.kampukter.homeweatherstation.MyViewModel
import m.kampukter.homeweatherstation.R
import m.kampukter.homeweatherstation.data.dto.DeviceInteractionApi
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModel<MyViewModel>()

    private lateinit var siteFirstURL: URL
    private lateinit var siteSecondURL: URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Smart House"
            //setDisplayHomeAsUpEnabled(true)
            //setDisplayShowHomeEnabled(true)
        }
        siteFirstURL = URL(getString(R.string.firstServerUrl))
        siteSecondURL = URL(getString(R.string.secondServerUrl))

        viewPager.adapter = DeviceWSPagerAdapter(supportFragmentManager)

        lateinit var currentURL: URL
        viewModel.urlWS.observe(this, Observer {
            currentURL = it
        })

        viewModel.connectStatusWS.observe(this, Observer { status ->
            //Log.d("blablabla", "Observer $currentURL")

            //statusConstraint.visibility = View.VISIBLE
            statusTextView.visibility = View.INVISIBLE
            failedTextView.visibility = View.INVISIBLE
            failedMsgTextView.visibility = View.INVISIBLE
            materialIconButton.visibility = View.INVISIBLE
            when (status) {
                is DeviceInteractionApi.ConnectionStatus.Connected -> {
                    statusTextView.visibility = View.VISIBLE
                    statusTextView.text =
                        getString(R.string.connection_changed_message, "Connected")
                    /*
                    Handler().postDelayed({
                        statusConstraint.visibility = View.GONE
                    }, 3000)

                     */
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
        //Log.d("blablabla", "TestActivity->onPause")
        viewModel.disconnectWS(siteFirstURL)
        viewModel.disconnectWS(siteSecondURL)
    }
}