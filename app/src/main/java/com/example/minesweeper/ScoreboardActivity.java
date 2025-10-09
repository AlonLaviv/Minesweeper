package com.example.minesweeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import java.util.List;

public class ScoreboardActivity extends AppCompatActivity {

    private RecyclerView recyclerScores;
    private ScoreAdapter adapter;
    private ScoreDao scoreDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        recyclerScores = findViewById(R.id.recyclerScores);
        recyclerScores.setLayoutManager(new LinearLayoutManager(this));

        scoreDao = ScoreDatabase.getInstance(this).scoreDao();

        List<Score> scores = scoreDao.getAllScores();
        adapter = new ScoreAdapter(this, scores);
        recyclerScores.setAdapter(adapter);


    }

    private void showClearConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Clear All Scores");
        builder.setMessage("Are you sure you want to delete all scores?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                scoreDao.clearAll();
                List<Score> emptyList = scoreDao.getAllScores();
                adapter = new ScoreAdapter(ScoreboardActivity.this, emptyList);
                recyclerScores.setAdapter(adapter);
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }
}
