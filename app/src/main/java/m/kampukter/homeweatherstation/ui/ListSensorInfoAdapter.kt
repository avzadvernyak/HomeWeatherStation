package m.kampukter.homeweatherstation.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.homeweatherstation.R
import m.kampukter.homeweatherstation.data.InfoSensor


class ListSensorInfoAdapter: RecyclerView.Adapter<ListSensorInfoViewHolder>() {

        private var infoSensor: List<InfoSensor>? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListSensorInfoViewHolder {
            return ListSensorInfoViewHolder(
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.info_sensor_item, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return infoSensor?.size ?: 0
        }

        override fun onBindViewHolder(holder: ListSensorInfoViewHolder, position: Int) {
            infoSensor?.get(position)?.let { item ->
                holder.bind(item)
            }
        }

        fun setList(list: List<InfoSensor>) {
            this.infoSensor = list
            notifyDataSetChanged()
        }
}