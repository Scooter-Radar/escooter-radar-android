package alahyaoui.escooter.radar.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.squareup.moshi.Json

@Entity(tableName = "Scooter", primaryKeys = ["bike_id", "company"])
data class Scooter (

    @ColumnInfo(name = "bike_id")
    @Json(name = "bike_id")
    var bikeId: String,

    @ColumnInfo(name = "company")
    @Json(name = "company")
    var company: String,

    @ColumnInfo(name = "zone")
    @Json(name = "zone")
    var zone: String,

    @ColumnInfo(name = "lat")
    @Json(name = "lat")
    var latitude: Double,

    @ColumnInfo(name = "lon")
    @Json(name = "lon")
    var longitude: Double,

    @ColumnInfo(name = "is_disabled")
    @Json(name = "is_disabled")
    var isDisabled: Boolean,

    @ColumnInfo(name = "is_reserved")
    @Json(name = "is_reserved")
    var isReserved: Boolean,

    @ColumnInfo(name = "last_reported")
    @Json(name = "last_reported")
    var lastReported: Long,

    @ColumnInfo(name = "current_range_meters")
    @Json(name = "current_range_meters")
    var currentRangeMeters: Double,
)

