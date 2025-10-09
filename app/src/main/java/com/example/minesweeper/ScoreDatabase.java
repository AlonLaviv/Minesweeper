package com.example.minesweeper;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Score.class}, version = 1, exportSchema = false)
public abstract class ScoreDatabase extends RoomDatabase {

    private static ScoreDatabase instance;

    public abstract ScoreDao scoreDao();

    public static synchronized ScoreDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            ScoreDatabase.class, "score_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // ⚠ for simplicity (use background thread in production)
                    .build();
        }
        return instance;
    }
}
