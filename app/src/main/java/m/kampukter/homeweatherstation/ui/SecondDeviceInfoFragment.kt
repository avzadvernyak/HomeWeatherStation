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
import java.net.URL
import m.kampukter.homeweatherstation.Constants.EXTRA_MESSAGE
import m.kampukter.homeweatherstation.Constants.SECOND_LOCAL_URL
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SecondDeviceInfoFragment : Fragment() {

    private lateinit var siteURL: URL

    private val sharedViewModel by sharedViewModel<MyViewModel>()
    private val fragmentViewModel by viewModel<MyViewModel>()

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
        sharedViewModel.urlSet(siteURL)
        //Log.d("blablabla", "Set in Res $siteURL")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        siteURL = URL(SECOND_LOCAL_URL)
        sharedViewModel.secondURL.observe(this, Observer {_siteURL ->
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
//            Log.d("blablabla", "Set in Obs $siteURL")
        })


        temperatureTextView.text = getString(R.string.no_connect_value)
        voltageTextView.text = getString(R.string.no_connect_value)
        amperageTextView.text = getString(R.string.no_connect_value)

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