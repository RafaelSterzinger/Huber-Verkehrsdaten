package com.example.huber;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.VibrationEffect;

import java.util.Calendar;
import java.util.Objects;

class AlarmManager {

    static final String ALARM_EVENT = "com.example.huber.HUBER_ALARM";

    @SuppressLint("NewApi")
    static final VibrationEffect DEFAULT_VIBRATION = VibrationEffect.createWaveform(new long[]{0,1000,1000},1);
    static final long DEFAULT_VIBRATION_LENGTH = 60000;

    private AlarmManager() {
    }

    static void setAlarm(Context context, long directionId, String station, String direction, Calendar when) {
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(ALARM_EVENT);
        intent.setPackage("com.example.huber");
        intent.putExtra(MainActivity.ALARM_ID, directionId);
        intent.putExtra(MainActivity.DIRECTION_NAME, direction);
        intent.putExtra(MainActivity.STOP_NAME, station);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        // Necessary if time gets picked in the passed
                        /*
                        if (c.before(Calendar.getInstance())) {
                            c.add(Calendar.DATE, 1);
                        }
                        */

        Objects.requireNonNull(alarmManager).setExact(android.app.AlarmManager.RTC_WAKEUP, when.getTimeInMillis(), pendingIntent);
    }

}
