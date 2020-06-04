package com.example.huber.entity

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.ObservableField
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.huber.BR
import com.google.android.gms.maps.model.Marker
import java.util.*

@Entity(tableName = "haltestellen")
data class Station(
        @ColumnInfo(name = "HALTESTELLEN_ID") @PrimaryKey val uid: Int,
        @ColumnInfo(name = "DIVA") val diva: Int,
        @ColumnInfo(name = "NAME") val name: String,
        @ColumnInfo(name = "GEMEINDE") val municipality: String,
        @ColumnInfo(name = "GEMEINDE_ID") val municipalityID: Int,
        @ColumnInfo(name = "WGS84_LAT") val lat: Double,
        @ColumnInfo(name = "WGS84_LON") val lon: Double
) : BaseObservable() {
    // marker is for removing a station from the map (gets returned when a point is added to the map)
    @Ignore
    var marker: Marker? = null

    @Ignore
    var distanceKm: Double? = null

    @Ignore
    @get:Bindable
    var distanceHours: Int? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.distanceHours)
            notifyPropertyChanged(BR.stationVar)
        }

    @Ignore
    @get:Bindable
    var distanceMinutes: Int? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.distanceMinutes)
        }

    //TODO: remove
    @Ignore
    val testDistance: ObservableField<String>? = null

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