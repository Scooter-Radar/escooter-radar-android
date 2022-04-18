package alahyaoui.escooter.radar.models

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ScooterDao {

    // INSERT
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scooter: Scooter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(scooters: List<Scooter>)

    // UPDATE
    @Update
    suspend fun update(scooter: Scooter)

    @Update
    suspend fun updateAll(scooters: List<Scooter>)

    // DELETE
    @Delete
    fun delete(scooter: Scooter)

    @Delete
    fun deleteAll()

    // GET
    @Query("SELECT * FROM Scooter")
    fun getAll(): LiveData<List<Scooter>>

    @Query("SELECT * from Scooter WHERE company = :company")
    fun getAllByCompany(company: String): LiveData<List<Scooter>>
}