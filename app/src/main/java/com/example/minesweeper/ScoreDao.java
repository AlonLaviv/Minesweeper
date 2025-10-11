package com.example.minesweeper;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * ScoreDao
 * --------
 * Data Access Object (DAO) for the Room database.
 * This interface defines how the app interacts with the "scores" table.
 *
 * It allows inserting new game results and retrieving scores
 * filtered by difficulty level.
 *
 * The @Dao annotation tells Room to automatically generate
 * the necessary SQL code behind the scenes.
 */
@Dao
public interface ScoreDao {

    /**
     * Inserts a single score record into the database.
     *
     * @param score The Score object to insert.
     *
     * Example usage:
     *   Score score = new Score("Easy", 35, "2025-10-08 14:23");
     *   scoreDao.insert(score);
     */
    @Insert
    void insert(Score score);

    /**
     * Retrieves all scores that match the given difficulty.
     * Results are sorted in ascending order by completion time (best scores first).
     *
     * @param difficulty The difficulty level (e.g. "Easy", "Medium", or "Hard").
     * @return A list of Score objects for the specified difficulty.
     *
     * Example usage:
     *   List<Score> easyScores = scoreDao.getScoresByDifficulty("Easy");
     */
    @Query("SELECT * FROM scores WHERE difficulty = :difficulty ORDER BY time ASC")
    List<Score> getScoresByDifficulty(String difficulty);
}
