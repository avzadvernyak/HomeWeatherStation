package m.kampukter.homeweatherstation.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.first_device_info_fragment.*
import kotlinx.android.synthetic.main.first_device_info_fragment.is_switch_of_bulb_off
import kotlinx.android.synthetic.main.first_device_info_fragment.is_switch_of_bulb_on
import m.kampukter.homeweatherstation.Constants.EXTRA_MESSAGE
import m.kampukter.homeweatherstation.Constants.FIRST_LOCAL_URL
import m.kampukter.homeweatherstation.MyViewModel
import m.kampukter.homeweatherstation.R
import m.kampukter.homeweatherstation.data.Sensor
import m.kampukter.homeweatherstation.data.dto.DeviceInteractionApi
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.URL

class FirstDeviceInfoFragment : Fragment() {
    private lateinit var siteURL: URL
    private val sharedViewModel by sharedViewModel<MyViewModel>()
    private val fragmentViewModel by viewModel<MyViewModel>()

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
            //Log.d("blablabla", "Set in Res $siteURL")
            sharedViewModel.urlSet(siteURL)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        siteURL = URL(FIRST_LOCAL_URL)
        sharedViewModel.firstURL.observe(this, Observer {_siteURL ->
            siteURL = _siteURL
            fragmentViewModel.urlSet(_siteURL)
            is_switch_of_bulb_off.setOnClickListener {
                is_switch_of_bulb_off.hide()
                fragmentViewModel.commandSend(_siteURL, "Relay1On")
            }
            is_switch_of_bulb_on.setOnClickListener {
                is_switch_of_bulb_on.hide()
                fragmentViewModel.commandSend(_siteURL, "Relay1Off")
            }
        })

        temperatureOutdoorTextView.text = getString(R.string.no_connect_value)
        temperatureIndoorTextView.text = getString(R.string.no_connect_value)
        pressureTextView.text = getString(R.string.no_connect_value)
        humidityTextView.text = getString(R.string.no_connect_value)

        fragmentViewModel.connectStatusWS.observe(this, Observer {
            is_switch_of_bulb_on.hide()
            is_switch_of_bulb_off.hide()
        })

        fragmentViewModel.messageWS.observe(this, androidx.lifecycle.Observer {
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