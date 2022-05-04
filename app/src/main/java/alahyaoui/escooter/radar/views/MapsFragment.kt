package alahyaoui.escooter.radar.views

import alahyaoui.escooter.radar.R
import alahyaoui.escooter.radar.databinding.FragmentMapsBinding
import alahyaoui.escooter.radar.utils.Constants.REQUEST_CODE_LOCATION_PERMISSION
import alahyaoui.escooter.radar.utils.TrackingUtility
import alahyaoui.escooter.radar.viewmodels.MapsViewModel
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


class MapsFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentMapsBinding? = null

    private val binding get() = _binding!!

    private lateinit var mapsViewModel: MapsViewModel

    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Get a reference to the ViewModel associated with this fragment.
        mapsViewModel = ViewModelProvider(this)[MapsViewModel::class.java]

        _binding = FragmentMapsBinding.inflate(inflater, container, false)
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
        mapsViewModel.scootersLiveData.observe(viewLifecycleOwner) {
            addScooterMarkers()
        }

        mapsViewModel.userLocationLiveData.observe(viewLifecycleOwner) {
            mapsViewModel.fetchScootersFromApi()
        }

        mapsViewModel.nbOfScootersLiveData.observe(viewLifecycleOwner) {
            mapsViewModel.fetchScootersFromApi()
        }
    }

    private fun addScooterMarkers() {
        val scooters = mapsViewModel.scootersLiveData.value
        if (scooters != null) {
            for (scooter in scooters) {
                val scooterLat = scooter.location.coordinates[1]
                val scooterLong = scooter.location.coordinates[0]
                mMap.addMarker(
                    MarkerOptions().position(LatLng(scooterLat, scooterLong))
                        .title(scooter.company)
                )
            }
        }
    }

    // Permission handling

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

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

    @SuppressLint("MissingPermission")
    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            mMap.isMyLocationEnabled = true
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
            fusedLocationProviderClient.lastLocation.addOnSuccessListener  { location ->
                mapsViewModel.userLocationLiveData.value = location
                mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude)))
            }

            mMap.setOnMyLocationChangeListener { location ->
                mapsViewModel.userLocationLiveData.value = location
            }
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
}