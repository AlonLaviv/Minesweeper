package com.example.minesweeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.List;

/**
 * ScoreboardActivity
 * -------------------
 * Displays a list of saved game scores using a RecyclerView.
 * The player can filter scores by difficulty (Easy / Medium / Hard)
 * using a Spinner dropdown, and return to the Main Menu using a button.
 *
 * This class interacts with the Room database (ScoreDatabase via ScoreDao)
 * to retrieve and display data.
 */
public class ScoreboardActivity extends AppCompatActivity {

    private static final String TAG = "ScoreboardActivity";

    /** UI components */
    private RecyclerView recyclerScores;
    private Spinner spinnerDifficulty;
    private Button btnReturnMain;

    /** Adapter and data access */
    private ScoreAdapter scoreAdapter;
    private ScoreDao scoreDao;

    /** Currently selected difficulty (default: Easy) */
    private String selectedDifficulty = "Easy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);
        Log.d(TAG, "onCreate: ScoreboardActivity created");

        // --- Initialize Views ---
        recyclerScores = (RecyclerView) findViewById(R.id.recyclerScores);
        spinnerDifficulty = (Spinner) findViewById(R.id.spinnerDifficulty);
        btnReturnMain = (Button) findViewById(R.id.btnReturnMain);

        // --- Initialize Database ---
        scoreDao = ScoreDatabase.getInstance(this).scoreDao();
        Log.d(TAG, "Database instance and DAO initialized");

        // --- Spinner Setup ---
        final String[] difficulties = {"Easy", "Medium", "Hard"};
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.spinner_selected_item,
                difficulties
        );
        adapter.setDropDownViewResource(R.layout.spinner_selected_item);
        spinnerDifficulty.setAdapter(adapter);
        Log.d(TAG, "Spinner adapter set with difficulty levels");

        // --- Get difficulty passed from GameActivity or MainActivity ---
        Intent intent = getIntent();
        String passedDifficulty = intent.getStringExtra("difficulty");
        if (passedDifficulty != null) {
            selectedDifficulty = passedDifficulty;
            Log.d(TAG, "Received difficulty from intent: " + passedDifficulty);
        }

        // --- Set spinner to correct position ---
        int spinnerPos = adapter.getPosition(selectedDifficulty);
        if (spinnerPos >= 0) {
            spinnerDifficulty.setSelection(spinnerPos);
            Log.d(TAG, "Spinner set to position " + spinnerPos + " (" + selectedDifficulty + ")");
        }

        // --- RecyclerView setup ---
        recyclerScores.setLayoutManager(new LinearLayoutManager(this));
        Log.d(TAG, "RecyclerView layout manager set");

        // --- Spinner listener for difficulty selection ---
        spinnerDifficulty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDifficulty = parent.getItemAtPosition(position).toString();
                Log.d(TAG, "Spinner changed: selected difficulty = " + selectedDifficulty);
                loadScores(selectedDifficulty);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "No difficulty selected in spinner");
            }
        });

        // --- Return to main menu button ---
        btnReturnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Return to main menu button clicked");
                Intent mainIntent = new Intent(ScoreboardActivity.this, MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);
                finish();
            }
        });

        // --- Initial load of scores ---
        loadScores(selectedDifficulty);
    }

    /**
     * Called when activity resumes — ensures scoreboard is updated
     * if new scores were added.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: refreshing scores for " + selectedDifficulty);
        loadScores(selectedDifficulty);
    }

    /**
     * Loads scores from the database by difficulty and updates the RecyclerView.
     *
     * @param difficulty The difficulty level to filter (Easy, Medium, Hard)
     */
    private void loadScores(String difficulty) {
        List<Score> scores = scoreDao.getScoresByDifficulty(difficulty);
        Log.d(TAG, "loadScores: Retrieved " + scores.size() + " scores for difficulty: " + difficulty);

        scoreAdapter = new ScoreAdapter(ScoreboardActivity.this, scores);
        recyclerScores.setAdapter(scoreAdapter);
        Log.d(TAG, "RecyclerView adapter updated");
    }
}
