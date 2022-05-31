package alahyaoui.escooter.radar.viewmodels

import alahyaoui.escooter.radar.models.Scooter
import alahyaoui.escooter.radar.models.ScooterApi
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MapsViewModel : ViewModel() {

    /* Scooters state */
    private val _scootersLiveData = MutableLiveData<List<Scooter>>()

    val scootersLiveData: LiveData<List<Scooter>> = _scootersLiveData

    var nbOfScooters : Int

    /* Maps state */

    var userLocation : Location? = null

    init {
        nbOfScooters = 0
    }

    fun fetchScootersFromApi() {
        viewModelScope.launch {
            val latitude = userLocation?.latitude
            val longitude = userLocation?.longitude
            val nbOfScooters = nbOfScooters
            if (latitude != null && longitude != null) {
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