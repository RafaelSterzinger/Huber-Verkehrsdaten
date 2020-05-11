package com.example.huber.entityDAO

import androidx.room.Dao
import androidx.room.Query
import com.example.huber.entity.Station

@Dao
interface StationDAO {
    @Query("SELECT * FROM haltestellen")
    fun getAll(): List<Station>

    // TODO: order by distance of position, else moving the screen while zoomed out will make the stations change randomly
    @Query("SELECT * FROM haltestellen WHERE WGS84_LAT <= :east AND WGS84_LAT >= :west AND WGS84_LON <= :north AND WGS84_LON >= :south")
    fun getInBound(north: Double, east: Double, south: Double, west: Double): List<Station>

    /*
    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): User

*/
}
