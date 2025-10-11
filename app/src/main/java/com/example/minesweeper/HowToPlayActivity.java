package com.example.minesweeper;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * HowToPlayActivity
 * -----------------
 * Displays simple instructions for how to play the game.
 * This activity is accessible only from the Main Menu.
 * Includes a single "Back" button to return to the main menu.
 */
public class HowToPlayActivity extends AppCompatActivity {

    private static final String TAG = "HowToPlayActivity"; // Log tag
    private Button btnBack; // Button to go back to main menu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);
        Log.d(TAG, "onCreate: HowToPlayActivity started");

        // Bind button from layout
        btnBack = (Button) findViewById(R.id.btnBack);

        // Set click listener for Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Back button clicked — returning to main menu");
                finish(); // Close this activity and return to MainActivity
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Activity visible");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Activity resumed");
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
