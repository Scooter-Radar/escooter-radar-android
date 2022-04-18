package alahyaoui.escooter.radar.viewmodels

import alahyaoui.escooter.radar.models.Scooter
import alahyaoui.escooter.radar.models.ScooterDao
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class MapsViewModel(val database: ScooterDao) : ViewModel() {

    /* Scooters state */
    public val scootersLiveData: LiveData<List<Scooter>>
        get() = getScooters()

    private fun getScooters(): LiveData<List<Scooter>> {
        return database.getAll()
    }
}