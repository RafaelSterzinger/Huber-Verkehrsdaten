package com.example.huber.entity

import com.google.android.gms.maps.model.LatLng
import java.util.*
import kotlin.collections.ArrayList

data class LiveEntry(val diva: Int, val stationName: String, val geometry: LatLng, val directionInfo: DirectionInfo, val departures: ArrayList<DepartureTime>)

data class DepartureTime(var countdown: Int, val timePlanned: String, var timeReal: String)

data class DirectionInfo(val direction: Char, val lineId: Int, val name: String, val realtimeSupported: Boolean, val richtungsId: Int, val towards: String, val type: String) {
    var rbl: Int? = null
        set(value) {
            field = value
        }
}