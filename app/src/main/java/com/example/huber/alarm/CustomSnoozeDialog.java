package com.example.huber.alarm;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.huber.R;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;

public class CustomSnoozeDialog extends DialogFragment {

    private long rlb;
    private String station;
    private String direction;

    public CustomSnoozeDialog(long rlb, String station, String direction) {
        this.rlb = rlb;
        this.station = station;
        this.direction = direction;
    }

    @Override
    public void onStart() {
        super.onStart();
        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow())
                .setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Vibrator v = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Objects.requireNonNull(v).vibrate(VibrationEffect.createWaveform(new long[]{0, 1000, 1000}, 1));
        } else {
            Objects.requireNonNull(v).vibrate(60000);
        }

        int[] placeholder = new int[]{1, 3, 12};


        //TODO need to bind values
        //EntryBinding binding = DataBindingUtil.inflate(inflater, R.layout.snooze_config, container, false);
        View view = inflater.inflate(R.layout.snooze_config, container, false);
        ((ListView) view.findViewById(R.id.future_arrivals)).setAdapter(new ArrayAdapter<>(view.getContext(), R.layout.single_choice_layout, Arrays.stream(placeholder).mapToObj(entry -> entry + "'").toArray(String[]::new)));

        /*
        final int[] po = {-1};
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                po[0] = position;
            }
        });
         */

        ((ListView) view.findViewById(R.id.future_arrivals)).setOnItemClickListener(
                (parent, view1, position, id) -> Log.d("Clicked entry", String.valueOf(position))
        );
        view.findViewById(R.id.ok).setOnClickListener(event -> {
            v.cancel();
            dismiss();
        });
        view.findViewById(R.id.snooze).setOnClickListener(event -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.MINUTE, 1);
            AlarmManager.setAlarm(requireActivity(), 1000, station, direction, calendar);
            v.cancel();
            dismiss();
        });

        // Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        return view;
    }
}
