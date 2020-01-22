package m.kampukter.homeweatherstation.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class MonthDatePickerFragment: DialogFragment(), DatePickerDialog.OnDateSetListener {

    private var onDateSetListener: DatePickerDialog.OnDateSetListener? = null
    private lateinit var datePickerDialog: DatePickerDialog

    override fun onStart() {
        super.onStart()
        val fieldDay =
            datePickerDialog.findViewById<View>(
                Resources.getSystem().getIdentifier("android:id/day", null, null)
            )
        if (fieldDay != null) fieldDay.visibility = View.GONE
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        datePickerDialog = DatePickerDialog(requireContext(),
            AlertDialog.THEME_HOLO_LIGHT, onDateSetListener, year, month, day)
        return datePickerDialog
    }

    private fun setOnDateSetListener(listener: DatePickerDialog.OnDateSetListener?) {
        this.onDateSetListener = listener

    }

    companion object {
        fun create(onDateSetListener: DatePickerDialog.OnDateSetListener): MonthDatePickerFragment =
            MonthDatePickerFragment().apply {
                setOnDateSetListener(onDateSetListener)
            }
    }
}