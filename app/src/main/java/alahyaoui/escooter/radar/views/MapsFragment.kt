package alahyaoui.escooter.radar.views

import alahyaoui.escooter.radar.databinding.FragmentMapsBinding
import alahyaoui.escooter.radar.models.Scooter
import alahyaoui.escooter.radar.utils.Constants.REQUEST_CODE_LOCATION_PERMISSION
import alahyaoui.escooter.radar.utils.ScooterRenderer
import alahyaoui.escooter.radar.utils.TrackingUtility
import alahyaoui.escooter.radar.viewmodels.MapsViewModel
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterManager
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class MapsFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: FragmentMapsBinding

    private val mapsViewModel by viewModels<MapsViewModel>()

    private lateinit var map: GoogleMap

    private lateinit var clusterManager: ClusterManager<Scooter>

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = binding.mapView.getFragment<SupportMapFragment>()
        mapFragment.getMapAsync(callback)
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap

        // Create the ClusterManager class and set the custom renderer
        clusterManager = ClusterManager<Scooter>(requireContext(), map)
        clusterManager.renderer =
            ScooterRenderer(
                requireContext(),
                map,
                clusterManager
            )

        requestPermissions()
        initViewModelObservers()
        initMapTypeFab()
    }

    private fun initViewModelObservers() {
        mapsViewModel.nbOfScootersLiveData.observe(viewLifecycleOwner) {
            mapsViewModel.fetchScootersFromApi()
        }

        mapsViewModel.scootersLiveData.observe(viewLifecycleOwner) {
            addClusteredMarkers()

            // Ensure all places are visible in the map
            val bounds = LatLngBounds.builder()
            val scooters = mapsViewModel.scootersLiveData.value
            if (scooters?.size != 0) {
                scooters?.forEach { bounds.include(it.position) }
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))
            }
        }
    }

    /**
     * Adds markers to the map with clustering support.
     */
    private fun addClusteredMarkers() {
        // Add the places to the ClusterManager
        val scooters = mapsViewModel.scootersLiveData.value
        clusterManager.addItems(scooters)
        clusterManager.cluster()

        clusterManager.setOnClusterItemClickListener { item ->
            val action =
                MapsFragmentDirections.actionMapsDestinationToScooterInfoBottomSheetDestination(item)
            NavHostFragment.findNavController(this).navigate(action)
            return@setOnClusterItemClickListener false
        }

        // When the camera starts moving, change the alpha value of the marker to translucent
        map.setOnCameraMoveStartedListener {
            clusterManager.markerCollection.markers.forEach { it.alpha = 0.3f }
            clusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 0.3f }
        }

        map.setOnCameraIdleListener {
            // When the camera stops moving, change the alpha value back to opaque
            clusterManager.markerCollection.markers.forEach { it.alpha = 1.0f }
            clusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 1.0f }

            // Call clusterManager.onCameraIdle() when the camera stops moving so that re-clustering
            // can be performed when the camera stops moving
            clusterManager.onCameraIdle()
        }
    }

    // Map Type Fab Initialization

    private fun initMapTypeFab() {
        val mapTypeDefaultBackground = binding.mapTypeSelectionView.mapTypeDefaultBackground
        val mapTypeDefaultText = binding.mapTypeSelectionView.mapTypeDefaultText

        val mapTypeSatelliteBackground = binding.mapTypeSelectionView.mapTypeSatelliteBackground
        val mapTypeSatelliteText = binding.mapTypeSelectionView.mapTypeSatelliteText

        val mapTypeTerrainBackground = binding.mapTypeSelectionView.mapTypeTerrainBackground
        val mapTypeTerrainText = binding.mapTypeSelectionView.mapTypeTerrainText

        // When map is initially loaded, determine which map type option to 'select'
        when (map.mapType) {
            GoogleMap.MAP_TYPE_SATELLITE -> {
                mapTypeSatelliteBackground.visibility = View.VISIBLE
                mapTypeSatelliteText.setTextColor(Color.BLUE)
            }
            GoogleMap.MAP_TYPE_TERRAIN -> {
                mapTypeTerrainBackground.visibility = View.VISIBLE
                mapTypeTerrainText.setTextColor(Color.BLUE)
            }
            else -> {
                mapTypeDefaultBackground.visibility = View.VISIBLE
                mapTypeDefaultText.setTextColor(Color.BLUE)
            }
        }

        // Handle selection of the Default map type
        binding.mapTypeSelectionView.mapTypeDefault.setOnClickListener {
            mapTypeDefaultBackground.visibility = View.VISIBLE
            mapTypeSatelliteBackground.visibility = View.INVISIBLE
            mapTypeTerrainBackground.visibility = View.INVISIBLE
            mapTypeDefaultText.setTextColor(Color.BLUE)
            mapTypeSatelliteText.setTextColor(Color.parseColor("#808080"))
            mapTypeTerrainText.setTextColor(Color.parseColor("#808080"))
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

        // Handle selection of the Satellite map type
        binding.mapTypeSelectionView.mapTypeSatellite.setOnClickListener {
            mapTypeDefaultBackground.visibility = View.INVISIBLE
            mapTypeSatelliteBackground.visibility = View.VISIBLE
            mapTypeTerrainBackground.visibility = View.INVISIBLE
            mapTypeDefaultText.setTextColor(Color.parseColor("#808080"))
            mapTypeSatelliteText.setTextColor(Color.BLUE)
            mapTypeTerrainText.setTextColor(Color.parseColor("#808080"))
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        }

        // Handle selection of the terrain map type
        binding.mapTypeSelectionView.mapTypeTerrain.setOnClickListener {
            mapTypeDefaultBackground.visibility = View.INVISIBLE
            mapTypeSatelliteBackground.visibility = View.INVISIBLE
            mapTypeTerrainBackground.visibility = View.VISIBLE
            mapTypeDefaultText.setTextColor(Color.parseColor("#808080"))
            mapTypeSatelliteText.setTextColor(Color.parseColor("#808080"))
            mapTypeTerrainText.setTextColor(Color.BLUE)
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
        }

        // Set click listener on FAB to open the map type selection view
        val mapTypeFAB = binding.mapTypeFAB
        mapTypeFAB.setOnClickListener {

            // Start animator to reveal the selection view, starting from the FAB itself
            val mapTypeSelection = binding.mapTypeSelectionView.mapTypeSelection
            val anim = ViewAnimationUtils.createCircularReveal(
                mapTypeSelection,
                mapTypeSelection.width - (mapTypeFAB.width / 2),
                mapTypeFAB.height / 2,
                mapTypeFAB.width / 2f,
                mapTypeSelection.width.toFloat()
            )
            anim.duration = 200
            anim.interpolator = AccelerateDecelerateInterpolator()

            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    mapTypeSelection.visibility = View.VISIBLE
                }
            })

            anim.start()
            mapTypeFAB.visibility = View.INVISIBLE

        }

        // Set click listener on the map to close the map type selection view
        map.setOnMapClickListener {

            // Conduct the animation if the FAB is invisible (window open)
            if (mapTypeFAB.visibility == View.INVISIBLE) {

                // Start animator close and finish at the FAB position
                val mapTypeSelection = binding.mapTypeSelectionView.mapTypeSelection
                val anim = ViewAnimationUtils.createCircularReveal(
                    mapTypeSelection,
                    mapTypeSelection.width - (mapTypeFAB.width / 2),
                    mapTypeFAB.height / 2,
                    mapTypeSelection.width.toFloat(),
                    mapTypeFAB.width / 2f
                )
                anim.duration = 200
                anim.interpolator = AccelerateDecelerateInterpolator()

                anim.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        mapTypeSelection.visibility = View.INVISIBLE
                    }
                })

                // Set a delay to reveal the FAB. Looks better than revealing at end of animation
                Handler().postDelayed({
                    kotlin.run {
                        mapTypeFAB.visibility = View.VISIBLE
                    }
                }, 100)
                anim.start()
            }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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
    private fun onLocationEnabled() {
        map.isMyLocationEnabled = true
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            mapsViewModel.userLocation = location
            mapsViewModel.fetchScootersFromApi()
        }

        map.setOnMyLocationChangeListener { location ->
            mapsViewModel.userLocation = location
        }
    }
}