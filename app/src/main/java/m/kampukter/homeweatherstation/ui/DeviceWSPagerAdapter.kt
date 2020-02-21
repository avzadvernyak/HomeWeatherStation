package m.kampukter.homeweatherstation.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class DeviceWSPagerAdapter(supportFragmentManager: FragmentManager) :
    FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragments = mutableMapOf<Int, Fragment>()
    private val titles = mutableMapOf<Int, String>()

    override fun getItem(position: Int): Fragment = getFragmentByIndex(position)

    override fun getCount(): Int = 2

    override fun getPageTitle(position: Int): CharSequence? = getTitleByIndex(position)

    private fun getFragmentByIndex(index: Int): Fragment = fragments[index] ?: when (index) {
        0 -> FirstDeviceInfoFragment.newInstance()
        else -> SecondDeviceInfoFragment.newInstance()
    }.also { fragments[index] = it }

    private fun getTitleByIndex(index: Int): String = titles[index] ?: when (index) {
        0 -> "ESP8266-1"
        else -> "ESP8266-1"
    }.also { titles[index] = it }
}
