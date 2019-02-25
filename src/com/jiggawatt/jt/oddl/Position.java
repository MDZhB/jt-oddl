package com.jiggawatt.jt.oddl;

public class Position {
    private int row;
    private int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getLine() {
        return row;
    }

    public int getColumn() {
        return col;
    }
}
