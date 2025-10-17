package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Branch3Fragment extends Fragment {

    private Calendar selectedCalendar;

    public Branch3Fragment() {
        // Required empty public constructor
    }

    public static Branch3Fragment newInstance(String param1, String param2) {
        Branch3Fragment fragment = new Branch3Fragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_branch3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DatePicker datePicker = view.findViewById(R.id.datePicker);
        TextView textViewResult = view.findViewById(R.id.textViewResult);

        datePicker.init(
                datePicker.getYear(),
                datePicker.getMonth(),
                datePicker.getDayOfMonth(),
                (view1, year, monthOfYear, dayOfMonth) -> {
                    selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(Calendar.YEAR, year);
                    selectedCalendar.set(Calendar.MONTH, monthOfYear);

                    int daysInMonth = selectedCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                    StringBuilder saturdays = new StringBuilder();

                    for (int day = 1; day <= daysInMonth; day++) {
                        selectedCalendar.set(Calendar.DAY_OF_MONTH, day);
                        int dayOfWeek = selectedCalendar.get(Calendar.DAY_OF_WEEK);
                        if (dayOfWeek == Calendar.SATURDAY) {
                            String formattedDate = sdf.format(selectedCalendar.getTime());
                            saturdays.append(formattedDate).append("\n");
                        }
                    }

                    textViewResult.setText("Субботы в " + (monthOfYear + 1) + "/" + year + ":\n" + saturdays.toString());
                }
        );
    }
}
