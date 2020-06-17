package com.example.huber.entity

import android.util.Log
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.huber.BR
import com.example.huber.MainActivity
import com.example.huber.live.GetDataService
import com.example.huber.live.LiveData
import com.example.huber.live.RetrofitClientInstance
import com.example.huber.live.entity.data.Monitor
import com.example.huber.util.DistanceCalculatorHaversine.distance
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.function.Consumer

@Entity(tableName = "haltestellen")
data class Station(
        @ColumnInfo(name = "HALTESTELLEN_ID") @PrimaryKey val uid: Int,
        @ColumnInfo(name = "DIVA") val diva: Int,
        @ColumnInfo(name = "NAME") val name: String,
        @ColumnInfo(name = "GEMEINDE") val municipality: String,
        @ColumnInfo(name = "GEMEINDE_ID") val municipalityID: Int,
        @ColumnInfo(name = "WGS84_LAT") val lat: Double,
        @ColumnInfo(name = "WGS84_LON") val lon: Double,
        @ColumnInfo(name = "FAVORITE") private var _favorite: Boolean
) : BaseObservable() {

    var favorite: Boolean
        @Bindable get() = _favorite
        set(value) {
            _favorite = value
            marker?.setIcon(if (favorite) BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
            else BitmapDescriptorFactory.defaultMarker())
            notifyPropertyChanged(BR.favorite)
        }


    // Marker is for removing a station from the map (gets returned when a point is added to the map)
    @Ignore
    var marker: Marker? = null
    fun removeMarkerIfExists(): Boolean {
        return if (marker == null) {
            false
        } else {
            marker!!.remove()
            marker = null
            true
        }
    }

    @Ignore
    var distanceKm: Double = 0.0

    @Ignore
    @Bindable
    var distanceHours: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.distanceHours)
        }

    @Ignore
    @Bindable
    var distanceMinutes: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.distanceMinutes)
        }

    @Ignore
    var monitor: List<Monitor>? = null

    fun setDistance(latLng: LatLng?, walkSpeed: Double) {
        Log.d(MainActivity.ACTIVITY_NAME, "Calculating distance for $name")
        val distance = if (latLng != null) distance(latLng.latitude, latLng.longitude, lat, lon) else 0.0
        distanceKm = distance
        distanceHours = (distance / walkSpeed).toInt()
        distanceMinutes = (distance / walkSpeed * 60).toInt() % 60
        Log.d("Distance", "$name $distance $distanceMinutes")
    }

    fun requestLiveData(callback: Consumer<List<Monitor>>) {
        Log.d(MainActivity.ACTIVITY_NAME, "Requesting live data")
        if (monitor != null) {
            callback.accept(monitor!!)
            return
        }

        val request = RetrofitClientInstance.getRetrofitInstance().create(GetDataService::class.java)
        val call = request.getStationLiveData(diva)

        call.enqueue(object : Callback<LiveData> {
            override fun onFailure(call: Call<LiveData>, t: Throwable) {
                Log.d("Error during API call", t.toString())
            }

            override fun onResponse(call: Call<LiveData>, response: Response<LiveData>) {
                if (response.body()?.data?.monitors != null) {
                    monitor = response.body()!!.data.monitors
                    callback.accept(monitor!!)
                } else {
                    monitor = emptyList()
                }
            }
        })
    }


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

    fun getLatLng(): LatLng {
        return LatLng(lat, lon)
    }
}