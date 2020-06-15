package com.example.huber.alarm;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.huber.MainActivity;
import com.example.huber.entity.Station;

import java.util.Calendar;
import java.util.Objects;

public class AlarmManager {

    public static final String ALARM_EVENT = "com.example.huber.HUBER_ALARM";

    // Is checked when used, constant declaration for usage
    /*
    @SuppressLint("NewApi")
    static final VibrationEffect DEFAULT_VIBRATION = VibrationEffect.createWaveform(new long[]{0, 1000, 1000}, 1);
    static final long DEFAULT_VIBRATION_LENGTH = 60000;

     */

    private AlarmManager() {
    }

    public static void setAlarm(Context context, long rlb, Station station, String direction, Calendar when) {
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(ALARM_EVENT);
        intent.setPackage("com.example.huber");
        intent.putExtra(MainActivity.RLB, rlb);
        intent.putExtra(MainActivity.DIRECTION_NAME, direction);
        intent.putExtra(MainActivity.STATION_NAME, station.getName());
        intent.putExtra(MainActivity.STATION_UID, station.getUid());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        // TODO enable for release
        // Necessary if time gets picked in the passed
        if (when.before(Calendar.getInstance())) {
            //when.add(Calendar.DATE, 1);
        }

        Objects.requireNonNull(alarmManager).setExact(android.app.AlarmManager.RTC_WAKEUP, when.getTimeInMillis(), pendingIntent);
    }

}
