package com.example.huber.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.huber.entity.Station
import com.example.huber.entityDAO.StationDAO

@Database(
        entities = [Station::class],
        version = 1,
        exportSchema = false)
abstract class HuberDataBase : RoomDatabase() {
    //The companion object is a singleton, and its members can be accessed directly via the name of the containing class
    companion object {
        @Volatile
        private var instance: HuberDataBase? = null
        private val LOCK = Any()

        private const val DATABASE_NAME = "huber"
        private const val DATABASE_DIR = "huber.sqlite"

        /*fun getInstance(context: Context): HuberDataBase {
            return Room
                    .databaseBuilder(context, HuberDataBase::class.java, DATABASE_NAME)
                    .createFromAsset(DATABASE_DIR)
                    .build()
        }*/

        operator fun invoke(context: Context) = instance
                ?: synchronized(LOCK) {
                    instance
                            ?: buildDatabase(context).also { instance = it }
                }

        private fun buildDatabase(context: Context) = Room
                .databaseBuilder(context, HuberDataBase::class.java, DATABASE_NAME)
                .createFromAsset(DATABASE_DIR)
                .build()
    }

    abstract fun stationDao(): StationDAO
}