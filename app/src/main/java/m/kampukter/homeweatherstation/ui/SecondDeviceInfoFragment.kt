package m.kampukter.homeweatherstation.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.second_device_info_fragment.*
import kotlinx.android.synthetic.main.status_info_bar.view.*
import m.kampukter.homeweatherstation.MyViewModel
import m.kampukter.homeweatherstation.R
import m.kampukter.homeweatherstation.data.Sensor
import m.kampukter.homeweatherstation.data.dto.DeviceInteractionApi
import java.net.URL
import m.kampukter.homeweatherstation.Constants.EXTRA_MESSAGE
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SecondDeviceInfoFragment : Fragment() {

    private lateinit var siteURL: URL

    private val viewModel by sharedViewModel<MyViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.second_device_info_fragment, container, false)
    }

    override fun onResume() {
        super.onResume()
        viewModel.urlSet(siteURL)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        siteURL = URL(getString(R.string.secondServerUrl))

        temperatureTextView.text = getString(R.string.no_connect_value)
        voltageTextView.text = getString(R.string.no_connect_value)
        amperageTextView.text = getString(R.string.no_connect_value)

        viewModel.connectStatusWS.observe(this, Observer { status ->

            is_switch_of_bulb_on.hide()
            is_switch_of_bulb_off.hide()

            with(statusSecond) {
                statusTextView.visibility = View.INVISIBLE
                failedTextView.visibility = View.INVISIBLE
                failedMsgTextView.visibility = View.INVISIBLE
                materialIconButton.visibility = View.INVISIBLE
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
                        statusTextView.text = "Connecting..."
                    }
                    is DeviceInteractionApi.ConnectionStatus.Closing -> {
                        statusTextView.visibility = View.VISIBLE
                        statusTextView.text = "Closing..."
                    }
                    is DeviceInteractionApi.ConnectionStatus.Failed -> {
                        failedTextView.visibility = View.VISIBLE
                        failedMsgTextView.visibility = View.VISIBLE
                        materialIconButton.visibility = View.VISIBLE
                        failedTextView.text = "Failed"
                        failedMsgTextView.text = status.reason
                        materialIconButton.setOnClickListener {
                            materialIconButton.visibility = View.INVISIBLE
                            viewModel.disconnectWS(
                                siteURL
                            )
                            Handler().postDelayed({
                                viewModel.connectWS(
                                    siteURL
                                )
                            }, 3000)
                        }
                    }

                }
            }
        })
        viewModel.messageWS.observe(this, androidx.lifecycle.Observer {
            when (it) {
                is DeviceInteractionApi.Message.DeviceInfo -> {
                    with(it.content) {
                        for (sensor in sensorsData) {
                            when (sensor) {
                                is Sensor.Relay -> {
                                    if (sensor.id == "3") {
                                        if (sensor.state) {
                                            is_switch_of_bulb_off.hide()
                                            is_switch_of_bulb_on.show()
                                        } else {
                                            is_switch_of_bulb_off.show()
                                            is_switch_of_bulb_on.hide()
                                        }
                                    }
                                }
                                is Sensor.Temperature -> {
                                    if (sensor.id == "3") temperatureTextView.text =
                                        getString(
                                            R.string.temperatureOutdoorSensorValue,
                                            sensor.degrees.toString()
                                        )

                                }
                                is Sensor.Voltage -> {
                                    if (sensor.id == "1") {
                                        voltageTextView.text = if (sensor.volts != -270.0F)
                                            getString(
                                                R.string.voltageSensorValue,
                                                sensor.volts.toString()
                                            ) else getString(R.string.no_connect_value)
                                    }
                                }
                                is Sensor.Amperage -> {
                                    if (sensor.id == "1") {
                                        amperageTextView.text = if (sensor.amperes != -270.0F)
                                            getString(
                                                R.string.amperageSensorValue,
                                                sensor.amperes.toString()
                                            ) else getString(R.string.no_connect_value)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })
        is_switch_of_bulb_off.setOnClickListener {
            is_switch_of_bulb_off.hide()
            viewModel.commandSend(siteURL, "Relay1On")
        }
        is_switch_of_bulb_on.setOnClickListener {
            is_switch_of_bulb_on.hide()
            viewModel.commandSend(siteURL, "Relay1Off")
        }
        graphTemperatureImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    GraphSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE, "TempGuestRoom") }
            )
        }
        listTemperatureImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    ListSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE, "TempGuestRoom") }
            )
        }
        graphVoltageImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    GraphSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE, "Voltage") }
            )
        }
        listVoltageImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    ListSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE, "Voltage") }
            )
        }
        graphAmperageImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    GraphSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE, "Amperage") }
            )
        }
        listAmperageImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    ListSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE, "Amperage") }
            )
        }
    }

    companion object {
        fun newInstance(): SecondDeviceInfoFragment {
            return SecondDeviceInfoFragment()
        }
    }
}