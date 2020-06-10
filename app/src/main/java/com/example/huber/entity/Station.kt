package com.example.huber.entity

import android.util.Log
import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.huber.BR
import com.example.huber.DistanceCalculatorHaversine.distance
import com.example.huber.live.GetDataService
import com.example.huber.live.GetLiveEntryDeserializer
import com.example.huber.live.RetrofitClientInstance
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    //TODO static data loader

    // marker is for removing a station from the map (gets returned when a point is added to the map)
    @Ignore
    var marker: Marker? = null
    fun removeMarkerIfExists(): Boolean {
        if (marker == null) {
            return false
        } else {
            marker!!.remove()
            marker = null
            return true
        }
    }

    @Ignore
    var distanceKm: Double = 0.0

    @Ignore
    var distanceHours: Int = 0
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.distanceHours)
        }

    @Ignore
    var distanceMinutes: Int = 0
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.distanceMinutes)
        }

    @Ignore
    var favourite: Boolean = false
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.favourite)
        }

    @Ignore
    var liveData: ArrayList<LiveEntry> = ArrayList()

    //@BindingAdapter("android:onClick")
    public fun favouriteAll(view: View?) {  // must be view since it gets used as onClickListener(View view) in entry layout
        // TODO: connect with DB
        favourite = !favourite
        Log.d("Station", "favouriteClick " + favourite)
    }

    fun setDistance(latLng: LatLng?, walkSpeed: Double) {
        val request = RetrofitClientInstance
                .getRetrofitInstance()
                .create(GetDataService::class.java)
        val call = request.getStationLiveData(diva)

        call.enqueue(object : Callback<List<LiveEntry>> {
            override fun onResponse(call: Call<List<LiveEntry>>, response: Response<List<LiveEntry>>) {
                print("recieved response");

            }

            override fun onFailure(call: Call<List<LiveEntry>>, t: Throwable) {
                println(t.message)
                t.printStackTrace()
                TODO("Not yet implemented")
            }
        })

        val distance = if (latLng != null) distance(latLng.latitude, latLng.longitude, lat, lon) else 0.0
        distanceKm = distance
        distanceHours = (distance / walkSpeed).toInt()
        distanceMinutes = (distance / walkSpeed * 60).toInt() % 60
        Log.d("Distance", "$name $distance $distanceMinutes")
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