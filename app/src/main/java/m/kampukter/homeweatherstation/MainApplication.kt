package m.kampukter.homeweatherstation

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import m.kampukter.homeweatherstation.data.SensorInformation
import m.kampukter.homeweatherstation.data.dao.MyDatabase
import m.kampukter.homeweatherstation.data.dto.DeviceInteractionApi
import m.kampukter.homeweatherstation.data.dto.WebSocketDeviceInteractionApi
import m.kampukter.homeweatherstation.data.repository.SensorRepository
import m.kampukter.homeweatherstation.data.repository.WebSocketRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.net.URL

@Suppress("unused")
class MainApplication : Application() {
    private val module = module {

        single<DeviceInteractionApi> { WebSocketDeviceInteractionApi() }

        single { WebSocketRepository(get(),get<MyDatabase>().sensorInformationDao()) }
        single { SensorRepository(get<MyDatabase>().sensorInformationDao()) }
        viewModel { MyViewModel(get(), get()) }
        single {
            Room.databaseBuilder(androidContext(), MyDatabase::class.java, "myDatabase.db")
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(supportDb: SupportSQLiteDatabase) {
                        GlobalScope.launch(context = Dispatchers.IO) {
                            get<MyDatabase>().sensorInformationDao().insertAll(
                                initSensorInformation()
                            )
                        }
                    }
                }).build()
        }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            modules(module)
        }
    }

    private fun initSensorInformation() = listOf(
        SensorInformation(
            "ESP8266-10",
            URL("http://192.168.0.82:81/"),
            "TempOutdoor",
            "°C",
            50.0,
            -35.0
        ),
        SensorInformation(
            "ESP8266-11",
            URL("http://192.168.0.82:81/"),
            "TempIndoor",
            "°C",
            50.0,
            -5.0
        ),
        SensorInformation(
            "ESP8266-21",
            URL("http://192.168.0.83:81/"),
            "TempGuestRoom",
            "°C",
            50.0,
            5.0
        ),
        SensorInformation(
            "ESP8266-22",
            URL("http://192.168.0.83:81/"),
            "Voltage",
            "°mV",
            30000.0,
            0.0
        ),
        SensorInformation(
            "ESP8266-23",
            URL("http://192.168.0.83:81/"),
            "Amperage",
            "°mA",
            4000.0,
            0.0
        ),
        SensorInformation(
            "ESP8266-12",
            URL("http://192.168.0.82:81/"),
            "Pressure",
            "mm Hg",
            770.0,
            720.0
        ),
        SensorInformation(
            "ESP8266-13",
            URL("http://192.168.0.82:81/"),
            "Humidity",
            "%",
            100.0,
            0.0
        )
    )
}