package com.jiggawatt.jt.oddl;

public class ODDLFormatException extends Exception {
    private static final long serialVersionUID = -335864823755113174L;

    // TODO use token param
    public ODDLFormatException(Position position, String message) {
        super(message);
    }

    public ODDLFormatException(ODDLToken token, String message) {
        super(message);
    }

    public ODDLFormatException(ODDLToken token, String message, Throwable cause) {
        super(message, cause);
    }
}
