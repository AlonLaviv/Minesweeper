package com.example.minesweeper;

import java.util.Random;

public class MinesweeperGame {

    private int rows, cols, bombs;
    private Cell[][] board;
    private boolean gameOver;
    private int revealedCells;
    private boolean firstMove = true;

    public MinesweeperGame(int rows, int cols, int bombs) {
        this.rows = rows;
        this.cols = cols;
        this.bombs = bombs;
        this.board = new Cell[rows][cols];
        this.gameOver = false;
        this.revealedCells = 0;

        initBoard();
        placeBombs();
        calculateNeighbors();
    }

    /** Initializes the board with empty cells */
    private void initBoard() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                board[row][col] = new Cell();
            }
        }
    }

    /** Randomly place bombs on the board */
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
    }

    /** Count and assign number of neighboring bombs for each cell */
    private void calculateNeighbors() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Cell current = board[row][col];

                if (current.isBomb()) continue;

                int neighborBombs = 0;
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
    }

    /** Reveals a cell and handles game logic */
    public boolean revealCell(int row, int col) {
        if (board[row][col].isRevealed() || board[row][col].isFlagged()) return true;

        // --- Handle first move safety ---
        if (firstMove) {
            firstMove = false;

            // If the first click hits a bomb or a numbered cell, re-generate the board
            while (board[row][col].isBomb() || board[row][col].getNeighborBombs() != 0) {
                regenerateBoardWithout(row, col);
            }
        }

        board[row][col].setRevealed(true);
        revealedCells++;

        // Hit a bomb?
        if (board[row][col].isBomb()) {
            gameOver = true;
            return false;
        }

        // If this cell has no nearby bombs, reveal its neighbors recursively
        if (board[row][col].getNeighborBombs() == 0) {
            floodReveal(row, col);
        }

        return true;
    }

    /** Recursive flood-fill to reveal connected empty cells */
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

                if (neighbor.isRevealed() || neighbor.isFlagged() || neighbor.isBomb()) continue;

                neighbor.setRevealed(true);
                revealedCells++;

                // Keep expanding if also empty
                if (neighbor.getNeighborBombs() == 0) {
                    floodReveal(neighborRow, neighborCol);
                }
            }
        }
    }

    /** Re-generates the board, ensuring the clicked cell and its neighbors are safe */
    private void regenerateBoardWithout(int safeRow, int safeCol) {
        // Clear all cells
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                board[r][c].setBomb(false);
                board[r][c].setNeighborBombs(0);
                board[r][c].setRevealed(false);
                board[r][c].setFlagged(false);
            }
        }

        // Place bombs again, avoiding the clicked cell and its 8 neighbors
        Random rand = new Random();
        int placed = 0;

        while (placed < bombs) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);

            // Don't place near the clicked area
            boolean nearSafeCell = Math.abs(r - safeRow) <= 1 && Math.abs(c - safeCol) <= 1;
            if (nearSafeCell) continue;

            if (!board[r][c].isBomb()) {
                board[r][c].setBomb(true);
                placed++;
            }
        }

        // Recalculate neighbors
        calculateNeighbors();
    }

    /** Checks if all safe cells are revealed */
    public boolean checkWin() {
        int totalCells = rows * cols;
        return (totalCells - revealedCells) == bombs && !gameOver;
    }

    public Cell getCell(int row, int col) {
        return board[row][col];
    }

    public boolean isGameOver() {
        return gameOver;
    }
}
