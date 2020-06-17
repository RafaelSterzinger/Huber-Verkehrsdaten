package com.example.huber.alarm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.huber.R;
import com.example.huber.entity.Station;
import com.example.huber.live.entity.Line;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CustomAlarmDialog extends MaterialAlertDialogBuilder {

    private MaterialAlertDialogBuilder builder;

    public CustomAlarmDialog(@NonNull Context context, Station station) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        // Necessary to access values from time picker and spinner at onClick
        @SuppressLint("InflateParams") View config = inflater.inflate(R.layout.alarm_config, null);
        TimePicker tp = config.findViewById(R.id.time_picker);
        Spinner sp = config.findViewById(R.id.direction_picker);

        Map<String, Integer> directions;

        if (station.getMonitor() != null) {
            directions = station.getMonitor().stream().collect(
                    Collectors.toMap(
                            monitor -> {
                                Line line = monitor.getLines().get(0);
                                return line.getName() + " " + line.getTowards();
                            },
                            monitor -> monitor.getLocationStop().getProperties().getAttributes().getRbl()
                    ));
            if (directions.size() == 0) {
                directions.put("Keine Richtung", -1);
            }
        } else {
            directions = new HashMap<>();
            directions.put("Keine Richtung", -1);
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, new ArrayList<>(directions.keySet()));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(arrayAdapter);

        this.builder = new MaterialAlertDialogBuilder(context)
                .setTitle(Objects.requireNonNull(station).getName())
                .setView(config)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.HOUR_OF_DAY, tp.getHour());
                    c.set(Calendar.MINUTE, tp.getMinute());
                    c.set(Calendar.SECOND, 0);
                    String selection = sp.getSelectedItem().toString();
                    Integer rbl = directions.get(selection);
                    AlarmManager.setAlarm(context, rbl != null ? rbl : -1, station, sp.getSelectedItem().toString(), c);
                });
    }

    @Override
    public AlertDialog show() {
        return builder.show();
    }
}
