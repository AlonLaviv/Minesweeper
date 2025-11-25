package com.example.minesweeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
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
import android.widget.Toast;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import java.text.DateFormat;
import java.util.Date;

/**
 * GameActivity manages the main Minesweeper gameplay screen.
 * It handles:
 * - Timer
 * - Grid creation
 * - Click & flag logic
 * - Win/Loss detection
 * - Database saving of scores
 */
public class GameActivity extends AppCompatActivity {

    private static final String TAG = "GameActivity";

    //Gemini API stuff
    private String GeminiMassage;
    private Client client;
    private static final String MODEL = "gemini-2.5-flash";
    private static final String FIXED_PROMPT =
            "give me a very short congrats sentence for wining in a game " +
                    "of minesweeper use different results each time. " +
                    "show only one without any syntax" +
                    "the end of the sentence should end with (congrats emoji) You Won!" +
                    "make sure the sentence makes sense";


    // UI Components
    private GridLayout gameGrid;
    private TextView tvFlags, tvTimer;
    private Button btnPause;

    // Game variables
    private int rows, cols, bombs;
    private int flagsLeft;
    private boolean isPaused = false;
    private boolean gameOver = false;
    private boolean boardRevealed = false;

    private MinesweeperGame game;

    // Timer management
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private int elapsedTime = 0;

    // Cell buttons for grid
    private Button[][] cellButtons;

    // Room database access
    private ScoreDao scoreDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Log.d(TAG, "onCreate: GameActivity started");
        //API Gemini stuff
        String apiKey = BuildConfig.GOOGLE_API_KEY;
        client = Client.builder().apiKey(apiKey).build();
        generateMessage();

        // Get difficulty data from intent
        rows = getIntent().getIntExtra("rows", 8);
        cols = getIntent().getIntExtra("cols", 8);
        bombs = getIntent().getIntExtra("bombs", 10);
        flagsLeft = bombs;

        // Bind UI elements
        gameGrid = findViewById(R.id.gameGrid);
        tvFlags = findViewById(R.id.tvFlags);
        tvTimer = findViewById(R.id.tvTimer);
        btnPause = findViewById(R.id.btnPause);

        // Initialize grid and labels
        gameGrid.setColumnCount(cols);
        gameGrid.setRowCount(rows);
        tvFlags.setText("Flags: " + flagsLeft);
        tvTimer.setText("Time: 0");

        // Create game logic and database
        game = new MinesweeperGame(rows, cols, bombs);
        scoreDao = ScoreDatabase.getInstance(this).scoreDao();

