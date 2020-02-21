package m.kampukter.homeweatherstation.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.homeweatherstation.R
import m.kampukter.homeweatherstation.data.SensorValue


class ListSensorInfoAdapter: RecyclerView.Adapter<ListSensorInfoViewHolder>() {

        private var sensorValue: List<SensorValue>? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListSensorInfoViewHolder {
            return ListSensorInfoViewHolder(
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.info_sensor_item, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return sensorValue?.size ?: 0
        }

        override fun onBindViewHolder(holder: ListSensorInfoViewHolder, position: Int) {
            sensorValue?.get(position)?.let { item ->
                holder.bind(item)
            }
        }

        fun setList(list: List<SensorValue>) {
            this.sensorValue = list
            notifyDataSetChanged()
        }
}