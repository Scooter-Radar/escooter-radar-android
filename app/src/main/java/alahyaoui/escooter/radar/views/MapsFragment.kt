package alahyaoui.escooter.radar.views

import alahyaoui.escooter.radar.R
import alahyaoui.escooter.radar.databinding.FragmentMapsBinding
import alahyaoui.escooter.radar.models.ScooterDatabase
import alahyaoui.escooter.radar.viewmodels.MapsViewModel
import alahyaoui.escooter.radar.viewmodels.MapsViewModelFactory
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    private val REQUEST_LOCATION_PERMISSION = 1


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


        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        enableMyLocation()

        mapsViewModel.scootersLiveData.observe(viewLifecycleOwner, Observer {
            updateMap()
        })

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
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
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address> = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5)
        return addresses[3]
    }

    // Permission handling

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            mMap.setOnMyLocationChangeListener({ location ->
                val userLocation = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation))
                val userAddress = getAddress(userLocation)
                mapsViewModel.fetchScootersFromApi(userAddress.locality)
            })
        } else {
            requestPermissions(
                 arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }
}