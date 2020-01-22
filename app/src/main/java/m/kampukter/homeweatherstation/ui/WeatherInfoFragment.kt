package m.kampukter.homeweatherstation.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.weather_main_fragment.*
import m.kampukter.homeweatherstation.MyViewModel
import m.kampukter.homeweatherstation.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class WeatherInfoFragment : Fragment() {

    private val viewModel by viewModel<MyViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onStop() {
        super.onStop()

        viewModel.stopWS()
    }

    override fun onStart() {
        super.onStart()
        viewModel.setCurrentSite(null)
        progressBar.visibility = View.VISIBLE

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.weather_main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = getString(R.string.app_name)
        }

        is_switch_of_bulb_on.hide()
        is_switch_of_bulb_off.hide()

        viewModel.currentSiteInfo.observe(this, Observer {
            //currentSiteInfo = it
            viewModel.startWS(it.id)
            toolbar.subtitle = it?.url
            Snackbar.make(
                weather_info,
                "Handshake proceeding on \n${it?.url}",
                Snackbar.LENGTH_LONG
            ).show()
        })
        viewModel.isStatusWS.observe(this, Observer { isStatus ->
            if (isStatus) {
                Snackbar.make(
                    weather_info,
                    "Handshake with WS successfull",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                progressBar.visibility = View.GONE
                Snackbar.make(
                    weather_info,
                    "Handshake with WS unsuccessfull",
                    Snackbar.LENGTH_LONG
                ).show()
                is_switch_of_bulb_on.hide()
                is_switch_of_bulb_off.hide()
                temperatureOutdoorTextView.text = "\u221E"
                pressureTextView.text = "\u221E"
                humidityTextView.text = "\u221E"
                temperatureIndoorTextView.text = "\u221E"
            }
        })

        viewModel.sensorInfo.observe(this, Observer {
            progressBar.visibility = View.GONE
            temperatureOutdoorTextView.text = if (it.sensor1 != -270.0F)
                getString(
                    R.string.temperatureOutdoorSensorValue,
                    it.sensor1.toString()
                ) else "\u221E"
            pressureTextView.text = getString(R.string.pressureSensorValue, it.sensor2.toString())
            humidityTextView.text = if (it.sensor4 != -270.0F) getString(
                R.string.humiditySensorValue,
                it.sensor4.toString()
            ) else "\u221E"
            temperatureIndoorTextView.text = if (it.sensor3 != -270.0F) getString(
                R.string.temperatureIndoorSensorValue,
                it.sensor3.toString()
            ) else "\u221E"
            //Log.d("blablabla", "relay1- ${it.relay1}")
            if (it.relay1) {
                is_switch_of_bulb_off.hide()
                is_switch_of_bulb_on.show()
            } else {
                is_switch_of_bulb_off.show()
                is_switch_of_bulb_on.hide()
            }

        })
        graphHumidityImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(Intent(
                context,
                GraphSensorInfoActivity::class.java
            )
                .apply { putExtra(EXTRA_MESSAGE, "Humidity") }
            )
        }
        listHumidityImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(Intent(
                context,
                ListSensorInfoActivity::class.java
            )
                .apply { putExtra(EXTRA_MESSAGE, "Humidity") }
            )
        }
        graphTempOutdoorImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(Intent(
                context,
                GraphSensorInfoActivity::class.java
            )
                .apply { putExtra(EXTRA_MESSAGE, "TempOutdoor") }
            )
        }
        listTempOutdoorImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(Intent(
                context,
                ListSensorInfoActivity::class.java
            )
                .apply { putExtra(EXTRA_MESSAGE, "TempOutdoor") }
            )
        }
        graphPressureImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(Intent(
                context,
                GraphSensorInfoActivity::class.java
            )
                .apply { putExtra(EXTRA_MESSAGE, "Pressure") }
            )
        }
        listPressureImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(Intent(
                context,
                ListSensorInfoActivity::class.java
            )
                .apply { putExtra(EXTRA_MESSAGE, "Pressure") }
            )
        }
        graphTemperatureIndoorImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(Intent(
                context,
                GraphSensorInfoActivity::class.java
            )
                .apply { putExtra(EXTRA_MESSAGE, "TempIndoor") }
            )
        }
        listTemperatureIndoorImageButton.setOnClickListener {
            (context as AppCompatActivity).startActivity(Intent(
                context,
                ListSensorInfoActivity::class.java
            )
                .apply { putExtra(EXTRA_MESSAGE, "TempIndoor") }
            )
        }
        is_switch_of_bulb_off.setOnClickListener {
            Log.d("blablabla", "is_switch_of_bulb_off")
            is_switch_of_bulb_off.hide()
            viewModel.sendCommandToWs("Relay1On")
            Snackbar.make(
                weather_info,
                "Switching on command sent to WS",
                Snackbar.LENGTH_LONG
            ).show()
        }
        is_switch_of_bulb_on.setOnClickListener {
            Log.d("blablabla", "is_switch_of_bulb_on")
            is_switch_of_bulb_on.hide()
            viewModel.sendCommandToWs("Relay1Off")
            Snackbar.make(
                weather_info,
                "Switching off command sent to WS",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_wetherinfo_view_settings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_wan_server -> {
                viewModel.stopWS()
                viewModel.setCurrentSite(1)
                progressBar.visibility = View.VISIBLE
                true
            }
            R.id.action_lan_server -> {
                viewModel.stopWS()
                viewModel.setCurrentSite(2)
                progressBar.visibility = View.VISIBLE
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    companion object {
        const val EXTRA_MESSAGE = "EXTRA_MESSAGE"
    }
}