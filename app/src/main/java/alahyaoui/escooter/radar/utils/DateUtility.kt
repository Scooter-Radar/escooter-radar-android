package alahyaoui.escooter.radar.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtility {
    fun convertLongToTime(time: Long): String {
        val date = Date(time * 1000)
        val format = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
        return format.format(date)
    }
}
