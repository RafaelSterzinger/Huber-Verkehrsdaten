package com.example.huber;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.huber.AlarmReceiver;
import com.example.huber.MainActivity;

import java.util.Calendar;
import java.util.Objects;

public class AlarmManager {


    private AlarmManager() {
    }

    public static void setAlarm(Context context, long directionId, String station, String direction, Calendar when) {

        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
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
