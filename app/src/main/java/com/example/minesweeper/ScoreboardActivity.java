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

        recyclerScores = findViewById(R.id.recyclerScores);
        Spinner spinnerDifficulty = findViewById(R.id.spinnerDifficulty);

        String[] difficulties = {"Easy", "Medium", "Hard"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_selected_item,   // layout for selected item
                difficulties
        );

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item); // layout for dropdown items

        spinnerDifficulty.setAdapter(adapter);


        btnReturnMain = findViewById(R.id.btnReturnMain);

        recyclerScores.setLayoutManager(new LinearLayoutManager(this));
        scoreDao = ScoreDatabase.getInstance(this).scoreDao();


        // If coming from MainActivity or GameActivity, get difficulty
        Intent intent = getIntent();
        String incomingDiff = intent.getStringExtra("difficulty");
        if (incomingDiff != null) {
            selectedDifficulty = incomingDiff;
        }

        // Set Spinner to correct difficulty
        int spinnerPos = adapter.getPosition(selectedDifficulty);
        spinnerDifficulty.setSelection(spinnerPos >= 0 ? spinnerPos : 0);

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
                Intent intent = new Intent(ScoreboardActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        // Load scores initially
        loadScores(selectedDifficulty);
    }

    /** ✅ Automatically refresh scores when activity resumes */
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
