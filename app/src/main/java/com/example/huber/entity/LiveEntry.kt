package com.example.huber.entity

import com.google.android.gms.maps.model.LatLng

data class LiveEntry (val direction: Char, val name:String, val richtungsId: Int, val towards: String, val type: String, val latLng: LatLng, val rbl: Int?, val departures: ArrayList<Departure>) {


}

