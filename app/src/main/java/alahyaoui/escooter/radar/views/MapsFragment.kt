package alahyaoui.escooter.radar.views

import alahyaoui.escooter.radar.R
import alahyaoui.escooter.radar.databinding.FragmentMapsBinding
import alahyaoui.escooter.radar.models.Scooter
import alahyaoui.escooter.radar.utils.Constants.REQUEST_CODE_LOCATION_PERMISSION
import alahyaoui.escooter.radar.utils.MarkerInfoWindowAdapter
import alahyaoui.escooter.radar.utils.ScooterRenderer
import alahyaoui.escooter.radar.utils.TrackingUtility
import alahyaoui.escooter.radar.viewmodels.MapsViewModel
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.ktx.addCircle
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


class MapsFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: FragmentMapsBinding

    private lateinit var mapsViewModel: MapsViewModel

    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Get a reference to the ViewModel associated with this fragment.
        mapsViewModel = ViewModelProvider(this)[MapsViewModel::class.java]

        binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        requestPermissions()

        initViewModelObservers()
    }

    private fun initViewModelObservers(){
        mapsViewModel.nbOfScootersLiveData.observe(viewLifecycleOwner) {
            mapsViewModel.fetchScootersFromApi()
        }

        mapsViewModel.scootersLiveData.observe(viewLifecycleOwner) {
            addClusteredMarkers()

            // Ensure all places are visible in the map
            val bounds = LatLngBounds.builder()
            val scooters = mapsViewModel.scootersLiveData.value
            if(scooters?.size != 0){
                scooters?.forEach { bounds.include(it.position) }
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))
            }
        }
    }

    /**
     * Adds markers to the map with clustering support.
     */
    private fun addClusteredMarkers() {
        // Create the ClusterManager class and set the custom renderer
        val clusterManager = ClusterManager<Scooter>(requireContext(), mMap)
        clusterManager.renderer =
            ScooterRenderer(
                requireContext(),
                mMap,
                clusterManager
            )

        // Set custom info window adapter
        clusterManager.markerCollection.setInfoWindowAdapter(MarkerInfoWindowAdapter(requireContext()))

        // Add the places to the ClusterManager
        val scooters = mapsViewModel.scootersLiveData.value
        clusterManager.addItems(scooters)
        clusterManager.cluster()

        // When the camera starts moving, change the alpha value of the marker to translucent
        mMap.setOnCameraMoveStartedListener {
            clusterManager.markerCollection.markers.forEach { it.alpha = 0.3f }
            clusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 0.3f }
        }

        mMap.setOnCameraIdleListener {
            // When the camera stops moving, change the alpha value back to opaque
            clusterManager.markerCollection.markers.forEach { it.alpha = 1.0f }
            clusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 1.0f }

            // Call clusterManager.onCameraIdle() when the camera stops moving so that re-clustering
            // can be performed when the camera stops moving
            clusterManager.onCameraIdle()
        }
    }

    // Permission handling

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        onLocationEnabled()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            onLocationEnabled()
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app",
                REQUEST_CODE_LOCATION_PERMISSION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app",
                REQUEST_CODE_LOCATION_PERMISSION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun onLocationEnabled(){
        mMap.isMyLocationEnabled = true
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient.lastLocation.addOnSuccessListener  { location ->
            mapsViewModel.userLocation = location
            mapsViewModel.fetchScootersFromApi()
        }

        mMap.setOnMyLocationChangeListener { location ->
            mapsViewModel.userLocation = location
        }
    }
}