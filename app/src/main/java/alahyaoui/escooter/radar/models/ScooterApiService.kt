package alahyaoui.escooter.radar.models

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import alahyaoui.escooter.radar.utils.ScooterBackend

/**
 * Build the Moshi object that Retrofit will be using, making sure to add the Kotlin adapter for
 * full Kotlin compatibility.
 */
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/**
 * Use the Retrofit builder to build a retrofit object using a Moshi converter with our Moshi
 * object.
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(ScooterBackend.BASE_URL)
    .build()

/**
 * A public interface that exposes the [getScooters] method
 */
interface ScooterApiService {
    /**
     * Returns a [List] of [Scooter] and this method can be called from a Coroutine.
     * The @GET annotation indicates that the "scooter" endpoint will be requested with the GET
     * HTTP method
     */
    @GET("scooter/provider")
    suspend fun getScootersByCompany(@Query("company") zone: String): List<Scooter>

    @GET("scooter/location/zone")
    suspend fun getScootersByCity(@Query("city") zone: String): List<Scooter>

    @GET("scooter/location/within")
    suspend fun getScootersByLocationWithinDegree(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("degree") degree: Double
    ): List<Scooter>

    @GET("scooter/location/near")
    suspend fun getScootersNearLocation(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("limit") nbOfScooters: Int
    ): List<Scooter>
}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object ScooterApi {
    val retrofitService: ScooterApiService by lazy { retrofit.create(ScooterApiService::class.java) }
}
