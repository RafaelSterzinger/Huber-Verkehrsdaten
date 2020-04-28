package com.example.huber.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Stop(
        @ColumnInfo(name = "HALTESTELLEN_ID") @PrimaryKey val uid: Int,
        @ColumnInfo(name = "TYP") val type: String,
        @ColumnInfo(name = "DIVA") val diva: Int,
        @ColumnInfo(name = "NAME") val name: String,
        @ColumnInfo(name = "GEMEINDE") val municipality: String,
        @ColumnInfo(name = "GEMEINDE_ID") val municipalityID: Int,
        @ColumnInfo(name = "WGS84_LAT") val lat: Double,
        @ColumnInfo(name = "WGS84_LON") val lon: Double
)