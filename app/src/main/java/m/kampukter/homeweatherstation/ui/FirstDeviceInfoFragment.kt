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
import kotlinx.android.synthetic.main.first_device_info_fragment.*
import kotlinx.android.synthetic.main.first_device_info_fragment.is_switch_of_bulb_off
import kotlinx.android.synthetic.main.first_device_info_fragment.is_switch_of_bulb_on
import kotlinx.android.synthetic.main.status_info_bar.view.*
import m.kampukter.homeweatherstation.Constants.EXTRA_MESSAGE
import m.kampukter.homeweatherstation.MyViewModel
import m.kampukter.homeweatherstation.R
import m.kampukter.homeweatherstation.data.Sensor
import m.kampukter.homeweatherstation.data.dto.DeviceInteractionApi
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.net.URL

class FirstDeviceInfoFragment : Fragment() {
    private lateinit var siteURL: URL
    private val viewModel by sharedViewModel<MyViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.first_device_info_fragment, container, false)
    }

    override fun onResume() {
        super.onResume()
        viewModel.urlSet(siteURL)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        siteURL = URL(getString(R.string.firstServerUrl))

        temperatureOutdoorTextView.text = getString(R.string.no_connect_value)
        temperatureIndoorTextView.text = getString(R.string.no_connect_value)
        pressureTextView.text = getString(R.string.no_connect_value)
        humidityTextView.text = getString(R.string.no_connect_value)

        viewModel.connectStatusWS.observe(this, Observer { status ->

            is_switch_of_bulb_on.hide()
            is_switch_of_bulb_off.hide()

            with(statusFirst) {
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
            //statusFirst.lastUpdateTextView.text = DateFormat.format("HH:mm:ss", Date()).toString()
            when (it) {
                is DeviceInteractionApi.Message.DeviceInfo -> {
                    with(it.content) {
                        for (sensor in sensorsData) {
                            when (sensor) {
                                is Sensor.Relay -> {
                                    if (sensor.id == "1") {
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
                                    if (sensor.id == "1") {
                                        temperatureOutdoorTextView.text =
                                            if (sensor.degrees != -270.0F)
                                                getString(
                                                    R.string.temperatureOutdoorSensorValue,
                                                    sensor.degrees.toString()
                                                ) else "\u221E"
                                    } else if ((sensor.id == "2")) {
                                        temperatureIndoorTextView.text =
                                            if (sensor.degrees != -270.0F) getString(
                                                R.string.temperatureIndoorSensorValue,
                                                sensor.degrees.toString()
                                            ) else "\u221E"
                                    }
                                }
                                is Sensor.Humidity -> {
                                    if (sensor.id == "1") {
                                        humidityTextView.text =
                                            if (sensor.percentage != -270.0F) getString(
                                                R.string.humiditySensorValue,
                                                sensor.percentage.toString()
                                            ) else "\u221E"
                                    }
                                }
                                is Sensor.Pressure -> {
                                    if (sensor.id == "1") {
                                        pressureTextView.text = getString(
                                            R.string.pressureSensorValue,
                                            sensor.mmHg.toString()
                                        )
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
        graphHumidityImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    GraphSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE, "Humidity") }
            )
        }
        listHumidityImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    ListSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE, "Humidity") }
            )
        }
        graphTempOutdoorImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    GraphSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE, "TempOutdoor") }
            )
        }
        listTempOutdoorImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    ListSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE, "TempOutdoor") }
            )
        }
        graphPressureImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    GraphSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE, "Pressure") }
            )
        }
        listPressureImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    ListSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE, "Pressure") }
            )
        }
        graphTemperatureIndoorImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    GraphSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE, "TempIndoor") }
            )
        }
        listTemperatureIndoorImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    ListSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE, "TempIndoor") }
            )
        }
    }

    companion object {
        fun newInstance(): FirstDeviceInfoFragment {
            return FirstDeviceInfoFragment()
        }
    }
}