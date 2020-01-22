package m.kampukter.homeweatherstation.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ListSensorInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sensorID = intent.getStringExtra(WeatherInfoFragment.EXTRA_MESSAGE)
        val fragment = ListSensorInfoFragment.create(sensorID)
        if (savedInstanceState == null) supportFragmentManager.beginTransaction().add(
            android.R.id.content,
            fragment
        ).commit()
    }

}