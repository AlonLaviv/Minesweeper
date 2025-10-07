package com.example.minesweeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // tag for logcat
    private Button btnPlay;
    private Button btnScoreboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: MainActivity created");//log onCreate

        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnScoreboard = (Button) findViewById(R.id.btnScoreboard);

        // PLAY button
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Play button clicked");
                showDifficultyDialog();
            }
        });

        // SCOREBOARD button
        btnScoreboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Scoreboard button clicked");
                Intent intent = new Intent(MainActivity.this, ScoreboardActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showDifficultyDialog() {
        final String[] difficulties = {
                "Easy (8x8, 10 bombs)",
                "Medium (16x16, 40 bombs)",
                "Hard (24x24, 99 bombs)"
        };

        Log.d(TAG, "Opening difficulty selection dialog");

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Select Difficulty");

        builder.setItems(difficulties, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int rows = 0;
                int cols = 0;
                int bombs = 0;
                String difficulty = "";

                switch (which) {
                    case 0://easy
                        rows = 8; cols = 8; bombs = 10; difficulty = "Easy";
                        break;
                    case 1://medium
                        rows = 16; cols = 16; bombs = 40; difficulty = "Medium";
                        break;
                    case 2://hard
                        rows = 24; cols = 24; bombs = 99; difficulty = "Hard";
                        break;
                }

                Log.d(TAG, "Difficulty chosen: " + difficulty +
                        " (" + rows + "x" + cols + ", " + bombs + " bombs)");

                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putExtra("rows", rows);
                intent.putExtra("cols", cols);
                intent.putExtra("bombs", bombs);
                intent.putExtra("difficulty", difficulty);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "Difficulty selection canceled");
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Activity visible");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Activity active");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Activity paused");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Activity stopped");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Activity destroyed");
    }
}
