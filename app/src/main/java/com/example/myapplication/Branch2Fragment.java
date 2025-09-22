package com.example.myapplication;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Branch2Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Branch2Fragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private EditText etSchedule;
    private Spinner spStartDay, spEndDay;
    private TimePicker tpStartTime, tpEndTime;
    private Button btnShow;
    private TextView tvResult;

    // Русские дни недели от понедельника (0) до воскресенья (6)
    private static final String[] DAY_NAMES = {
            "понедельник", "вторник", "среда",
            "четверг",     "пятница",  "суббота",
            "воскресенье"
    };

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Branch2Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Branch2Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Branch2Fragment newInstance(String param1, String param2) {
        Branch2Fragment fragment = new Branch2Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_branch2, container, false);

        etSchedule  = view.findViewById(R.id.etSchedule);
        spStartDay  = view.findViewById(R.id.spStartDay);
        spEndDay    = view.findViewById(R.id.spEndDay);
        tpStartTime = view.findViewById(R.id.tpStartTime);
        tpEndTime   = view.findViewById(R.id.tpEndTime);
        btnShow     = view.findViewById(R.id.btnShow);
        tvResult    = view.findViewById(R.id.tvResult);

        setupSpinners(view); // передаём view
        tpStartTime.setIs24HourView(true);
        tpEndTime.setIs24HourView(true);

        btnShow.setOnClickListener(v -> onShowClicked());

        return view;
    }


    private void setupSpinners(View view) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                DAY_NAMES
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStartDay.setAdapter(adapter);
        spEndDay.setAdapter(adapter);
    }


    private void onShowClicked() {
        String raw = etSchedule.getText().toString().trim();
        if (TextUtils.isEmpty(raw)) {
            Toast.makeText(requireContext(), "Введите расписание кружка", Toast.LENGTH_SHORT).show();

            return;
        }

        List<Scheduled> allMeetings;
        try {
            allMeetings = parseSchedule(raw);
        } catch (IllegalArgumentException ex) {
            Toast.makeText(requireContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        // Преобразуем начало/конец интервала в «минуты от Пн 00:00»
        int startIdx = computeIndex(
                spStartDay.getSelectedItemPosition(),
                tpStartTime.getHour(),
                tpStartTime.getMinute()
        );
        int endIdx = computeIndex(
                spEndDay.getSelectedItemPosition(),
                tpEndTime.getHour(),
                tpEndTime.getMinute()
        );

        // Фильтруем и сортируем
        List<Scheduled> inRange = new ArrayList<>();
        for (Scheduled s : allMeetings) {
            int idx = computeIndex(s.dayIndex, s.hour, s.minute);
            if (idx >= startIdx && idx <= endIdx) {
                inRange.add(s);
            }
        }
        Collections.sort(inRange, (a, b) ->
                Integer.compare(
                        computeIndex(a.dayIndex, a.hour, a.minute),
                        computeIndex(b.dayIndex, b.hour, b.minute)
                )
        );

        // Выводим результат
        if (inRange.isEmpty()) {
            tvResult.setText("В выбранном интервале нет занятий");
        } else {
            StringBuilder sb = new StringBuilder();
            for (Scheduled s : inRange) {
                String day = DAY_NAMES[s.dayIndex];
                day = day.substring(0, 1).toUpperCase() + day.substring(1);
                sb.append(day)
                        .append(", ")
                        .append(String.format(Locale.getDefault(),
                                "%02d:%02d", s.hour, s.minute))
                        .append(";\n");
            }
            // удаляем заключительный «;\n»
            tvResult.setText(sb.substring(0, sb.length() - 2));
        }
    }

    /**
     * Парсит входную строку вида
     *   "вторник,15:30; среда,20:30; пятница,17:00"
     * и возвращает список Scheduled {dayIndex, hour, minute}.
     */
    private List<Scheduled> parseSchedule(String raw) {
        String[] items = raw.split(";");
        List<Scheduled> list = new ArrayList<>(items.length);

        for (String item : items) {
            item = item.trim();
            if (item.isEmpty()) continue;

            String[] parts = item.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException(
                        "Неверный формат элемента: «" + item +
                                "». Ожидается «день,HH:mm»."
                );
            }

            String dayStr = parts[0].trim().toLowerCase(Locale.getDefault());
            int dayIdx = indexOfDay(dayStr);
            if (dayIdx < 0) {
                throw new IllegalArgumentException(
                        "Неизвестный день недели: «" + parts[0].trim() + "»."
                );
            }

            String[] hm = parts[1].trim().split(":");
            if (hm.length != 2) {
                throw new IllegalArgumentException(
                        "Неверное время: «" + parts[1].trim() + "». Ожидается HH:mm."
                );
            }
            int h = Integer.parseInt(hm[0]);
            int m = Integer.parseInt(hm[1]);
            if (h < 0 || h > 23 || m < 0 || m > 59) {
                throw new IllegalArgumentException(
                        "Часы или минуты вне диапазона: " + parts[1].trim()
                );
            }

            list.add(new Scheduled(dayIdx, h, m));
        }

        return list;
    }

    private int indexOfDay(String name) {
        for (int i = 0; i < DAY_NAMES.length; i++) {
            if (DAY_NAMES[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /** Минуты от Пн 00:00 для упрощённого сравнения внутри недели. */
    private int computeIndex(int dayIndex, int hour, int minute) {
        return dayIndex * 24 * 60 + hour * 60 + minute;
    }

    /** Модель одной встречи: день недели + время */
    private static class Scheduled {
        final int dayIndex;  // 0=понедельник … 6=воскресенье
        final int hour;
        final int minute;

        Scheduled(int dayIndex, int hour, int minute) {
            this.dayIndex = dayIndex;
            this.hour     = hour;
            this.minute   = minute;
        }
    }
}