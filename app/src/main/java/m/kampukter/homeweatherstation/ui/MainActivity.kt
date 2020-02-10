package m.kampukter.homeweatherstation.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.main_activity.*
import m.kampukter.homeweatherstation.MyViewModel
import m.kampukter.homeweatherstation.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.URL

class MainActivity: AppCompatActivity() {

    private val viewModel by viewModel<MyViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Smart House"
            //setDisplayHomeAsUpEnabled(true)
            //setDisplayShowHomeEnabled(true)
        }

        viewPager.adapter = DeviceWSPagerAdapter(supportFragmentManager)
    }
    override fun onResume() {
        super.onResume()
        viewModel.connectWS(URL(getString(R.string.firstServerUrl)))
        viewModel.connectWS(URL(getString(R.string.secondServerUrl)))
    }

    override fun onPause() {
        super.onPause()
        //Log.d("blablabla", "TestActivity->onPause")
        viewModel.disconnectWS(URL(getString(R.string.firstServerUrl)))
        viewModel.disconnectWS(URL(getString(R.string.secondServerUrl)))
    }
}