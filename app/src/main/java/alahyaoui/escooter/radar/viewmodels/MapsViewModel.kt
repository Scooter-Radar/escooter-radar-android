package alahyaoui.escooter.radar.viewmodels

import alahyaoui.escooter.radar.models.Scooter
import alahyaoui.escooter.radar.models.ScooterApi
import android.location.Location
import android.location.LocationManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MapsViewModel : ViewModel() {

    /* Scooters state */
    private val _scootersLiveData = MutableLiveData<List<Scooter>>()

    val scootersLiveData: LiveData<List<Scooter>> = _scootersLiveData

    var nbOfScooters: Int

    /* Maps state */
    var origin: Location
    var destination: Location

    init {
        origin = Location(LocationManager.GPS_PROVIDER)
        destination = Location(LocationManager.GPS_PROVIDER)
        nbOfScooters = 0
    }

    fun fetchScootersFromApi() {
        viewModelScope.launch {
            val latitude = origin.latitude
            val longitude = origin.longitude
            if (latitude != 0.0 && longitude != 0.0 && nbOfScooters != 0) {
                try {
                    _scootersLiveData.value = ScooterApi.retrofitService.getScootersNearLocation(
                        latitude,
                        longitude,
                        nbOfScooters
                    )
                } catch (e: Exception) {
                    _scootersLiveData.value = listOf()
                }
            }
        }
    }
}