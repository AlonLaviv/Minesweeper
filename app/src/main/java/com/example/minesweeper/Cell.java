package com.example.minesweeper;

import android.util.Log;

/**
 * Represents a single cell in the Minesweeper grid.
 * Each cell stores its own state:
 * - Whether it contains a bomb
 * - Whether it is revealed
 * - Whether it is flagged
 * - How many bombs are around it
 */
public class Cell {

    private static final String TAG = "Cell"; // Used for log messages

    private boolean isBomb;        // True if this cell contains a bomb
    private boolean isRevealed;    // True if this cell has been revealed
    private boolean isFlagged;     // True if this cell is flagged by the player
    private int neighborBombs;     // Number of bombs surrounding this cell

    /**
     * Default constructor — creates a safe, hidden, and unflagged cell.
     */
    public Cell() {
        this.isBomb = false;
        this.isRevealed = false;
        this.isFlagged = false;
        this.neighborBombs = 0;
        Log.d(TAG, "Cell created: safe and hidden.");
    }

    /**
     * Returns true if this cell contains a bomb.
     */
    public boolean isBomb() {
        return isBomb;
    }

    /**
     * Sets whether this cell contains a bomb.
     */
    public void setBomb(boolean bomb) {
        this.isBomb = bomb;
        if (bomb) {
            Log.d(TAG, "setBomb: This cell is now a BOMB.");
        } else {
            Log.d(TAG, "setBomb: This cell is SAFE.");
        }
    }

    /**
     * Returns true if this cell has been revealed.
     */
    public boolean isRevealed() {
        return isRevealed;
    }

    /**
     * Sets whether this cell has been revealed.
     */
    public void setRevealed(boolean revealed) {
        this.isRevealed = revealed;
        Log.d(TAG, "setRevealed: " + revealed);
    }

    /**
     * Returns true if this cell is flagged.
     */
    public boolean isFlagged() {
        return isFlagged;
    }

    /**
     * Sets whether this cell is flagged by the player.
     */
    public void setFlagged(boolean flagged) {
        this.isFlagged = flagged;
        Log.d(TAG, "setFlagged: " + flagged);
    }

    /**
     * Returns the number of bombs surrounding this cell.
     */
    public int getNeighborBombs() {
        return neighborBombs;
    }

    /**
     * Sets the number of bombs surrounding this cell.
     */
    public void setNeighborBombs(int count) {
        this.neighborBombs = count;
        Log.d(TAG, "setNeighborBombs: " + count);
    }

}
