package alahyaoui.escooter.radar.utils

import alahyaoui.escooter.radar.R
import alahyaoui.escooter.radar.models.Scooter
import alahyaoui.escooter.radar.utils.DateUtility.convertLongToTime
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class MarkerInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(marker: Marker): View? {
        // 1. Get tag
        val scooter = marker.tag as? Scooter ?: return null

        // 2. Inflate view and set title, address
        val view = LayoutInflater.from(context).inflate(R.layout.marker_info_contents, null)
        view.findViewById<TextView>(R.id.text_view_company).text = "Company: ${scooter.company}"
        view.findViewById<TextView>(R.id.text_view_city).text = "City: ${scooter.city}"
        view.findViewById<TextView>(R.id.text_view_is_disabled).text = "Is disabled: ${scooter.isDisabled}"
        view.findViewById<TextView>(R.id.text_view_is_reserved).text = "Is reserved: ${scooter.isReserved}"
        view.findViewById<TextView>(R.id.text_view_last_reported).text = "Last time reported: ${convertLongToTime(scooter.lastReported)}"
        view.findViewById<TextView>(R.id.text_view_current_range_meters).text = "Travelable distance remaining: ${scooter.currentRangeMeters}m"

        return view
    }

    override fun getInfoWindow(marker: Marker): View? {
        // Return null to indicate that the default window (white bubble) should be used
        return null
    }
}