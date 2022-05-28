package alahyaoui.escooter.radar.models

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class Location(
    @Json(name = "type")
    var type: String,

    @Json(name = "coordinates")
    var coordinates: List<Double>,
) : Parcelable