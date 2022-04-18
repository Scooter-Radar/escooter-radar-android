package alahyaoui.escooter.radar.models

import alahyaoui.escooter.radar.utils.DateConverter
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Scooter::class], version = 1)
@TypeConverters(DateConverter::class)
abstract class ScooterDatabase : RoomDatabase() {

    abstract val scooterDao: ScooterDao

    companion object {
        @Volatile
        private var INSTANCE: ScooterDatabase? = null

        fun getInstance(context: Context): ScooterDatabase {
            synchronized(this) {

                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ScooterDatabase::class.java,
                        "scooter_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}