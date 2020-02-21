package m.kampukter.homeweatherstation.ui

import android.text.format.DateFormat
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.info_sensor_item.view.*
import m.kampukter.homeweatherstation.data.SensorValue

class ListSensorInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(result: SensorValue) {
        with(itemView) {
            dateTimeTextView.text = DateFormat.format("dd/MM/yyyy HH:mm", result.date * 1000L)
            //dateTimeTextView.text = result.date.toString()
            valueTextView.text = result.value.toString()
        }
    }
}
