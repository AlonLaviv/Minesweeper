package com.example.minesweeper;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ScoreDao {
    @Insert
    void insert(Score score);

    @Query("SELECT * FROM scores WHERE difficulty = :difficulty ORDER BY time ASC")
    List<Score> getScoresByDifficulty(String difficulty);
}
