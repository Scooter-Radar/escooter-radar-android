package alahyaoui.escooter.radar.utils

import alahyaoui.escooter.radar.R
import alahyaoui.escooter.radar.models.Scooter
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator


class ScooterRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<Scooter>
) : DefaultClusterRenderer<Scooter>(context, map, clusterManager) {

    /**
     * The icon to use for lime e-scooter cluster item
     */
    private val limeIcon: BitmapDescriptor by lazy {
        BitmapHelper.vectorToBitmap(
            context,
            R.drawable.ic_lime_logo,
        )
    }

    /**
     * The icon to use for each bird e-scooter cluster item
     */
    private val birdIcon: BitmapDescriptor by lazy {
        BitmapHelper.vectorToBitmap(
            context,
            R.drawable.ic_bird_logo,
        )
    }

    /**
     * The icon to use for each pony e-scooter cluster item
     */
    private val ponyIcon: BitmapDescriptor by lazy {
        BitmapHelper.vectorToBitmap(
            context,
            R.drawable.ic_pony_logo,
        )
    }

    /**
     * The icon to use for each cluster item
     */
    private val spinIcon: BitmapDescriptor by lazy {
        BitmapHelper.vectorToBitmap(
            context,
            R.drawable.ic_spin_logo,
        )
    }

    /**
     * The icon to use for each cluster item
     */
    private val defaultIcon: BitmapDescriptor by lazy {
        BitmapHelper.vectorToBitmap(
            context,
            R.drawable.ic_baseline_electric_scooter,
        )
    }

    /**
     * Method called before the cluster item (i.e. the marker) is rendered. This is where marker
     * options should be set
     */
    override fun onBeforeClusterItemRendered(item: Scooter, markerOptions: MarkerOptions) {
        val scooterIcon: BitmapDescriptor =
            when (item.company.lowercase()) {
                "lime" -> limeIcon
                "bird" -> birdIcon
                "pony" -> ponyIcon
                "spin" -> spinIcon
                else -> defaultIcon
            }
        markerOptions
            .position(item.position)
            .icon(scooterIcon)
    }

    /**
     * Method called right after the cluster item (i.e. the marker) is rendered. This is where
     * properties for the Marker object should be set.
     */
    override fun onClusterItemRendered(clusterItem: Scooter, marker: Marker) {
        marker.tag = clusterItem
    }

    private val iconGenerator: IconGenerator

    init {
        iconGenerator = IconGenerator(context)
        val clusterIcon = context.resources.getDrawable(R.drawable.cluster_background, null)
        iconGenerator.setBackground(clusterIcon)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val clusterView: View = inflater.inflate(R.layout.cluster_view, null, false)
        iconGenerator.setContentView(clusterView)
    }

    override fun onBeforeClusterRendered(cluster: Cluster<Scooter>, markerOptions: MarkerOptions) {
        iconGenerator.makeIcon(cluster.size.toString())
        val icon = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon())
        markerOptions.icon(icon)
    }
}