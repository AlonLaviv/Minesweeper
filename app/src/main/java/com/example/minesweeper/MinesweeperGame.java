package com.example.minesweeper;

import android.util.Log;
import java.util.Random;

/**
 * MinesweeperGame
 * ----------------
 * Handles all the core game logic of Minesweeper:
 *  - Generating the board
 *  - Placing bombs
 *  - Calculating neighbor counts
 *  - Revealing and flagging cells
 *  - Checking win/loss conditions
 *
 *  This class is independent of the UI and interacts with {@link Cell} objects.
 */
public class MinesweeperGame {

    private static final String TAG = "MinesweeperGame"; // For Logcat debugging

    private int rows, cols, bombs;
    private Cell[][] board;
    private boolean gameOver;
    private int revealedCells;
    private boolean firstMove = true; // ensures first click is safe

    /** Constructor initializes the board and generates bombs + neighbors */
    public MinesweeperGame(int rows, int cols, int bombs) {
        this.rows = rows;
        this.cols = cols;
        this.bombs = bombs;
        this.board = new Cell[rows][cols];
        this.gameOver = false;
        this.revealedCells = 0;

        Log.d(TAG, "Initializing board: " + rows + "x" + cols + " with " + bombs + " bombs");
        initBoard();
        placeBombs();
        calculateNeighbors();
    }

    /** Creates an empty board of unrevealed, non-bomb cells */
    private void initBoard() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                board[row][col] = new Cell();
            }
        }
        Log.d(TAG, "Board initialized with empty cells.");
    }

    /** Randomly places bombs on the board */
    private void placeBombs() {
        Random rand = new Random();
        int placed = 0;

        while (placed < bombs) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);

            if (!board[r][c].isBomb()) {
                board[r][c].setBomb(true);
                placed++;
            }
        }
        Log.d(TAG, "Bombs placed: " + placed);
    }

    /** Counts bombs around each cell and stores it */
    private void calculateNeighbors() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Cell current = board[row][col];

                // Skip bombs
                if (current.isBomb()) continue;

                int neighborBombs = 0;

                // Check all 8 surrounding cells
                for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
                    for (int colOffset = -1; colOffset <= 1; colOffset++) {
                        if (rowOffset == 0 && colOffset == 0) continue;

                        int neighborRow = row + rowOffset;
                        int neighborCol = col + colOffset;

                        boolean insideBoard =
                                neighborRow >= 0 && neighborRow < rows &&
                                        neighborCol >= 0 && neighborCol < cols;

                        if (insideBoard && board[neighborRow][neighborCol].isBomb()) {
                            neighborBombs++;
                        }
                    }
                }
                current.setNeighborBombs(neighborBombs);
            }
        }
        Log.d(TAG, "Neighbor bomb counts calculated.");
    }

    /**
     * Reveals a cell. Handles first-move safety, flood-fill logic, and game-over conditions.
     * @return true if the cell is safe; false if a bomb was hit.
     */
    public boolean revealCell(int row, int col) {
        Log.d(TAG, "Revealing cell (" + row + ", " + col + ")");

        // Ignore already revealed or flagged cells
        if (board[row][col].isRevealed() || board[row][col].isFlagged()) return true;

        // Handle first move — regenerate if not safe
        if (firstMove) {
            firstMove = false;
            while (board[row][col].isBomb() || board[row][col].getNeighborBombs() != 0) {
                Log.d(TAG, "First click not safe — regenerating board.");
                regenerateBoardWithout(row, col);
            }
        }

        board[row][col].setRevealed(true);
        revealedCells++;

        // If bomb — game over
        if (board[row][col].isBomb()) {
            gameOver = true;
            Log.d(TAG, "💣 Bomb hit! Game Over.");
            return false;
        }

        // If empty cell — reveal neighbors recursively
        if (board[row][col].getNeighborBombs() == 0) {
            floodReveal(row, col);
        }

        return true;
    }

    /**
     * Recursive flood-fill for revealing connected empty cells
     * (reveals surrounding safe areas when a zero-cell is clicked).
     */
    private void floodReveal(int row, int col) {
        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            for (int colOffset = -1; colOffset <= 1; colOffset++) {
                if (rowOffset == 0 && colOffset == 0) continue;

                int neighborRow = row + rowOffset;
                int neighborCol = col + colOffset;

                boolean insideBoard =
                        neighborRow >= 0 && neighborRow < rows &&
                                neighborCol >= 0 && neighborCol < cols;

                if (!insideBoard) continue;

                Cell neighbor = board[neighborRow][neighborCol];

                // Skip revealed, flagged, or bomb cells
                if (neighbor.isRevealed() || neighbor.isFlagged() || neighbor.isBomb()) continue;

                neighbor.setRevealed(true);
                revealedCells++;

                // Continue revealing if also empty
                if (neighbor.getNeighborBombs() == 0) {
                    floodReveal(neighborRow, neighborCol);
                }
            }
        }
    }

    /**
     * Rebuilds the board so that the clicked cell and its 8 neighbors are guaranteed safe.
     */
    private void regenerateBoardWithout(int safeRow, int safeCol) {
        Log.d(TAG, "Regenerating board excluding area around (" + safeRow + ", " + safeCol + ")");

        // Clear board state
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                board[r][c].setBomb(false);
                board[r][c].setNeighborBombs(0);
                board[r][c].setRevealed(false);
                board[r][c].setFlagged(false);
            }
        }

        // Place bombs again — avoiding the safe cell and its neighbors
        Random rand = new Random();
        int placed = 0;

        while (placed < bombs) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);

            boolean nearSafeCell = Math.abs(r - safeRow) <= 1 && Math.abs(c - safeCol) <= 1;
            if (nearSafeCell) continue;

            if (!board[r][c].isBomb()) {
                board[r][c].setBomb(true);
                placed++;
            }
        }

        // Recalculate bomb counts
        calculateNeighbors();
    }

    /**
     * Checks if the player has revealed all non-bomb cells (win condition).
     * @return true if the game is won, false otherwise
     */
    public boolean checkWin() {
        int totalCells = rows * cols;
        boolean win = (totalCells - revealedCells) == bombs && !gameOver;
        if (win) {
            Log.d(TAG, "🏆 Player won the game!");
        }
        return win;
    }

    /** Returns a reference to the cell object at (row, col). */
    public Cell getCell(int row, int col) {
        return board[row][col];
    }

    /** Returns whether the game is currently over (used by UI). */
    public boolean isGameOver() {
        return gameOver;
    }
}
