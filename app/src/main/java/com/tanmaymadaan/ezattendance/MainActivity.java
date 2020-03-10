package com.tanmaymadaan.ezattendance;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tanmaymadaan.ezattendance.Models.UsersSubject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private int goal = 0;
    private Button addSubject;
    private FirebaseFirestore db;
    private String UID;
    private ArrayList<UsersSubject> subjectArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addSubject = findViewById(R.id.main_add_subject);
        addSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "addSubject");
                openAddSubjectDialog();
            }
        });

        UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        if(FirebaseAuth.getInstance().getCurrentUser() == null)
        {
            Intent intent = new Intent(MainActivity.this,StartActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        final SharedPreferences preferences = getSharedPreferences("first-time", Context.MODE_PRIVATE);
        boolean bool = preferences.getBoolean("first-timer", false);
        //if(bool){
            preferences.edit().putBoolean("first-timer", false).apply();
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            final View dialog = getLayoutInflater().inflate(R.layout.set_goal, null);
            final TextView goalPercText = dialog.findViewById(R.id.goalPercText);
            SeekBar seekbarGoal = dialog.findViewById(R.id.seekbarGoal);
            Button button = dialog.findViewById(R.id.setGoalBtn);
            builder.setView(dialog);
            builder.setCancelable(false);
            final AlertDialog alert = builder.create();
            final HashMap<String, Object> map = new HashMap<>();
            map.put("overall", 0.0);
            seekbarGoal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                   goalPercText.setText(String.valueOf(progress)+"%");
                    map.put("goal", (double)progress);
                    goal = progress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("goal",String.valueOf(goal));
                    alert.dismiss();
                }
            });

            alert.show();

        //}
    }

    private void openAddSubjectDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.add_subject_layout, null);
        final EditText editText = view.findViewById(R.id.dialog_add_subject);
        Button ok, cancel;
        ok = view.findViewById(R.id.addSubjectSubmit);
        cancel = view.findViewById(R.id.cancelSubject);
        builder.setView(view);
        final AlertDialog alert = builder.create();
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                addSubject(editText.getText().toString().trim());
            }
        });
        alert.show();
    }

    private void addSubject(String subject) {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("total", 0);
        map.put("present", 0);
        db.collection(UID).document(subject).set(map);

        UsersSubject usersSubject = new UsersSubject();
        usersSubject.setSubjectname(subject);
        usersSubject.setTotal(0);
        usersSubject.setGoal(goal);
        usersSubject.setPresent(0);
        usersSubject.setPercentage(0);
        usersSubject.setStatus("You are right on track");
        subjectArrayList.add(usersSubject);
    }
}
