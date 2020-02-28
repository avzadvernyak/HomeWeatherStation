package m.kampukter.homeweatherstation.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.first_device_info_fragment.*
import kotlinx.android.synthetic.main.first_device_info_fragment.lightingOffImageBottom
import kotlinx.android.synthetic.main.first_device_info_fragment.lightingOnImageBottom
import kotlinx.android.synthetic.main.first_device_info_fragment.progressBar
import m.kampukter.homeweatherstation.MyViewModel
import m.kampukter.homeweatherstation.R
import m.kampukter.homeweatherstation.data.Sensor
import m.kampukter.homeweatherstation.data.dto.DeviceInteractionApi
import org.koin.androidx.viewmodel.ext.android.viewModel

class FirstDeviceInfoFragment : Fragment() {

    private val fragmentViewModel by viewModel<MyViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.first_device_info_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString("ARG_DEVICE_NAME")?.let { name ->
            fragmentViewModel.setDeviceName(name)
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
                                    if (sensor.id == "1") {
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
        fragmentViewModel.isCommandSentLiveData.observe(this, Observer { visibleLightingProgressBar() })
        lightingOnImageBottom.setOnClickListener {
            fragmentViewModel.sendCommandToDevice("Relay1Off")
        }
        lightingOffImageBottom.setOnClickListener {
            fragmentViewModel.sendCommandToDevice("Relay1On")
        }

        graphHumidityImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    GraphSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE_SENSOR_GRAPH, "ESP8266-13") }
            )
        }
        listHumidityImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    ListSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE_SENSOR_LIST, "ESP8266-13") }
            )
        }
        graphTempOutdoorImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    GraphSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE_SENSOR_GRAPH, "ESP8266-10") }
            )
        }
        listTempOutdoorImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    ListSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE_SENSOR_LIST, "ESP8266-10") }
            )
        }
        graphPressureImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    GraphSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE_SENSOR_GRAPH, "ESP8266-12") }
            )
        }
        listPressureImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    ListSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE_SENSOR_LIST, "ESP8266-12") }
            )
        }
        graphTemperatureIndoorImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    GraphSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE_SENSOR_GRAPH, "ESP8266-11") }
            )
        }
        listTemperatureIndoorImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(
                Intent(
                    context,
                    ListSensorInfoActivity::class.java
                )
                    .apply { putExtra(EXTRA_MESSAGE_SENSOR_LIST, "ESP8266-11") }
            )
        }
    }

    private fun visibleLightingProgressBar() {

        val noConnect: String = getString(R.string.no_connect_value)

        progressBar.visibility = View.VISIBLE
        lightingOnImageBottom.visibility = View.INVISIBLE
        lightingOffImageBottom.visibility = View.INVISIBLE

        temperatureOutdoorTextView.text = noConnect
        temperatureIndoorTextView.text = noConnect
        pressureTextView.text = noConnect
        humidityTextView.text = noConnect
    }

    companion object {
        private const val ARG_DEVICE_NAME = "ARG_DEVICE_NAME"
        fun newInstance(deviceName: String): FirstDeviceInfoFragment {
            return FirstDeviceInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DEVICE_NAME, deviceName)
                }
            }
        }
    }
}