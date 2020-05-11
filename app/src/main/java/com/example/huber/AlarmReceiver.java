package com.example.huber;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.legacy.content.WakefulBroadcastReceiver;

import com.example.huber.database.HuberDataBase;

import java.util.Objects;

import static androidx.legacy.content.WakefulBroadcastReceiver.startWakefulService;

public class AlarmReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent taskEditIntent = new Intent(context, MainActivity.class);
        long taskId = intent.getLongExtra(MainActivity.ALARM_ID, -1);
        String station = intent.getStringExtra(MainActivity.STOP_NAME);
        String direction = intent.getStringExtra(MainActivity.DIRECTION_NAME);

        taskEditIntent.putExtra(MainActivity.ALARM_ID, taskId);
        PendingIntent pi = PendingIntent.getActivity(context, 0, taskEditIntent, PendingIntent.FLAG_ONE_SHOT);

        Notification.Builder note = new Notification.Builder(context)
                .setContentTitle(context.getString(R.string.alarm_notification))
                .setContentText(station + ", Richtung " + direction)
                .setSmallIcon(R.drawable.ic_huber)
                .setColor(0xfe0000)
                .setContentIntent(pi);

        String channelId = "Huber";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Huber",
                    NotificationManager.IMPORTANCE_HIGH);
            Objects.requireNonNull(mgr).createNotificationChannel(channel);
            note.setChannelId(channelId);
        }

        Objects.requireNonNull(mgr).notify((int) taskId, note.build());
    }
}
