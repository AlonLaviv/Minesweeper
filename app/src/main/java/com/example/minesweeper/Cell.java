package com.example.minesweeper;

public class Cell {
    private boolean isBomb;
    private boolean isRevealed;
    private boolean isFlagged;
    private int neighborBombs;

    public Cell() {
        this.isBomb = false;
        this.isRevealed = false;
        this.isFlagged = false;
        this.neighborBombs = 0;
    }

    public boolean isBomb() {
        return isBomb;
    }

    public void setBomb(boolean bomb) {
        isBomb = bomb;
    }

    public boolean isRevealed() {
        return isRevealed;
    }

    public void setRevealed(boolean revealed) {
        isRevealed = revealed;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public void setFlagged(boolean flagged) {
        isFlagged = flagged;
    }

    public int getNeighborBombs() {
        return neighborBombs;
    }

    public void setNeighborBombs(int count) {
        neighborBombs = count;
    }

    public void incrementNeighborBombs() {
        neighborBombs++;
    }
}
