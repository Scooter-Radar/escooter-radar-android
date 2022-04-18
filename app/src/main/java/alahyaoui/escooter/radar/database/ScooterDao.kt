package alahyaoui.escooter.radar.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ScooterDao {
    @Insert
    suspend fun insert(scooter: Scooter)

    @Update
    suspend fun update(scooter: Scooter)

    @Query("SELECT * FROM Scooter")
    fun getAll(): LiveData<List<Scooter>>

    @Query("SELECT * from Scooter WHERE company = :company")
    suspend fun getAllByCompany(company: String): LiveData<List<Scooter>>
}