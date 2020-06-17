package com.example.huber.entity

import androidx.room.Dao
import androidx.room.Query

@Dao
interface StationDAO {
    @Query("SELECT * FROM haltestellen")
    fun getAll(): List<Station>

    @Query("SELECT * FROM haltestellen WHERE WGS84_LAT <= :east AND WGS84_LAT >= :west AND WGS84_LON <= :north AND WGS84_LON >= :south")
    fun getInBound(north: Double, east: Double, south: Double, west: Double): List<Station>

    @Query("SELECT * FROM haltestellen WHERE name LIKE :name ORDER BY name LIMIT 10")
    fun getStationsWithNameLike(name: String): List<Station>

    @Query("SELECT * FROM haltestellen WHERE haltestellen_id == :uid")
    fun getStationWithUID(uid: Int): Station

    @Query("SELECT * FROM haltestellen WHERE favorite >= 1")
    fun getFavoriteStations(): List<Station>

    @Query("UPDATE haltestellen SET favorite = :favorite WHERE haltestellen_id = :uid")
    fun updateFavourite(uid: Int, favorite: Boolean): Int
}
