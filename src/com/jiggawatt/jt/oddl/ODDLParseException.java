package com.jiggawatt.jt.oddl;

/**
 * Superclass for various OpenDDL parsing exceptions.
 *
 * @author Nikita Leonidov
 */
public abstract class ODDLParseException extends Exception {
    private static final long serialVersionUID = -3469266098420744764L;

    ODDLParseException(String message) {
        super(message);
    }
}
