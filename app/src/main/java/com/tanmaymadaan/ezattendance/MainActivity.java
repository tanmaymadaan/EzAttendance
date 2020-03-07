package com.tanmaymadaan.ezattendance;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private String UID;
    private int goal = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart(){
        super.onStart();

        UID = FirebaseAuth.getInstance().getCurrentUser().getUid();

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
                }
            });

            alert.show();


        //}
    }
}
