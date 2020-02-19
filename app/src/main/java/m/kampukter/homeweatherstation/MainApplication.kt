package m.kampukter.homeweatherstation

import android.app.Application
import m.kampukter.homeweatherstation.data.dto.DeviceInteractionApi
import m.kampukter.homeweatherstation.data.dto.WebSocketDeviceInteractionApi
import m.kampukter.homeweatherstation.data.repository.InfoSensorRepository
import m.kampukter.homeweatherstation.data.repository.WebSocketRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

@Suppress("unused")
class MainApplication: Application() {
    private val module = module {

        single<DeviceInteractionApi> { WebSocketDeviceInteractionApi() }

        single { WebSocketRepository(get()) }
        single { InfoSensorRepository() }
        viewModel { MyViewModel(get(), get()) }

    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            modules(module)
        }
    }
}