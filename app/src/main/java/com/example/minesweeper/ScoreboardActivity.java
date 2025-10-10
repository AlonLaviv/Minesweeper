package com.example.minesweeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.List;

public class ScoreboardActivity extends AppCompatActivity {

    private RecyclerView recyclerScores;
    private ScoreAdapter scoreAdapter;
    private ScoreDao scoreDao;
    private Spinner spinnerDifficulty;
    private Button btnReturnMain;

    private String selectedDifficulty = "Easy"; // default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        // Initialize views
        recyclerScores = (RecyclerView) findViewById(R.id.recyclerScores);
        spinnerDifficulty = (Spinner) findViewById(R.id.spinnerDifficulty);
        btnReturnMain = (Button) findViewById(R.id.btnReturnMain);

        // Initialize database
        scoreDao = ScoreDatabase.getInstance(this).scoreDao();

        // Spinner setup
        final String[] difficulties = {"Easy", "Medium", "Hard"};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_selected_item,
                difficulties
        );
        adapter.setDropDownViewResource(R.layout.spinner_selected_item);
        spinnerDifficulty.setAdapter(adapter);

        // Get difficulty passed from GameActivity or MainActivity
        Intent intent = getIntent();
        String passedDifficulty = intent.getStringExtra("difficulty");
        if (passedDifficulty != null) {
            selectedDifficulty = passedDifficulty;
        }

        // Set correct spinner selection
        int spinnerPos = adapter.getPosition(selectedDifficulty);
        if (spinnerPos >= 0) {
            spinnerDifficulty.setSelection(spinnerPos);
        }

        // RecyclerView setup
        recyclerScores.setLayoutManager(new LinearLayoutManager(this));

        // Spinner listener
        spinnerDifficulty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDifficulty = parent.getItemAtPosition(position).toString();
                loadScores(selectedDifficulty);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Return to main menu button
        btnReturnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(ScoreboardActivity.this, MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);
                finish();
            }
        });

        // Initial load
        loadScores(selectedDifficulty);
    }

    /** ✅ Refresh scores when returning to activity */
    @Override
    protected void onResume() {
        super.onResume();
        loadScores(selectedDifficulty);
    }

    private void loadScores(String difficulty) {
        List<Score> scores = scoreDao.getScoresByDifficulty(difficulty);
        android.util.Log.d("ScoreboardActivity", "Loaded " + scores.size() + " scores for " + difficulty);
        scoreAdapter = new ScoreAdapter(ScoreboardActivity.this, scores);
        recyclerScores.setAdapter(scoreAdapter);
    }
}
