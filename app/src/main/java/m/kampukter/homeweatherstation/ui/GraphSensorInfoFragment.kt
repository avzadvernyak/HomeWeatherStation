package m.kampukter.homeweatherstation.ui

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import m.kampukter.homeweatherstation.MyViewModel
import m.kampukter.homeweatherstation.R
import m.kampukter.homeweatherstation.data.SensorRequest
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.lifecycle.Observer
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.sensor_graph_fragment.*
import kotlinx.android.synthetic.main.sensor_graph_fragment.toolbar
import kotlinx.android.synthetic.main.sensor_list_fragment.progressBar
import m.kampukter.homeweatherstation.data.ResultSensorValue
import java.util.*

private const val KEY_SELECTED_PERIOD = "KEY_SELECTED_PERIOD"

class GraphSensorInfoFragment : Fragment() {

    private val viewModel by viewModel<MyViewModel>()

    private var strDateBegin: String =
        DateFormat.format("yyyy-MM-dd", Date(Date().time - (1000 * 60 * 60 * 24))).toString()
    private var strDateEnd: String = DateFormat.format("yyyy-MM-dd", Date()).toString()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.sensor_graph_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        @Suppress("UNCHECKED_CAST")
        val pickerRange: MaterialDatePicker<androidx.core.util.Pair<Long, Long>> =
            fragmentManager?.findFragmentByTag("Picker") as? MaterialDatePicker<androidx.core.util.Pair<Long, Long>>
                ?: MaterialDatePicker.Builder.dateRangePicker().build()

        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)

        savedInstanceState?.let { bundle ->
            bundle.getStringArray(KEY_SELECTED_PERIOD)?.let { saveDate ->
                strDateBegin = saveDate[0]
                strDateEnd = saveDate[1]
            }
        }
        arguments?.getString("ARG_MESSAGE")?.let {
            viewModel.setQuestionSensorValue(SensorRequest(it, strDateBegin, strDateEnd))
        }
        val series: LineGraphSeries<DataPoint> = LineGraphSeries()
        progressBar.visibility = View.VISIBLE

        viewModel.sensorLiveData.observe(this, Observer { (sensorInformation, infoSensorList) ->
            pickerRange.addOnPositiveButtonClickListener { dateSelected ->
                dateSelected.first?.let {
                    strDateBegin = DateFormat.format("yyyy-MM-dd", it).toString()
                }
                dateSelected.second?.let {
                    strDateEnd = DateFormat.format("yyyy-MM-dd", it).toString()
                }
                viewModel.setQuestionSensorValue(SensorRequest(sensorInformation.sensorName, strDateBegin, strDateEnd))
                progressBar.visibility = View.VISIBLE
            }

            (activity as AppCompatActivity).supportActionBar?.apply {
                title = sensorInformation.sensorName
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
            }

            with(graphSensorFS) {
                addSeries(series)
                gridLabelRenderer.isHorizontalLabelsVisible = false
                viewport.isXAxisBoundsManual = true
                viewport.isYAxisBoundsManual = true
                sensorInformation.let {
                    viewport.setMinY(it.minValue)
                    viewport.setMaxY(it.maxValue)
                    title = it.measure
                }
            }
            when (infoSensorList) {
                is ResultSensorValue.Success -> {
                    progressBar.visibility = View.GONE
                    val value = infoSensorList.sensorValue
                    val graphValue = Array(value.size) { i ->
                        DataPoint(Date(value[i].date * 1000L), value[i].value.toDouble())
                    }
                    series.resetData(graphValue)
                    val begTime =
                        DateFormat.format(
                            getString(R.string.formatDT),
                            value.first().date * 1000L
                        )
                    val endTime =
                        DateFormat.format(
                            getString(R.string.formatDT),
                            value.last().date * 1000L
                        )
                    titleTextView.text =
                        getString(R.string.periodView, begTime.toString(), endTime.toString())

                    graphSensorFS.viewport.setMinX(value.first().date * 1000L.toDouble())
                    graphSensorFS.viewport.setMaxX(value.last().date * 1000L.toDouble())
                }
                is ResultSensorValue.OtherError -> {
                    progressBar.visibility = View.GONE
                    Log.d("blablabla", "Other Error:" + infoSensorList.tError)
                }
                is ResultSensorValue.EmptyResponse -> {
                    progressBar.visibility = View.GONE
                    Snackbar.make(
                        sensor_graph_fragment,
                        getString(R.string.noDataMessage, strDateBegin, strDateEnd),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        })
        dateRangeFAB.setOnClickListener {
            fragmentManager?.let { pickerRange.show(it, "Picker") }
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