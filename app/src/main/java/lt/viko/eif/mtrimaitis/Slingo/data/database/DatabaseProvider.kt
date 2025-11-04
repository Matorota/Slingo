package lt.viko.eif.mtrimaitis.Slingo.data.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "slingo_database"
            )
            .fallbackToDestructiveMigration() // For development - removes data on schema change
            .build()
            INSTANCE = instance
            instance
        }
    }
}

