package com.example.huber.alarm;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.example.huber.R;
import com.example.huber.databinding.SnoozeConfigBinding;
import com.example.huber.entity.Station;
import com.example.huber.live.entity.Monitor;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CustomSnoozeDialog extends DialogFragment {

    private long rlb;
    private Station station;
    private String direction;
    private boolean fromNotification;
    private Vibrator v;

    public CustomSnoozeDialog(long rlb, Station station, String direction, boolean fromNotification) {
        this.rlb = rlb;
        this.station = station;
        this.direction = direction;
        this.fromNotification = fromNotification;
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
        v = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);
        if (!fromNotification) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Objects.requireNonNull(v).vibrate(VibrationEffect.createWaveform(AlarmManager.DEFAULT_VIBRATION, AlarmManager.DEFAULT_REPEAT));
            } else {
                Objects.requireNonNull(v).vibrate(AlarmManager.DEFAULT_VIBRATION_LENGTH);
            }
        }


        SnoozeConfigBinding binding = DataBindingUtil.inflate(inflater, R.layout.snooze_config, container, false);
        binding.setStation(station);
        binding.setDirection(direction);

        View view = binding.getRoot();

        view.findViewById(R.id.ok).setOnClickListener(event -> {
            dismiss();
        });

        AtomicInteger selectionValue = new AtomicInteger();

        //TODO set in preferences as time to prepare
        int temp = station.getDistanceMinutes() + station.getDistanceHours() * 60;
        final int walkingDistance = temp > 0 ? temp : 5;

        Button snooze = view.findViewById(R.id.snooze);
        snooze.setOnClickListener(event -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            calendar.add(Calendar.MINUTE, (selectionValue.get() - walkingDistance));
            AlarmManager.setAlarm(requireActivity(), rlb, station, direction, calendar);
            dismiss();
        });

        if (station.getMonitor() != null) {
            Monitor directionFromRLB = station.getMonitor().stream().filter(monitor -> monitor.getLocationStop().getProperties().getAttributes().getRbl() == rlb).findFirst().orElse(null);

            if (directionFromRLB != null) {
                List<Integer> departures = directionFromRLB.getLines().get(0).getDepartures().getDeparture().stream().map(departure -> departure.getDepartureTime().getCountdown()).collect(Collectors.toList());

                if (departures.size() >= 1) {
                    binding.setDeparture(departures.get(0));

                    ListView futureDepartures = view.findViewById(R.id.future_arrivals);
                    futureDepartures.setAdapter(new ArrayAdapter<>(view.getContext(), R.layout.single_choice_layout, departures.stream().filter(countdown -> countdown > walkingDistance).map(entry -> entry + "'").collect(Collectors.toList())));

                    futureDepartures.setOnItemClickListener(
                            (parent, view1, position, id) -> {
                                if (!snooze.isEnabled()) {
                                    snooze.setEnabled(true);
                                }
                                String selection = (String) futureDepartures.getAdapter().getItem(position);
                                selectionValue.set(Integer.parseInt(selection.replace("'", "")));
                            });
                    if (departures.size() >= 4) {
                        ViewGroup.LayoutParams lp = futureDepartures.getLayoutParams();
                        lp.height = 400;
                        futureDepartures.setLayoutParams(lp);
                    }
                }
            }
        }


        // Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        return view;
    }

    @Override
    public void dismiss() {
        if (v!=null){
            v.cancel();
        }
        super.dismiss();
    }
}
