package alahyaoui.escooter.radar.views

import alahyaoui.escooter.radar.R
import alahyaoui.escooter.radar.databinding.FragmentMapsBinding
import alahyaoui.escooter.radar.models.ScooterDatabase
import alahyaoui.escooter.radar.viewmodels.MapsViewModel
import alahyaoui.escooter.radar.viewmodels.MapsViewModelFactory
import android.Manifest
import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*


class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null

    private val binding get() = _binding!!

    private lateinit var mapsViewModel: MapsViewModel

    private lateinit var mMap: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Create an instance of the ViewModel Factory.
        val application = requireNotNull(this.activity).application
        val dataSource = ScooterDatabase.getInstance(application).scooterDao
        val viewModelFactory = MapsViewModelFactory(dataSource)

        // Get a reference to the ViewModel associated with this fragment.
        mapsViewModel = ViewModelProvider(this, viewModelFactory)[MapsViewModel::class.java]

        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        askLocationPermission()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun askLocationPermission() {
        val locationPermissionRequest =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        // Precise location access granted.
                    }
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        // Only approximate location access granted.
                    }
                    else -> {
                        // No location access granted.
                    }
                }
            }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        mMap.isMyLocationEnabled = true

        mapsViewModel.scootersLiveData.observe(viewLifecycleOwner, Observer {
            updateMap()
        })

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.setOnMyLocationChangeListener({ location ->
            val userLocation = LatLng(location.latitude, location.longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation))

            val userAddress = getAddress(userLocation)
            mapsViewModel.fetchScootersFromApi(userAddress.locality)
        })
    }

    private fun updateMap() {
        val scooters = mapsViewModel.scootersLiveData.value
        if (scooters != null) {
            for (scooter in scooters) {
                    mMap.addMarker(
                        MarkerOptions().position(LatLng(scooter.latitude, scooter.longitude))
                            .title(scooter.company)
                    )
            }
        }
    }

    private fun getAddress(latLng: LatLng): Address {
        val geocoder = Geocoder(context, Locale.ENGLISH)
        val addresses: List<Address> = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        return addresses[0]
    }
}