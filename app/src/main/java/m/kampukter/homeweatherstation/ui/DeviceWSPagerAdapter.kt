package m.kampukter.homeweatherstation.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class DeviceWSPagerAdapter(supportFragmentManager: FragmentManager) :
    FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var deviceName: List<String> = emptyList()
    private val fragments = mutableMapOf<Int, Fragment>()

    override fun getItem(position: Int): Fragment = getFragmentByIndex(position)

    override fun getCount(): Int = deviceName.size

    override fun getPageTitle(position: Int): CharSequence? = deviceName[position]

    private fun getFragmentByIndex(index: Int): Fragment = fragments[index] ?: when (index) {
        0 -> FirstDeviceInfoFragment.newInstance(deviceName[0])
        else -> SecondDeviceInfoFragment.newInstance(deviceName[1])
    }.also { fragments[index] = it }

    fun setListDevice(listDevice: List<String>) {
        deviceName = listDevice
        notifyDataSetChanged()
    }
    fun getListDevice(currentPosition: Int): String = deviceName[currentPosition]


}
//