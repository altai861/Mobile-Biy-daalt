package com.example.todoapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.todoapp.Model.TodoModel;
import com.example.todoapp.Utils.DatabaseHandler;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;

public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "ActionBottomDialog";

    private EditText newTaskText;
    private TextView datePickerText;
    private String selectedDate = "";
    private Button newTaskSaveButton;
    private DatabaseHandler db;

    public static AddNewTask newInstance() {
        return new AddNewTask();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_task, container, false);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newTaskText = getView().findViewById(R.id.newTaskText);
        newTaskSaveButton = getView().findViewById(R.id.newTaskButton);

        datePickerText = getView().findViewById(R.id.datePickerText);

        datePickerText.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view1, year1, month1, dayOfMonth) -> {
                        selectedDate = year1 + "-" + (month1 + 1) + "-" + dayOfMonth;
                        datePickerText.setText(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        db = new DatabaseHandler(getActivity());
        db.openDatabase();

        boolean isUpdate = false;
        final Bundle bundle = getArguments();

        if (bundle != null) {
            isUpdate = true;
            String task = bundle.getString("task");
            String date = bundle.getString("date");
            newTaskText.setText(task);
            selectedDate = date;  // Set the selectedDate to the task's date
            datePickerText.setText(date); // Set the text for the date field

            if (task.length() > 0) {
                newTaskSaveButton.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
            }
        }

        newTaskText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean isReady = !charSequence.toString().isEmpty() && datePickerText.getText().length() > 0;
                newTaskSaveButton.setEnabled(isReady);
                newTaskSaveButton.setTextColor(isReady ?
                        ContextCompat.getColor(getContext(), R.color.colorPrimaryDark) : Color.GRAY);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        boolean finalIsUpdate = isUpdate;
        newTaskSaveButton.setOnClickListener(v -> {
            String text = newTaskText.getText().toString();
            if (finalIsUpdate) {
                Log.d("date", selectedDate);
                db.updateTask(bundle.getInt("id"), text, selectedDate);
            } else {
                TodoModel task = new TodoModel();
                task.setTask(text);
                task.setStatus(0);
                task.setDate(datePickerText.getText().toString()); // Save the selected date
                db.insertTask(task);
            }
            dismiss();
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Activity activity = getActivity();
        if (activity instanceof DialogCloseListener) {
            ((DialogCloseListener)activity).handleDialogClose(dialog);
        }
    }

}
