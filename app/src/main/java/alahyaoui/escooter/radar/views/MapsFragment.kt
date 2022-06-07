package alahyaoui.escooter.radar.views

import alahyaoui.escooter.radar.R
import alahyaoui.escooter.radar.databinding.FragmentMapsBinding
import alahyaoui.escooter.radar.models.Scooter
import alahyaoui.escooter.radar.utils.MapsApiUrls
import alahyaoui.escooter.radar.utils.ScooterRenderer
import alahyaoui.escooter.radar.utils.TrackingUtility
import alahyaoui.escooter.radar.viewmodels.MapsViewModel
import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.maps.android.clustering.ClusterManager
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


class MapsFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: FragmentMapsBinding

    private val mapsViewModel by viewModels<MapsViewModel>()

    /* Map attributes */
    private lateinit var map: GoogleMap
    private lateinit var clusterManager: ClusterManager<Scooter>
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    /* Preference attributes */
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

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

        // Shared Preferences initialization
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        editor = sharedPreferences.edit()
        mapsViewModel.nbOfScooters =
            Integer.parseInt(sharedPreferences.getString("nb_of_scooters", "100"))

        // Map initialization
        val mapFragment = binding.mapView.getFragment<SupportMapFragment>()
        mapFragment.getMapAsync(callback)
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap

        // Create the ClusterManager class and set the custom renderer
        clusterManager = ClusterManager<Scooter>(requireContext(), map)
        clusterManager.renderer = ScooterRenderer(requireContext(), map, clusterManager)

        requestPermissions()
        initViewModelObservers()
        initLocationFab()
        initMapTypeFab()
        initMapType()
        initCompassFab()
        initDirectionFab()
    }

    private fun initViewModelObservers() {
        mapsViewModel.scootersLiveData.observe(viewLifecycleOwner) { scooters ->
            addClusteredMarkers()

            // Ensure all places are visible in the map
            val bounds = LatLngBounds.builder()
            scooters.forEach { bounds.include(it.position) }
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))
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

            mapsViewModel.apply {
                destination.latitude = item.location.coordinates[1]
                destination.longitude = item.location.coordinates[0]
            }
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

    private fun initDirectionFab() {
        map.uiSettings.isMapToolbarEnabled = false
        binding.directionFAB.setOnClickListener {
            var path = "${MapsApiUrls.directionBaseUrl}/?api=1"
            val origin = mapsViewModel.origin
            val destination = mapsViewModel.destination

            if (origin.latitude != 0.0 && origin.longitude != 0.0) {
                path += "&origin=${origin.latitude},${origin.longitude}"
            }

            if (destination.latitude != 0.0 && destination.longitude != 0.0) {
                path += "&destination=${destination.latitude},${destination.longitude}"
            }

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(path)
            )
            startActivity(intent)
        }
    }

    private fun initLocationFab() {
        map.uiSettings.isMyLocationButtonEnabled = false
        binding.mapLocationFAB.setOnClickListener {
            val location = mapsViewModel.origin
            if (location.latitude != 0.0 && location.longitude != 0.0) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLng(userLatLng))
            }
        }
    }

    private fun initCompassFab() {
        map.uiSettings.isCompassEnabled = false
        binding.compassFAB.setOnClickListener {
            val cameraPosition =
                CameraPosition.Builder(map.cameraPosition).bearing(0.0F).build()
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    // Map Type Initialization

    private fun initMapType() {
        map.mapType = Integer.parseInt(sharedPreferences.getString("map_type", "1"))

        binding.mapTypeSelectionView.apply {
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
                updateMapType(GoogleMap.MAP_TYPE_NORMAL)
            }

            // Handle selection of the Satellite map type
            binding.mapTypeSelectionView.mapTypeSatellite.setOnClickListener {
                mapTypeDefaultBackground.visibility = View.INVISIBLE
                mapTypeSatelliteBackground.visibility = View.VISIBLE
                mapTypeTerrainBackground.visibility = View.INVISIBLE
                mapTypeDefaultText.setTextColor(Color.parseColor("#808080"))
                mapTypeSatelliteText.setTextColor(Color.BLUE)
                mapTypeTerrainText.setTextColor(Color.parseColor("#808080"))
                updateMapType(GoogleMap.MAP_TYPE_SATELLITE)
            }

            // Handle selection of the terrain map type
            binding.mapTypeSelectionView.mapTypeTerrain.setOnClickListener {
                mapTypeDefaultBackground.visibility = View.INVISIBLE
                mapTypeSatelliteBackground.visibility = View.INVISIBLE
                mapTypeTerrainBackground.visibility = View.VISIBLE
                mapTypeDefaultText.setTextColor(Color.parseColor("#808080"))
                mapTypeSatelliteText.setTextColor(Color.parseColor("#808080"))
                mapTypeTerrainText.setTextColor(Color.BLUE)
                updateMapType(GoogleMap.MAP_TYPE_TERRAIN)
            }
        }
    }

    private fun updateMapType(mapType: Int) {
        map.mapType = mapType
        editor.putString("map_type", "$mapType")
        editor.apply()
    }

    private fun initMapTypeFab() {
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

    private var upgradeAsked: Boolean = false

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            onLocationEnabled()
            Log.e("MapsFragment", "Je suis ici")
        } else if (TrackingUtility.hasOnlyCoarseLocationPermissions(requireContext()) && upgradeAsked) {
            onLocationEnabled()
            Log.e("MapsFragment", "Je suis la")
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (TrackingUtility.hasOnlyCoarseLocationPermissions(requireContext())) {
            if (!upgradeAsked) {
                upgradeAsked = true
                requestPermissions()
            }
        } else if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
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
        } else {
            TrackingUtility.requestLocationPermissions(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun onLocationEnabled() {
        map.isMyLocationEnabled = true
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        fetchLastLocation()
    }

    @SuppressLint("MissingPermission")
    private fun fetchLastLocation() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location == null) {
                fetchCurrentLocation()
            } else {
                mapsViewModel.origin = location
                mapsViewModel.fetchScootersFromApi()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation() {
        fusedLocationProviderClient.getCurrentLocation(
            PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                    CancellationTokenSource().token

                override fun isCancellationRequested() = false
            }).addOnSuccessListener { location: Location? ->
            if (location == null) {
                Toast.makeText(requireContext(), getString(R.string.cannot_get_location_error_message), Toast.LENGTH_LONG).show()
            } else {
                mapsViewModel.origin = location
                mapsViewModel.fetchScootersFromApi()
            }
        }
    }
}