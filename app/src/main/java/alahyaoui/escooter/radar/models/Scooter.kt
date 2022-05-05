package alahyaoui.escooter.radar.models

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.squareup.moshi.Json

data class Scooter(

    @Json(name = "bike_id")
    var bikeId: String,

    @Json(name = "company")
    var company: String,

    @Json(name = "city")
    var city: String,

    @Json(name = "location")
    var location: Location,

    @Json(name = "is_disabled")
    var isDisabled: Boolean,

    @Json(name = "is_reserved")
    var isReserved: Boolean,

    @Json(name = "last_reported")
    var lastReported: Long,

    @Json(name = "current_range_meters")
    var currentRangeMeters: Double,
) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(location.coordinates[1], location.coordinates[0])

    override fun getTitle(): String = company

    override fun getSnippet(): String = city
}