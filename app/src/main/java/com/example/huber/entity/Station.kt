package com.example.huber.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.Marker

@Entity(tableName = "haltestellen")
data class Station(
        @ColumnInfo(name = "HALTESTELLEN_ID") @PrimaryKey val uid: Int,
        @ColumnInfo(name = "DIVA") val diva: Int,
        @ColumnInfo(name = "NAME") val name: String,
        @ColumnInfo(name = "GEMEINDE") val municipality: String,
        @ColumnInfo(name = "GEMEINDE_ID") val municipalityID: Int,
        @ColumnInfo(name = "WGS84_LAT") val lat: Double,
        @ColumnInfo(name = "WGS84_LON") val lon: Double
)
{
    @Ignore var marker: Marker? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Station

        if (uid != other.uid) return false

        return true
    }

    override fun hashCode(): Int {
        return uid
    }
}