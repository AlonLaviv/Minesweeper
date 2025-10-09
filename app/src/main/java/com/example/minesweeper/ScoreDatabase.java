package com.example.minesweeper;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Score.class}, version = 1)
public abstract class ScoreDatabase extends RoomDatabase {

    private static ScoreDatabase instance;

    public abstract ScoreDao scoreDao();

    public static synchronized ScoreDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            ScoreDatabase.class, "score_database")
                    .allowMainThreadQueries() // ✅ Needed for now
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
