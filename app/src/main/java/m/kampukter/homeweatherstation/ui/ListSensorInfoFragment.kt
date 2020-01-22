package m.kampukter.homeweatherstation.ui

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.sensor_list_fragment.*
import m.kampukter.homeweatherstation.MyViewModel
import m.kampukter.homeweatherstation.R
import m.kampukter.homeweatherstation.data.InfoSensor
import m.kampukter.homeweatherstation.data.RequestPeriod
import m.kampukter.homeweatherstation.data.ResultInfoSensor
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class ListSensorInfoFragment : Fragment() {

    private val viewModel by viewModel<MyViewModel>()
    private var listSensorInfoAdapter: ListSensorInfoAdapter? = null

    private var searchSensor: String = ""


    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString("ARG_MESSAGE")?.let {
            searchSensor = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.sensor_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val format = SimpleDateFormat("yyyy-MM-dd")
        var currentDay = Date()
        val strDateEnd = format.format(currentDay)
        val sensorInformation = viewModel.getInfoBySensor(searchSensor)

        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = sensorInformation?.id
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        listSensorInfoAdapter = ListSensorInfoAdapter()

        with(recyclerView) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                context,
                androidx.recyclerview.widget.RecyclerView.VERTICAL,
                false
            )
            adapter = listSensorInfoAdapter
        }
        if (sensorInformation != null)
            viewModel.getQuestionInfoSensor(RequestPeriod(sensorInformation.sensorName, strDateEnd, strDateEnd))
        progressBar.visibility = View.VISIBLE
        viewModel.infoSensor.observe(this, Observer { infoSensorList ->
            when (infoSensorList) {
                is ResultInfoSensor.Success -> {
                    progressBar.visibility = View.GONE
                    dateTextView.text =
                        getString(
                            R.string.dateInfoView,
                            DateFormat.format("dd/MM/yyyy", currentDay),
                            sensorInformation?.id
                        )
                    val dateMax =
                        infoSensorList.infoSensor.maxBy { it.value }?.date?.let { time ->
                            DateFormat.format("HH:mm", time * 1000L)
                        }
                    maxTextView.text = getString(
                        R.string.maxValuePeriod,
                        infoSensorList.infoSensor.maxBy { it.value }?.value.toString(),
                        sensorInformation?.measure,
                        dateMax.toString()
                    )

                    val dateMin =
                        infoSensorList.infoSensor.minBy { it.value }?.date?.let { time ->
                            DateFormat.format("HH:mm", time * 1000L)
                        }
                    minTextView.text = getString(
                        R.string.minValuePeriod,
                        infoSensorList.infoSensor.minBy { it.value }?.value.toString(),
                        sensorInformation?.measure,
                        dateMin.toString()
                    )
                    val sumValue = infoSensorList.infoSensor.sumBy { it.value.toInt() }
                    averageTextView.text = getString(
                        R.string.averageValuePeriod,
                        (sumValue / infoSensorList.infoSensor.size).toString(),
                        sensorInformation?.measure
                    )
                    listSensorInfoAdapter?.setList(infoSensorList.infoSensor)
                }
                is ResultInfoSensor.OtherError -> {
                    progressBar.visibility = View.GONE
                    Log.d("blablabla", "Other Error" + infoSensorList.tError)
                }
                is ResultInfoSensor.EmptyResponse -> {
                    progressBar.visibility = View.GONE
                    maxTextView.text = ""
                    minTextView.text = ""
                    averageTextView.text = ""
                    dateTextView.text =
                        getString(
                            R.string.dateInfoView,
                            DateFormat.format("dd/MM/yyyy", currentDay),
                            sensorInformation?.id
                        )
                    listSensorInfoAdapter?.setList(emptyList<InfoSensor>())
                    Snackbar.make(
                        sensor1_fragment,
                        getString(R.string.noDataMessage, DateFormat.format("dd/MM/yyyy", currentDay)),
                        Snackbar.LENGTH_LONG
                    ).show()

                    Log.d("blablabla", "infoSensorList is Empty")
                }
            }
        })
        calendarFAB.setOnClickListener {


            val c = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener { _,
                                                                       year, monthOfYear, dayOfMonth ->
                c.set(Calendar.YEAR, year)
                c.set(Calendar.MONTH, monthOfYear)
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                currentDay = c.time
                val searchDate = format.format(c.time)
                if (sensorInformation != null)
                    viewModel.getQuestionInfoSensor(RequestPeriod(sensorInformation.sensorName, searchDate, searchDate))
                progressBar.visibility = View.VISIBLE
            }

            fragmentManager?.let {
                DatePickerFragment.create(dateSetListener)
                    .show(it, "datePicker")
            }
        }
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


