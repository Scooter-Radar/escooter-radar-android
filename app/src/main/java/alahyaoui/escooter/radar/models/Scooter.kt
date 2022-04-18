package alahyaoui.escooter.radar.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "Scooter")
data class Scooter (
    @PrimaryKey
    var id: UUID,

    @ColumnInfo(name = "bike_id")
    var bikeId: String,

    @ColumnInfo(name = "company")
    var company: String,

    @ColumnInfo(name = "lat")
    var latitude: Double,

    @ColumnInfo(name = "lon")
    var longitude: Double,

    @ColumnInfo(name = "is_disabled")
    var isDisabled: Boolean,

    @ColumnInfo(name = "is_reserved")
    var isReserved: Boolean,

    @ColumnInfo(name = "last_reported")
    var lastReported: Long,

    @ColumnInfo(name = "current_range_meters")
    var currentRangeMeters: Double,
)

