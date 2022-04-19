package alahyaoui.escooter.radar.viewmodels

import alahyaoui.escooter.radar.models.Scooter
import alahyaoui.escooter.radar.models.ScooterApi
import alahyaoui.escooter.radar.models.ScooterDao
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

enum class ScooterApiStatus { LOADING, ERROR, DONE }

class MapsViewModel(val database: ScooterDao) : ViewModel() {

    /* Request status */
    private val _statusLiveData = MutableLiveData<ScooterApiStatus>()

    val status: LiveData<ScooterApiStatus> = _statusLiveData


    /* Scooters state */
    private val _scootersLiveData = MutableLiveData<List<Scooter>>()

    public val scootersLiveData: LiveData<List<Scooter>> = _scootersLiveData
        //get() = getScooters()

    /*private fun getScooters(): LiveData<List<Scooter>> {
        return database.getAll()
    }*/

    init {
        //fetchScootersFromApi()
    }

    public fun fetchScootersFromApi(zone: String){
        viewModelScope.launch {
            _statusLiveData.value = ScooterApiStatus.LOADING
            try {
                _scootersLiveData.value = ScooterApi.retrofitService.getScooters(zone)
                _statusLiveData.value = ScooterApiStatus.DONE
            } catch (e: Exception) {
                _scootersLiveData.value = listOf()
                _statusLiveData.value = ScooterApiStatus.ERROR
            }
        }
    }

    public fun fetchScootersFromApiOffline(zone: String){
        viewModelScope.launch {
            _statusLiveData.value = ScooterApiStatus.LOADING
            try {
                database.insertAll(ScooterApi.retrofitService.getScooters(zone))
                _statusLiveData.value = ScooterApiStatus.DONE
            } catch (e: Exception) {
                _statusLiveData.value = ScooterApiStatus.ERROR
            }
        }
        _scootersLiveData.value = database.getAll().value
    }
}