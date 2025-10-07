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
    private int elapsedTime = 0;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Log.d(TAG, "onCreate: Game started");

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

        // Create grid after layout is measured
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
        Log.d(TAG, "Creating grid " + rows + "x" + cols);
        gameGrid.removeAllViews();

        int gridWidth = gameGrid.getWidth();
        int gridHeight = gameGrid.getHeight();

        int cellSize = Math.min(gridWidth / cols, gridHeight / rows);
        int usedWidth = cellSize * cols;
        int usedHeight = cellSize * rows;

        // Use generic LayoutParams instead of GridLayout.LayoutParams
        android.view.ViewGroup.LayoutParams gridParams = gameGrid.getLayoutParams();
        gridParams.width = usedWidth;
        gridParams.height = usedHeight;
        gameGrid.setLayoutParams(gridParams);

        gameGrid.setColumnCount(cols);
        gameGrid.setRowCount(rows);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Button cell = new Button(this);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                params.rowSpec = GridLayout.spec(r);
                params.columnSpec = GridLayout.spec(c);
                params.setMargins(0, 0, 0, 0);

                cell.setLayoutParams(params);
                cell.setPadding(0, 0, 0, 0);
                cell.setBackgroundResource(R.drawable.cell_background);

                gameGrid.addView(cell);
            }
        }

        Log.d(TAG, "Grid created with cell size = " + cellSize + "px");
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
}
