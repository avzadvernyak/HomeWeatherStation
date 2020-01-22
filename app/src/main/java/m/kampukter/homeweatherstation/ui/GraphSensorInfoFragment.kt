package m.kampukter.homeweatherstation.ui

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import m.kampukter.homeweatherstation.MyViewModel
import m.kampukter.homeweatherstation.R
import m.kampukter.homeweatherstation.data.RequestPeriod
import m.kampukter.homeweatherstation.data.Sensor
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import androidx.lifecycle.Observer
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.sensor_graph_fragment.*
import kotlinx.android.synthetic.main.sensor_list_fragment.progressBar
import m.kampukter.homeweatherstation.data.ResultInfoSensor
import java.util.*

private const val KEY_SELECTED_PERIOD = "KEY_SELECTED_PERIOD"

class GraphSensorInfoFragment : Fragment() {

    private val viewModel by viewModel<MyViewModel>()
    private var searchSensor: String? = null
    private var sensorInformation: Sensor? = null

    private val format = SimpleDateFormat("yyyy-MM-dd")
    private var currentDay = Date()
    private var strDateBegin: String = format.format(Date(currentDay.time - (1000 * 60 * 60 * 24)))
    private var strDateEnd: String = format.format(currentDay)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString("ARG_MESSAGE")?.let {
            searchSensor = it
            sensorInformation = viewModel.getInfoBySensor(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.sensor_graph_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = sensorInformation?.id
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        savedInstanceState?.let { bundle ->
            bundle.getStringArray(KEY_SELECTED_PERIOD)?.let { saveDate ->
                strDateBegin = saveDate[0]
                strDateEnd = saveDate[1]
            }
        }

        val series: LineGraphSeries<DataPoint> = LineGraphSeries()

        sensorInformation?.let {
            viewModel.getQuestionInfoSensor(
                RequestPeriod(it.sensorName, strDateBegin, strDateEnd)
            )
        }
        progressBar.visibility = View.VISIBLE
        viewModel.infoSensor.observe(this, Observer { infoSensorList ->
            when (infoSensorList) {
                is ResultInfoSensor.Success -> {
                    progressBar.visibility = View.GONE
                    val value = infoSensorList.infoSensor
                    val graphValue = Array(value.size) { i ->
                        DataPoint(Date(value[i].date * 1000L), value[i].value.toDouble())
                    }
                    series.resetData(graphValue)
                    val begTime =
                        DateFormat.format(getString(R.string.formatDT), value.first().date * 1000L)
                    val endTime =
                        DateFormat.format(getString(R.string.formatDT), value.last().date * 1000L)
                    titleTextView.text =
                        getString(R.string.periodView, begTime.toString(), endTime.toString())

                    graphSensorFS.viewport.setMinX(value.first().date * 1000L.toDouble())
                    graphSensorFS.viewport.setMaxX(value.last().date * 1000L.toDouble())
                }
                is ResultInfoSensor.OtherError -> {
                    progressBar.visibility = View.GONE
                    Log.d("blablabla", "Other Error:" + infoSensorList.tError)
                }
                is ResultInfoSensor.EmptyResponse -> {
                    progressBar.visibility = View.GONE
                    Log.d("blablabla", "EmptyResponse")
                }
            }
        })
        with(graphSensorFS) {
            addSeries(series)
            gridLabelRenderer.isHorizontalLabelsVisible = false
            viewport.isXAxisBoundsManual = true
            viewport.isYAxisBoundsManual = true
            sensorInformation?.let {
                viewport.setMinY(it.minValue)
                viewport.setMaxY(it.maxValue)
                title = it.measure
            }
        }
        dateRangeFAB.setOnClickListener {
            val pickerRange = MaterialDatePicker.Builder.dateRangePicker()
                .build()
            pickerRange.addOnPositiveButtonClickListener { pair ->
                strDateBegin = format.format(pair.first)
                strDateEnd = format.format(pair.second)
                sensorInformation?.let {
                    viewModel.getQuestionInfoSensor(
                        RequestPeriod(
                            it.sensorName,
                            strDateBegin,
                            strDateEnd
                        )
                    )
                }
                progressBar.visibility = View.VISIBLE
            }
            fragmentManager?.let { pickerRange.show(it, pickerRange.toString()) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArray(KEY_SELECTED_PERIOD, arrayOf(strDateBegin, strDateEnd))
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_graph_view_settings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_last_day -> {
                strDateBegin = format.format(Date(currentDay.time - (1000 * 60 * 60 * 24)))
                strDateEnd = format.format(currentDay)
                sensorInformation?.let {
                    viewModel.getQuestionInfoSensor(
                        RequestPeriod(
                            it.sensorName,
                            strDateBegin,
                            strDateEnd
                        )
                    )
                }
                progressBar.visibility = View.VISIBLE
                return true
            }
            R.id.action_last_week -> {
                strDateBegin = format.format(Date(currentDay.time - (1000 * 60 * 60 * 24 * 7)))
                strDateEnd = format.format(currentDay)
                sensorInformation?.let {
                    viewModel.getQuestionInfoSensor(
                        RequestPeriod(
                            it.sensorName,
                            strDateBegin,
                            strDateEnd
                        )
                    )
                }
                progressBar.visibility = View.VISIBLE
                return true
            }
            R.id.action_month -> {
                val c = Calendar.getInstance()
                val dateSetListener = DatePickerDialog.OnDateSetListener { _,
                                                                           year, monthOfYear, _ ->
                    c.set(Calendar.YEAR, year)
                    c.set(Calendar.MONTH, monthOfYear)
                    c.set(Calendar.DAY_OF_MONTH, 1)
                    strDateBegin = format.format(c.time)
                    c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
                    strDateEnd = format.format(c.time)
                    sensorInformation?.let {
                        viewModel.getQuestionInfoSensor(
                            RequestPeriod(
                                it.sensorName,
                                strDateBegin,
                                strDateEnd
                            )
                        )
                    }
                    progressBar.visibility = View.VISIBLE
                }

                fragmentManager?.let {
                    MonthDatePickerFragment.create(dateSetListener)
                        .show(it, "datePicker")
                }

                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }


    companion object {
        private const val ARG_MESSAGE = "ARG_MESSAGE"
        fun create(message: String) = GraphSensorInfoFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_MESSAGE, message)
            }
        }
    }
}