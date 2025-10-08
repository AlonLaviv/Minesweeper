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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        rows = getIntent().getIntExtra("rows", 8);
        cols = getIntent().getIntExtra("cols", 8);
        bombs = getIntent().getIntExtra("bombs", 10);
        flagsLeft = bombs;

        gameGrid = (GridLayout) findViewById(R.id.gameGrid);
        tvFlags = (TextView) findViewById(R.id.tvFlags);
        tvTimer = (TextView) findViewById(R.id.tvTimer);
        btnPause = (Button) findViewById(R.id.btnPause);

        gameGrid.setColumnCount(cols);
        gameGrid.setRowCount(rows);

        tvFlags.setText("Flags: " + flagsLeft);
        tvTimer.setText("Time: 0");

        game = new MinesweeperGame(rows, cols, bombs);

        ViewTreeObserver vto = gameGrid.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
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

                // Default style
                cellButton.setBackgroundResource(R.drawable.cell_background);

                // Handle click (reveal)
                cellButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (gameOver || isPaused) return;
                        handleCellClick(r, c);
                    }
                });

                // Handle long press (flag)
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
            showGameOverDialog(false);
        } else if (game.checkWin()) {
            gameOver = true;
            stopTimer();
            showGameOverDialog(true);
        }
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
                    }
                } else if (cell.isFlagged()) {
                    btn.setText("🚩");
                } else {
                    btn.setText("");
                    btn.setBackgroundResource(R.drawable.cell_background);
                }
            }
        }
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

        builder.setNegativeButton("Return to Main Menu", new DialogInterface.OnClickListener() {
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

    private void showGameOverDialog(boolean win) {
        String title = win ? "You Win!" : "Game Over!";
        String message = win ? "Congratulations, you cleared the board!" : "You hit a bomb!";

        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);

        builder.setPositiveButton("Main Menu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        builder.show();
    }
}
