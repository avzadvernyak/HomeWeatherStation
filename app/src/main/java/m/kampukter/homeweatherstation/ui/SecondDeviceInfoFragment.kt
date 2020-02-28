package m.kampukter.homeweatherstation.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.second_device_info_fragment.*
import m.kampukter.homeweatherstation.MyViewModel
import m.kampukter.homeweatherstation.R
import m.kampukter.homeweatherstation.data.Sensor
import m.kampukter.homeweatherstation.data.dto.DeviceInteractionApi
import org.koin.androidx.viewmodel.ext.android.viewModel

class SecondDeviceInfoFragment : Fragment() {

    private val fragmentViewModel by viewModel<MyViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.second_device_info_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString("ARG_DEVICE_NAME")?.let {
            fragmentViewModel.setDeviceName(it)
        }

        fragmentViewModel.connectionStatusLiveData.observe(this, Observer {
            visibleLightingProgressBar()
        })
        fragmentViewModel.webSocketMessageLiveData.observe(this, androidx.lifecycle.Observer {
            when (it) {
                is DeviceInteractionApi.Message.DeviceInfo -> {
                    with(it.content) {
                        for (sensor in sensorsData) {
                            when (sensor) {
                                is Sensor.Relay -> {
                                    if (sensor.id == "3") {
                                        progressBar.visibility = View.INVISIBLE
                                        lightingOnImageBottom.visibility = View.INVISIBLE
                                        lightingOffImageBottom.visibility = View.INVISIBLE
                                        if (sensor.state) {
                                            lightingOnImageBottom.visibility = View.VISIBLE
                                        } else {
                                            lightingOffImageBottom.visibility = View.VISIBLE
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
        fragmentViewModel.isCommandSentLiveData.observe(this, Observer { visibleLightingProgressBar() })

        lightingOnImageBottom.setOnClickListener {
            fragmentViewModel.sendCommandToDevice("Relay1Off")
        }
        lightingOffImageBottom.setOnClickListener {
            fragmentViewModel.sendCommandToDevice("Relay1On")
        }

        graphTemperatureImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    GraphSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE_SENSOR_GRAPH, "ESP8266-21") }
            )
        }
        listTemperatureImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    ListSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE_SENSOR_LIST, "ESP8266-21") }
            )
        }
        graphVoltageImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    GraphSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE_SENSOR_GRAPH, "ESP8266-22") }
            )
        }
        listVoltageImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    ListSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE_SENSOR_LIST, "ESP8266-22") }
            )
        }
        graphAmperageImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    GraphSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE_SENSOR_GRAPH, "ESP8266-23") }
            )
        }
        listAmperageImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    ListSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE_SENSOR_LIST, "ESP8266-23") }
            )
        }

    }

    private fun visibleLightingProgressBar() {

        val noConnect: String = getString(R.string.no_connect_value)

        progressBar.visibility = View.VISIBLE
        lightingOnImageBottom.visibility = View.INVISIBLE
        lightingOffImageBottom.visibility = View.INVISIBLE

        temperatureTextView.text = noConnect
        voltageTextView.text = noConnect
        amperageTextView.text = noConnect
    }

    companion object {
        private const val ARG_DEVICE_NAME = "ARG_DEVICE_NAME"
        fun newInstance(deviceName: String): SecondDeviceInfoFragment {
            return SecondDeviceInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DEVICE_NAME, deviceName)
                }
            }
        }
    }
}