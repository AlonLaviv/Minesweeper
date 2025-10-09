package com.example.minesweeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.content.Context;

import java.text.DateFormat;
import java.util.Date;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = "GameActivity";

    private GridLayout gameGrid;
    private TextView tvFlags, tvTimer;
    private Button btnPause;

    private int rows, cols, bombs;
    private int flagsLeft;
    private boolean isPaused = false;
    private boolean gameOver = false;

    private MinesweeperGame game;

    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private int elapsedTime = 0;

    private Button[][] cellButtons;

    // Room database DAO
    private ScoreDao scoreDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        rows = getIntent().getIntExtra("rows", 8);
        cols = getIntent().getIntExtra("cols", 8);
        bombs = getIntent().getIntExtra("bombs", 10);
        flagsLeft = bombs;

        gameGrid = findViewById(R.id.gameGrid);
        tvFlags = findViewById(R.id.tvFlags);
        tvTimer = findViewById(R.id.tvTimer);
        btnPause = findViewById(R.id.btnPause);

        gameGrid.setColumnCount(cols);
        gameGrid.setRowCount(rows);

        tvFlags.setText("Flags: " + flagsLeft);
        tvTimer.setText("Time: 0");

        game = new MinesweeperGame(rows, cols, bombs);
        scoreDao = ScoreDatabase.getInstance(this).scoreDao();

        gameGrid.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                gameGrid.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                createGrid();
            }
        });

        startTimer();

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPauseMenu();
            }
        });
    }

    private void createGrid() {
        gameGrid.removeAllViews();
        cellButtons = new Button[rows][cols];

        int gridWidth = gameGrid.getWidth();
        int gridHeight = gameGrid.getHeight();
        int cellSize = Math.min(gridWidth / cols, gridHeight / rows);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                final int r = row;
                final int c = col;

                Button cellButton = new Button(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                params.rowSpec = GridLayout.spec(row);
                params.columnSpec = GridLayout.spec(col);
                params.setMargins(0, 0, 0, 0);
                cellButton.setLayoutParams(params);
                cellButton.setPadding(0, 0, 0, 0);

                cellButton.setBackgroundResource(R.drawable.cell_background);

                // Reveal click
                cellButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (gameOver || isPaused) return;
                        handleCellClick(r, c);
                    }
                });

                // Long press to flag
                cellButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (gameOver || isPaused) return true;
                        handleCellFlag(r, c);
                        return true;
                    }
                });

                gameGrid.addView(cellButton);
                cellButtons[row][col] = cellButton;
            }
        }
    }

    private void handleCellClick(int row, int col) {
        boolean safe = game.revealCell(row, col);
        updateGrid();

        if (!safe) {
            gameOver = true;
            stopTimer();
            revealAllCells();
            showEndGameDialog(false);
        } else if (game.checkWin()) {
            gameOver = true;
            stopTimer();
            saveScoreToDatabase(); // ✅ Save score
            showEndGameDialog(true);
        }
    }

    /** ✅ Save the score to Room database when you win */
    private void saveScoreToDatabase() {
        String difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty == null) difficulty = "Easy"; // fallback
        String date = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());

        Score score = new Score(difficulty, elapsedTime, date);
        scoreDao.insert(score);
    }

    private void handleCellFlag(int row, int col) {
        Cell cell = game.getCell(row, col);
        if (cell.isRevealed()) return;

        if (cell.isFlagged()) {
            cell.setFlagged(false);
            flagsLeft++;
        } else if (flagsLeft > 0) {
            cell.setFlagged(true);
            flagsLeft--;
        }

        tvFlags.setText("Flags: " + flagsLeft);
        updateGrid();
    }

    private void updateGrid() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Cell cell = game.getCell(row, col);
                Button btn = cellButtons[row][col];

                if (cell.isRevealed()) {
                    btn.setEnabled(false);

                    if (cell.isBomb()) {
                        btn.setText("💣");
                        btn.setBackgroundColor(0xFFFF4444); // red
                    } else {
                        int n = cell.getNeighborBombs();
                        btn.setText(n == 0 ? "" : String.valueOf(n));
                        btn.setBackgroundColor(0xFFDDDDDD);

                        // 🎨 Color the numbers
                        switch (n) {
                            case 1: btn.setTextColor(0xFF0000FF); break; // blue
                            case 2: btn.setTextColor(0xFF008000); break; // green
                            case 3: btn.setTextColor(0xFFFF0000); break; // red
                            case 4: btn.setTextColor(0xFF800080); break; // purple
                            case 5: btn.setTextColor(0xFF8B0000); break; // dark red
                            case 6: btn.setTextColor(0xFF00FFFF); break; // cyan
                            case 7: btn.setTextColor(0xFF000000); break; // black
                            case 8: btn.setTextColor(0xFF555555); break; // dark gray
                            default: btn.setTextColor(0xFF000000); break; // default black
                        }
                    }

                } else if (cell.isFlagged()) {
                    btn.setText("🚩");
                    btn.setTextColor(0xFF000000);
                } else {
                    btn.setText("");
                    btn.setBackgroundResource(R.drawable.cell_background);
                }
            }
        }
    }

    /** Reveal the entire board when the game is lost */
    private void revealAllCells() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Cell cell = game.getCell(row, col);
                cell.setRevealed(true);
            }
        }
        updateGrid();
    }

    private void startTimer() {
        isPaused = false;
        elapsedTime = 0;

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPaused) {
                    elapsedTime++;
                    tvTimer.setText("Time: " + elapsedTime);
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };

        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void stopTimer() {
        isPaused = true;
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void showPauseMenu() {
        isPaused = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle("Game Paused");

        builder.setPositiveButton("Return to Game", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isPaused = false;
                timerHandler.postDelayed(timerRunnable, 1000);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Main Menu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopTimer();
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        builder.setCancelable(false);
        builder.show();
    }

    /** ✅ Unified Win/Lose dialog */
    private void showEndGameDialog(final boolean win) {
        vibrate(win); // 🔔 feedback
        String title = win ? "🎉 You Win!" : "💣 Game Over";
        String message = win
                ? "You cleared the board in " + elapsedTime + " seconds!"
                : "You hit a bomb after " + elapsedTime + " seconds.";

        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);

        builder.setPositiveButton("Play Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        builder.setNeutralButton("Main Menu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        builder.setNegativeButton("View Scoreboard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(GameActivity.this, ScoreboardActivity.class);
                startActivity(intent);
            }
        });

        builder.show();
    }
    /** 🔔 Vibrate feedback on win or loss */
    private void vibrate(boolean win) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) return;

        // Use modern VibrationEffect if supported
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (win) {
                vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                // three short pulses for loss
                long[] pattern = {0, 150, 100, 150, 100, 150};
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            }
        } else {
            // Legacy support for pre-Android O
            if (win) {
                vibrator.vibrate(150);
            } else {
                vibrator.vibrate(new long[]{0, 150, 100, 150, 100, 150}, -1);
            }
        }
    }

}
