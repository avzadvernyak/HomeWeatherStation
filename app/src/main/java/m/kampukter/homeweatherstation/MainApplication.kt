package m.kampukter.homeweatherstation

import android.app.Application
import m.kampukter.homeweatherstation.repository.InfoSensorRepository
import m.kampukter.homeweatherstation.repository.WebSocketRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MainApplication: Application() {
    private val module = module {
        single { WebSocketRepository() }
        single { InfoSensorRepository() }
        viewModel { MyViewModel( get(), get() )}
    }
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            modules(module)
        }
    }
}