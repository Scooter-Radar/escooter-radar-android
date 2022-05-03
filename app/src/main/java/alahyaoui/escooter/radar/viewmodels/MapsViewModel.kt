package alahyaoui.escooter.radar.viewmodels

import alahyaoui.escooter.radar.models.Scooter
import alahyaoui.escooter.radar.models.ScooterApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MapsViewModel() : ViewModel() {

    /* Scooters state */
    private val _scootersLiveData = MutableLiveData<List<Scooter>>()

    val scootersLiveData: LiveData<List<Scooter>> = _scootersLiveData

    fun fetchScootersFromApi(latitude: Double, longitude: Double, degree: Double){
        viewModelScope.launch {
            try {
                _scootersLiveData.value = ScooterApi.retrofitService.getScootersByLocationWithinDegree(latitude, longitude, degree)
            } catch (e: Exception) {
                _scootersLiveData.value = listOf()
            }
        }
    }
}