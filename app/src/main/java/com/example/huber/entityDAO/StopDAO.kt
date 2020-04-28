package com.example.huber.entityDAO

import androidx.room.Dao
import androidx.room.Query
import com.example.huber.entity.Stop

@Dao
interface StopDAO {
    //@Query("SELECT * FROM haltestelle")
    //fun getAll(): List<Stop>

    /*
    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): User

*/
}
