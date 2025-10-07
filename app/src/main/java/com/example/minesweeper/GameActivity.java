package com.example.minesweeper;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = "GameActivity";//tag for logcat
    private int rows;
    private int cols;
    private int bombs;
    private String difficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Log.d(TAG, "onCreate: GameActivity started");

        rows = getIntent().getIntExtra("rows", 8);
        cols = getIntent().getIntExtra("cols", 8);
        bombs = getIntent().getIntExtra("bombs", 10);
        difficulty = getIntent().getStringExtra("difficulty");

        Log.d(TAG, "Received Intent data: rows=" + rows + ", cols=" + cols + ", bombs=" + bombs + ", difficulty=" + difficulty);

        // temporary test text
        TextView info = new TextView(this);
        info.setTextSize(20);
        info.setText("Difficulty: " + difficulty + "\nGrid: " + rows + "x" + cols + "\nBombs: " + bombs);
        setContentView(info);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Game paused");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Game stopped");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Game destroyed");
    }
}
