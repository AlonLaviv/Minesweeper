package com.example.minesweeper;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * ScoreDatabase
 * --------------
 * This class defines the Room database for storing player scores.
 *
 * It uses a singleton pattern to ensure only one instance of the database
 * exists throughout the entire app.
 *
 * The database includes one entity: {Score}.
 * Access to the database is done through the {ScoreDao}.
 */
@Database(entities = {Score.class}, version = 1)
public abstract class ScoreDatabase extends RoomDatabase {

    private static final String TAG = "ScoreDatabase";

    /** The single static instance of the database (singleton). */
    private static ScoreDatabase instance;

    /**
     * Abstract method to access the DAO (Data Access Object).
     * Room automatically generates the implementation.
     *
     * @return A ScoreDao used for querying or inserting scores.
     */
    public abstract ScoreDao scoreDao();

    /**
     * Returns a singleton instance of the database.
     *
     * Uses synchronized locking to prevent multiple threads from
     * creating separate instances at the same time.
     *
     * @param context The application context (not an Activity context).
     * @return The single ScoreDatabase instance.
     */
    public static synchronized ScoreDatabase getInstance(Context context) {
        if (instance == null) {
            Log.d(TAG, "Database not yet initialized. Creating new instance...");

            // Build the Room database
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            ScoreDatabase.class,
                            "score_database"
                    )
                    // Allows database operations on the main thread.
                    // Normally discouraged, but acceptable for small apps or prototypes.
                    .allowMainThreadQueries()

                    // Build the database
                    .build();

            Log.d(TAG, "ScoreDatabase instance created successfully.");
        } else {
            Log.d(TAG, "Returning existing ScoreDatabase instance.");
        }

        return instance;
    }
}
