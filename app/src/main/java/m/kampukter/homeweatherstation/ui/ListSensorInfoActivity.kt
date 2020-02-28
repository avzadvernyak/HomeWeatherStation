package m.kampukter.homeweatherstation.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

const val EXTRA_MESSAGE_SENSOR_LIST = "EXTRA_MESSAGE_SENSOR_LIST"

class ListSensorInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sensorID = intent.getStringExtra(EXTRA_MESSAGE_SENSOR_LIST)
        val fragment = ListSensorInfoFragment.create(sensorID)
        if (savedInstanceState == null) supportFragmentManager.beginTransaction().add(
            android.R.id.content,
            fragment
        ).commit()
    }

}