        // Wait until grid layout is measured to create cells
        gameGrid.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                gameGrid.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Log.d(TAG, "Grid layout ready, creating cells.");
                createGrid();
            }
        });

        // Start timer
        startTimer();

        // Pause button listener
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (boardRevealed) {
                    // If user already viewed the board, act as "Continue"
                    boardRevealed = false;
                    btnPause.setText("Pause");
                    showEndGameDialog(game.checkWin());
                } else {
                    // Normal pause behavior
                    showPauseMenu();
                }
            }
        });


    }

    /**
     * Creates the grid of clickable cells dynamically.
     */
    private void createGrid() {
        gameGrid.removeAllViews();
        cellButtons = new Button[rows][cols];

        int gridWidth = gameGrid.getWidth();
        int gridHeight = gameGrid.getHeight();
        int cellSize = Math.min(gridWidth / cols, gridHeight / rows);

        Log.d(TAG, "Creating grid: " + rows + "x" + cols + " cells.");

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

                // Click: reveal cell
                cellButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (gameOver || isPaused) {
                            return;
                        }
                        Log.d(TAG, "Cell clicked at (" + r + ", " + c + ")");
                        handleCellClick(r, c);
                    }
                });

                // Long click: flag cell
                cellButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (gameOver || isPaused) {
                            return true;
                        }
                        Log.d(TAG, "Cell long-pressed (flag) at (" + r + ", " + c + ")");
                        handleCellFlag(r, c);
                        return true;
                    }
                });

                gameGrid.addView(cellButton);
                cellButtons[row][col] = cellButton;
            }
        }
    }

    /**
     * Handles what happens when a cell is clicked (reveal action).
     */
    private void handleCellClick(int row, int col) {
        boolean safe = game.revealCell(row, col);
        updateGrid();

        if (!safe) {
            Log.d(TAG, "Bomb hit at (" + row + ", " + col + ")");
            gameOver = true;
            stopTimer();
            revealAllCells();
            showEndGameDialog(false);
        } else {
            if (game.checkWin()) {
                Log.d(TAG, "Player WON the game!");
                gameOver = true;
                stopTimer();
                saveScoreToDatabase();
                showEndGameDialog(true);
            }
        }
    }

    /**
     * Saves the current score to the database when the player wins.
     */
    private void saveScoreToDatabase() {
        String difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty == null) {
            difficulty = "Easy";
        }

        String date = DateFormat.getDateTimeInstance().format(new Date());
        Score score = new Score(difficulty, elapsedTime, date);
        scoreDao.insert(score);

        Log.d(TAG, "Score saved: " + difficulty + " - " + elapsedTime + "s at " + date);
    }

    /**
     * Handles flag placement/removal on long press.
     */
    private void handleCellFlag(int row, int col) {
        Cell cell = game.getCell(row, col);
        if (cell.isRevealed()) {
            return;
        }

        if (cell.isFlagged()) {
            cell.setFlagged(false);
            flagsLeft = flagsLeft + 1;
        } else {
            if (flagsLeft > 0) {
                cell.setFlagged(true);
                flagsLeft = flagsLeft - 1;
            }
        }

        tvFlags.setText("Flags: " + flagsLeft);
        updateGrid();
    }

    /**
     * Updates the grid UI to reflect the current game state.
     */
    private void updateGrid() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Cell cell = game.getCell(row, col);
                Button btn = cellButtons[row][col];

                if (cell.isRevealed()) {
                    btn.setEnabled(false);

                    if (cell.isBomb()) {
                        btn.setText("💣");
                        btn.setBackgroundColor(0xFFFF4444); // Red
                    } else {
                        int n = cell.getNeighborBombs();

                        if (n == 0) {
                            btn.setText("");
                        } else {
                            btn.setText(String.valueOf(n));
                        }

                        btn.setBackgroundColor(0xFFDDDDDD);

                        // Color numbers
                        if (n == 1) btn.setTextColor(0xFF0000FF);
                        else if (n == 2) btn.setTextColor(0xFF008000);
                        else if (n == 3) btn.setTextColor(0xFFFF0000);
                        else if (n == 4) btn.setTextColor(0xFF800080);
                        else if (n == 5) btn.setTextColor(0xFF8B0000);
                        else if (n == 6) btn.setTextColor(0xFF00FFFF);
                        else if (n == 7) btn.setTextColor(0xFF000000);
                        else if (n == 8) btn.setTextColor(0xFF555555);
                        else btn.setTextColor(0xFF000000);
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

    /**
     * Reveals the entire grid when the player loses.
     */
    private void revealAllCells() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Cell cell = game.getCell(row, col);
                cell.setRevealed(true);
            }
        }
        updateGrid();
    }

    /**
     * Starts the timer that counts seconds during the game.
     */
    private void startTimer() {
        isPaused = false;
        elapsedTime = 0;

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPaused) {
                    elapsedTime = elapsedTime + 1;
                    tvTimer.setText("Time: " + elapsedTime);
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };

        timerHandler.postDelayed(timerRunnable, 1000);
        Log.d(TAG, "Timer started.");
    }

    /**
     * Stops the timer.
     */
    private void stopTimer() {
        isPaused = true;
        timerHandler.removeCallbacks(timerRunnable);
        Log.d(TAG, "Timer stopped at " + elapsedTime + " seconds.");
    }

    /**
     * Displays the pause menu dialog.
     */
    private void showPauseMenu() {
        isPaused = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle("Game Paused");
        builder.setCancelable(true); // ✅ allow outside tap

        // Return to Game
        builder.setPositiveButton("Return to Game", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("GameActivity", "Pause menu: Return to Game clicked");

                if (!gameOver && !boardRevealed) {
                    isPaused = false;
                    timerHandler.postDelayed(timerRunnable, 1000);
                }

                dialog.dismiss();
            }
        });

        // Main Menu
        builder.setNeutralButton("Main Menu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("GameActivity", "Pause menu: Main Menu clicked");
                stopTimer();
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        // Retry
        builder.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("GameActivity", "Pause menu: Retry clicked");
                stopTimer();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        // ✅ Handle tap outside
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d("GameActivity", "Pause menu: dismissed by tapping outside — acting as Return to Game");

                if (!gameOver && !boardRevealed) {
                    isPaused = false;
                    timerHandler.postDelayed(timerRunnable, 1000);
                }

                dialog.dismiss();
            }
        });

        builder.show();
    }

    /**
     *
     * Gets using Gemini's API a congratulation massage for the user
     */
    private String generateMessageSync() {
        try {
            GenerateContentResponse response = client.models.generateContent(
                    MODEL,
                    FIXED_PROMPT,
                    null
            );

            String text = response.text();
            return text != null ? text : "Well Played!";

        } catch (Exception e) {
            Log.d(TAG, "Exeption catched" + e.getMessage());
            return "Well Played!";
        }
    }
    private void generateMessage() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                final String result = generateMessageSync();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GeminiMassage = result;
                        Toast.makeText(GameActivity.this, "Message generated with Gemini API: " + GeminiMassage, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Message generated with Gemini API: " + GeminiMassage);
                    }
                });
            }
        }).start();
    }


    /**
     * Shows the end-game dialog (for win or loss).
     */
    private void showEndGameDialog(final boolean win) {
        vibrate(win); // Haptic feedback
        String title;
        String message;

        if (win) {
            title = GeminiMassage ;
            message = "You cleared the board in " + elapsedTime + " seconds!";
        } else {
            title = "💣 Game Over";
            message = "You hit a bomb after " + elapsedTime + " seconds.";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true); // tap outside = view board

        // ✅ 1️⃣ VIEW SCOREBOARD (LEFT)
        builder.setNegativeButton("View Scoreboard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(GameActivity.this, ScoreboardActivity.class);
                String difficulty = getIntent().getStringExtra("difficulty");
                if (difficulty == null) {
                    difficulty = "Easy";
                }
                intent.putExtra("difficulty", difficulty);
                startActivity(intent);
            }
        });

        // ✅ 2️⃣ MAIN MENU (CENTER)
        builder.setNeutralButton("Main Menu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        // ✅ 3️⃣ RETRY (RIGHT)
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        // ✅ Tap outside → reveal board (view mode)
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                revealAllCells();
                updateGrid();
                btnPause.setText("Continue");
                isPaused = true;    // freeze game
                gameOver = true;    // prevent unpausing timer later
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }




    /**
     * Vibrates the device with different patterns for win or loss.
     */
    private void vibrate(boolean win) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) {
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (win) {
                vibrator.vibrate(VibrationEffect.createOneShot(450, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                long[] pattern = {0, 150, 100, 250, 100, 350};
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            }
        } else {
            if (win) {
                vibrator.vibrate(450);
            } else {
                long[] pattern = {0, 150, 100, 250, 100, 350};
                vibrator.vibrate(pattern, -1);
            }
        }

        Log.d(TAG, "Vibration triggered. Win: " + win);
    }
}
