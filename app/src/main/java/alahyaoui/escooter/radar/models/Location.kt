package alahyaoui.escooter.radar.models

import com.squareup.moshi.Json

data class Location(
    @Json(name = "type")
    var type: String,

    @Json(name = "coordinates")
    var coordinates: List<Double>,
)