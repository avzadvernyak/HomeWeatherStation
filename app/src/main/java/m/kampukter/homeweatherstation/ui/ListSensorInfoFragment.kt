package m.kampukter.homeweatherstation.ui

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.sensor_list_fragment.*
import m.kampukter.homeweatherstation.MyViewModel
import m.kampukter.homeweatherstation.R
import m.kampukter.homeweatherstation.data.SensorRequest
import m.kampukter.homeweatherstation.data.ResultSensorValue
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

private const val KEY_SELECTED_PERIOD = "KEY_SELECTED_PERIOD"

class ListSensorInfoFragment : Fragment() {

    private val viewModel by viewModel<MyViewModel>()

    private var listSensorInfoAdapter: ListSensorInfoAdapter? = null

    private var strDateBegin = DateFormat.format("yyyy-MM-dd", Date()).toString()
    private var strDateEnd = strDateBegin

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.sensor_list_fragment, container, false)
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

        listSensorInfoAdapter = ListSensorInfoAdapter()
        with(recyclerView) {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = listSensorInfoAdapter
        }
        progressBar.visibility = View.VISIBLE

        arguments?.getString("ARG_MESSAGE")?.let {
            viewModel.setQuestionSensorValue(SensorRequest(it, strDateBegin, strDateEnd))
        }

        viewModel.sensorLiveData.observe(this, Observer { (sensorInformation, resultSensorValue) ->

            pickerRange.addOnPositiveButtonClickListener { dateSelected ->
                dateSelected.first?.let {
                    strDateBegin = DateFormat.format("yyyy-MM-dd", it).toString()
                }
                dateSelected.second?.let {
                    strDateEnd = DateFormat.format("yyyy-MM-dd", it).toString()
                }
                viewModel.setQuestionSensorValue(
                    SensorRequest(sensorInformation.sensorName, strDateBegin, strDateEnd)
                )

                progressBar.visibility = View.VISIBLE
            }

            (activity as AppCompatActivity).supportActionBar?.apply {
                title = sensorInformation.sensorName
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
            }

            when (resultSensorValue) {
                is ResultSensorValue.Success -> {
                    progressBar.visibility = View.GONE
                    val begTime =
                        DateFormat.format(
                            getString(R.string.formatDT),
                            resultSensorValue.sensorValue.first().date * 1000L
                        )
                    val endTime =
                        DateFormat.format(
                            getString(R.string.formatDT),
                            resultSensorValue.sensorValue.last().date * 1000L
                        )
                    dateTextView.text =
                        getString(
                            R.string.dateInfoView,
                            begTime.toString(),
                            endTime.toString(),
                            sensorInformation.title
                        )
                    val dateMax =
                        resultSensorValue.sensorValue.maxBy { it.value }?.date?.let { time ->
                            DateFormat.format("dd/MM/yy HH:mm", time * 1000L)
                        }
                    maxTextView.text = getString(
                        R.string.maxValuePeriod,
                        resultSensorValue.sensorValue.maxBy { it.value }?.value.toString(),
                        sensorInformation.measure,
                        dateMax.toString()
                    )

                    val dateMin =
                        resultSensorValue.sensorValue.minBy { it.value }?.date?.let { time ->
                            DateFormat.format("dd/MM/yy HH:mm", time * 1000L)
                        }
                    minTextView.text = getString(
                        R.string.minValuePeriod,
                        resultSensorValue.sensorValue.minBy { it.value }?.value.toString(),
                        sensorInformation.measure,
                        dateMin.toString()
                    )
                    val sumValue = resultSensorValue.sensorValue.sumBy { it.value.toInt() }
                    averageTextView.text = getString(
                        R.string.averageValuePeriod,
                        (sumValue / resultSensorValue.sensorValue.size).toString(),
                        sensorInformation.measure
                    )
                    listSensorInfoAdapter?.setList(resultSensorValue.sensorValue)
                }
                is ResultSensorValue.OtherError -> {
                    progressBar.visibility = View.GONE
                    Log.d("blablabla", "Other Error" + resultSensorValue.tError)
                }
                is ResultSensorValue.EmptyResponse -> {
                    progressBar.visibility = View.GONE
                    maxTextView.text = getString(R.string.noDataMessage, strDateBegin, strDateEnd)
                    minTextView.text = ""
                    averageTextView.text = ""
                    dateTextView.text =
                        getString(
                            R.string.dateInfoView,
                            strDateBegin,
                            strDateEnd,
                            sensorInformation.sensorName
                        )
                    listSensorInfoAdapter?.setList(emptyList())
                    Snackbar.make(
                        sensor1_fragment,
                        getString(R.string.noDataMessage, strDateBegin, strDateEnd),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        })

        calendarFAB.setOnClickListener {
            fragmentManager?.let { pickerRange.show(it, "Picker") }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArray(KEY_SELECTED_PERIOD, arrayOf(strDateBegin, strDateEnd))
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val ARG_MESSAGE = "ARG_MESSAGE"
        fun create(message: String) = ListSensorInfoFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_MESSAGE, message)
            }
        }
    }
}


