package com.example.todoapp;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.Adapter.TodoAdapter;
import com.example.todoapp.Model.TodoModel;
import com.example.todoapp.Utils.DatabaseHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements DialogCloseListener {

    private RecyclerView tasksRecyclerView;
    private TodoAdapter tasksAdapter;
    private FloatingActionButton fab;

    private List<TodoModel> taskList;
    private DatabaseHandler db;

    private TextView todaysDateText;
    private Button selectDateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Objects.requireNonNull(getSupportActionBar()).hide();
        todaysDateText = findViewById(R.id.todaysDate);
        selectDateButton = findViewById(R.id.selectDateButton);
        Button todayButton = findViewById(R.id.todayButton); // Reference to the "Today" button

        db = new DatabaseHandler(this);
        db.openDatabase();
        taskList = new ArrayList<>();

        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        fab = findViewById(R.id.fab);

        String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        todaysDateText.setText("Today: " + todayDate);

        tasksAdapter = new TodoAdapter(db, this);
        tasksRecyclerView.setAdapter(tasksAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerItemTouchHelper(tasksAdapter));
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);

        showTasksForToday();
        // DatePicker button listener
        selectDateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Open DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                    (view, year1, month1, dayOfMonth) -> {
                        String selectedDate = year1 + "-" + (month1 + 1) + "-" + dayOfMonth;
                        // Update the TextView to show the selected date
                        todaysDateText.setText(selectedDate);
                        // Show tasks for the selected date
                        showTasksForDate(selectedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // "Today" button listener
        todayButton.setOnClickListener(v -> {
            showTasksForToday();
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG);
            }
        });
    }

    @Override
    public void handleDialogClose(DialogInterface dialog) {
        showTasksForDate(todaysDateText.getText().toString());
        tasksAdapter.notifyDataSetChanged();
    }

    private void showTasksForToday() {
        // Get today's date in the format yyyy-MM-dd
        String todayDate = getFormattedTodayDate();
        Log.d("MAIN", "Today date: " + todayDate);
        todaysDateText.setText(todayDate);
        // Fetch tasks for today from the database
        List<TodoModel> tasksForToday = db.getTasksForDate(todayDate);

        // Check if tasks were fetched correctly
        Log.d("MAIN", "Fetched tasks for today: " + tasksForToday.size());

        if (tasksForToday != null && !tasksForToday.isEmpty()) {
            // Update the adapter with tasks for today
            tasksAdapter.setTasks(tasksForToday);
        } else {
            // If no tasks are found, set an empty list
            tasksAdapter.setTasks(new ArrayList<>());
        }

        // Notify the adapter to refresh the RecyclerView
        tasksAdapter.notifyDataSetChanged();
    }

    private void showTasksForDate(String date) {
        // Fetch tasks for the selected date
        List<TodoModel> tasksForDate = db.getTasksForDate(date);
        tasksAdapter.setTasks(tasksForDate);  // Update the RecyclerView with tasks for the selected date
    }

    // Function to get today's date in the format yyyy-MM-d (without leading zero for day)
    private String getFormattedTodayDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Month is 0-based, so add 1
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Return formatted date as yyyy-MM-d (without leading zero for day)
        return year + "-" + month + "-" + day;
    }
}