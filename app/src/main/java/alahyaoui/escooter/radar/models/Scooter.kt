package alahyaoui.escooter.radar.models

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
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
) : Parcelable, ClusterItem {
    override fun getPosition(): LatLng = LatLng(location.coordinates[1], location.coordinates[0])

    override fun getTitle(): String = company

    override fun getSnippet(): String = city
}