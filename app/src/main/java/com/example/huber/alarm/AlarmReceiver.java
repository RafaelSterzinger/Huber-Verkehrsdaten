package com.example.huber.alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.huber.MainActivity;
import com.example.huber.R;

import java.util.Objects;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
       //TODO integrate in notification
        /*
        try {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Objects.requireNonNull(v).vibrate(AlarmManager.DEFAULT_VIBRATION);
            } else {
                Objects.requireNonNull(v).vibrate(AlarmManager.DEFAULT_VIBRATION_LENGTH);
            }
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
         */
        NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        long taskId = intent.getLongExtra(MainActivity.ALARM_ID, -1);
        Intent taskEditIntent = new Intent(context, MainActivity.class);
        taskEditIntent.putExtra(MainActivity.ALARM_ID, taskId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, taskEditIntent, PendingIntent.FLAG_ONE_SHOT);

        String station = intent.getStringExtra(MainActivity.STOP_NAME);
        String direction = intent.getStringExtra(MainActivity.DIRECTION_NAME);

        Notification.Builder note = new Notification.Builder(context)
                .setContentTitle(context.getString(R.string.alarm_notification))
                .setContentText(station + ", Richtung " + direction)
                .setSmallIcon(R.drawable.ic_huber)
                .setColor(0xfe0000)
                .setCategory(Notification.CATEGORY_ALARM)
                .setContentIntent(pendingIntent);

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
