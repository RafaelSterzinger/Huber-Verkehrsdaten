package com.example.huber.alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;

import com.example.huber.MainActivity;
import com.example.huber.R;

import java.util.Objects;

public class AlarmReceiver extends BroadcastReceiver {

    private static Vibrator v;
    private static Ringtone r;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Objects.requireNonNull(v).vibrate(VibrationEffect.createWaveform(AlarmManager.DEFAULT_VIBRATION, AlarmManager.DEFAULT_REPEAT));

            } else {
                Objects.requireNonNull(v).vibrate(AlarmManager.DEFAULT_VIBRATION_LENGTH);
            }

            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String station = intent.getStringExtra(MainActivity.STATION_NAME);
        String direction = intent.getStringExtra(MainActivity.DIRECTION_NAME);
        long rlb = intent.getLongExtra(MainActivity.RLB, -1);
        int stationUID = intent.getIntExtra(MainActivity.STATION_UID, -1);

        NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra(MainActivity.RLB, rlb);
        notificationIntent.putExtra(MainActivity.DIRECTION_NAME, direction);
        notificationIntent.putExtra(MainActivity.STATION_NAME, station);
        notificationIntent.putExtra(MainActivity.STATION_UID, stationUID);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder note = new Notification.Builder(context)
                .setContentTitle(context.getString(R.string.alarm_notification))
                .setContentText(station + ", Richtung: " + direction)
                .setSmallIcon(R.drawable.ic_huber)
                .setColor(0xfe0000)
                .setCategory(Notification.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(pendingIntent);

        String channelId = "Huber";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Huber",
                    NotificationManager.IMPORTANCE_HIGH);

            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            Objects.requireNonNull(mgr).createNotificationChannel(channel);
            note.setChannelId(channelId);
        }

        Objects.requireNonNull(mgr).notify(0, note.build());
    }

    public static Vibrator getV() {
        return v;
    }

    public static Ringtone getR() {
        return r;
    }
}
