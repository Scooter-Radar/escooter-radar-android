package alahyaoui.escooter.radar.utils

import alahyaoui.escooter.radar.R
import alahyaoui.escooter.radar.models.Scooter
import android.content.Context
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class ScooterRenderer (
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<Scooter>
) : DefaultClusterRenderer<Scooter>(context, map, clusterManager) {

    /**
     * The icon to use for each cluster item
     */
    private val escooterIcon: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(context,
            R.color.colorPrimary
        )
        BitmapHelper.vectorToBitmap(
            context,
            R.drawable.ic_action_escooter,
            color
        )
    }

    /**
     * Method called before the cluster item (i.e. the marker) is rendered. This is where marker
     * options should be set
     */
    override fun onBeforeClusterItemRendered(item: Scooter, markerOptions: MarkerOptions) {
        markerOptions.title(item.title)
            .position(item.position)
            .icon(escooterIcon)
    }

    /**
     * Method called right after the cluster item (i.e. the marker) is rendered. This is where
     * properties for the Marker object should be set.
     */
    override fun onClusterItemRendered(clusterItem: Scooter, marker: Marker) {
        marker.tag = clusterItem
    }
}