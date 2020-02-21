package m.kampukter.homeweatherstation.data

import androidx.room.TypeConverter
import java.net.URL

class Converters {
    @TypeConverter
    fun fromURL(value: URL?): String? = value?.toString()
    @TypeConverter
    fun toURL(value: String?): URL?  = value?.let { URL(it) }
}