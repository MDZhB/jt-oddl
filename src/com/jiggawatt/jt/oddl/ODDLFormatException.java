package com.jiggawatt.jt.oddl;

public class ODDLFormatException extends Exception {
    private static final long serialVersionUID = -335864823755113174L;

    public ODDLFormatException(int row, int col, String message) {
        super(String.format("at line %d, column %d: %s", row, col, message));
    }

    public ODDLFormatException(int row, int col, String message, Throwable cause) {
        super(String.format("at line %d, column %d: %s", row, col, message), cause);
    }

    public ODDLFormatException(ODDLToken token, String message) {
        this(token.getRow(), token.getCol(), message);
    }

    public ODDLFormatException(ODDLToken token, String message, Throwable cause) {
        this(token.getRow(), token.getCol(), message, cause);
    }
}
