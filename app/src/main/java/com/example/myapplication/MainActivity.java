package com.example.myapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private Button pickDateBtn;
    private DatePicker datePicker;
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);



        pickDateBtn = findViewById(R.id.button);
        datePicker = findViewById(R.id.datePicker);
        textView = findViewById(R.id.textViewNumber);
    }

    public void OnButtonClick(View view)
    {
        var month = datePicker.getMonth();
        var year = datePicker.getYear();

        var calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year,month,1);

        var monthDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int mondays = 0;

        for(int day = 1; day <= monthDays; day++)
        {
            calendar.set(year, month, day);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if(dayOfWeek == Calendar.MONDAY)
            {
                mondays++;
            }
        }

        textView.setText(String.valueOf(mondays));
    }

}





