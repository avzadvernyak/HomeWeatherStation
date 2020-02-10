package m.kampukter.homeweatherstation.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import m.kampukter.homeweatherstation.Constants.EXTRA_MESSAGE

class ListSensorInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sensorID = intent.getStringExtra(EXTRA_MESSAGE)
        val fragment = ListSensorInfoFragment.create(sensorID)
        if (savedInstanceState == null) supportFragmentManager.beginTransaction().add(
            android.R.id.content,
            fragment
        ).commit()
    }

}