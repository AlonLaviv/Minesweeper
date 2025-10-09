package com.example.minesweeper;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "scores")
public class Score {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String difficulty;
    private int time;
    private String date;

    public Score(String difficulty, int time, String date) {
        this.difficulty = difficulty;
        this.time = time;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public int getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }
}
