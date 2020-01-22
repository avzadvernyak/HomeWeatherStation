package m.kampukter.homeweatherstation.ui

import android.content.Context
import android.os.Bundle
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
import androidx.lifecycle.Observer
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.sensor_graph_fragment.*
import kotlinx.android.synthetic.main.sensor_graph_fragment.toolbar
import kotlinx.android.synthetic.main.sensor_list_fragment.progressBar
import m.kampukter.homeweatherstation.data.ResultInfoSensor
import java.util.*

private const val KEY_SELECTED_PERIOD = "KEY_SELECTED_PERIOD"

class GraphSensorInfoFragment : Fragment() {

    private val viewModel by viewModel<MyViewModel>()
    private var searchSensor: String? = null
    private var sensorInformation: Sensor? = null

    private var strDateBegin: String =
        DateFormat.format("yyyy-MM-dd", Date(Date().time - (1000 * 60 * 60 * 24))).toString()
    private var strDateEnd: String = DateFormat.format("yyyy-MM-dd", Date()).toString()

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
                    Snackbar.make(
                        sensor_graph_fragment,
                        getString(R.string.noDataMessage, strDateBegin, strDateEnd),
                        Snackbar.LENGTH_LONG
                    ).show()
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
                pair.first?.let { strDateBegin = DateFormat.format("yyyy-MM-dd", it).toString() }
                pair.second?.let { strDateEnd = DateFormat.format("yyyy-MM-dd", it).toString() }
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


    companion object {
        private const val ARG_MESSAGE = "ARG_MESSAGE"
        fun create(message: String) = GraphSensorInfoFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_MESSAGE, message)
            }
        }
    }
}