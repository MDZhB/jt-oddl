/*
 * Jiggatech OpenDDL Parser
 *
 * Copyright (c) 2019 Nikita Leonidov
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.jiggawatt.jt.oddl;

/**
 * Represents an OpenDDL lexical token. Tokens may be converted from a general type to a more specific type without
 * casting, like so:
 * <pre>
 * {@code
 * // given an ODDLToken...
 * if (token.isFloat()) { // ...test whether or not it is a float literal...
 *     System.out.println(token.asFloat().getValue()); // ...and print its value
 * }
 * }
 * </pre>
 * If handling a large number of token types, it may be wiser to use a switch statement:
 * <pre>
 * {@code
 * switch (token.getType()) {
 *     case BOOL:
 *         System.out.println(token.asBool().getValue());
 *         break;
 *     case FLOAT:
 *         System.out.println(token.asFloat().getValue());
 *         break;
 *     // etc.
 * }
 * }
 * </pre>
 * These "downcasting" methods throw {@link IllegalArgumentException} when the token is not an instance of the desired
 * class.
 *
 * @author Nikita Leonidov
 */
public interface ODDLToken {

    enum Type {
        DELIMITER,
        IDENTIFIER,
        DATA_TYPE,
        NAME,
        BOOL,
        INT,
        FLOAT,
        REF,
        STRING
    }

    /**
     * @return the text corresponding to this token
     */
    String getText();

    Type getType();

    /**
     * Tests whether or not this token represents a delimiter. When true, {@link #asDelimiter()} is guaranteed to return
     * a valid token, and {@link #getType()} will return {@link Type#DELIMITER}.
     * @return <tt>true</tt> when this token represents a delimiter, <tt>false</tt> otherwise
     */
    default boolean isDelimiter() {
        return false;
    }

    /**
     * Tests whether or not the this token represents the given delimiter character.
     * @param c  a delimiter code point
     * @return <tt>true</tt> when this token represents a delimiter with the given value, <tt>false</tt> otherwise
     */
    default boolean isDelimiter(int c) {
        return false;
    }

    /**
     * Tests whether or not this token is the EOF delimiter.
     * @return <tt>true</tt> if this token represents end of file, <tt>false</tt> otherwise
     */
    default boolean isEOF() {
        return false;
    }

    /**
     * @return this token, if it represents a delimiter
     */
    default DelimiterToken asDelimiter() {
        throw new UnsupportedOperationException();
    }

    /**
     * Tests whether or not this token represents a data type. When true, {@link #asDataType()} is guaranteed to return
     * a valid token, and {@link #getType()} will return {@link Type#IDENTIFIER}.
     * @return <tt>true</tt> when this token represents a data type, <tt>false</tt> otherwise
     */
    default boolean isDataType() {
        return false;
    }

    /**
     * @return this token, if it represents a data type
     */
    default DataTypeToken asDataType() {
        throw new UnsupportedOperationException();
    }

    /**
     * Tests whether or not this token represents an identifier. When true, {@link #asIdentifier()} ()} is guaranteed to
     * return a valid token, and {@link #getType()} will return {@link Type#IDENTIFIER}.
     * @return <tt>true</tt> when this token represents an identifier, <tt>false</tt> otherwise
     */
    default boolean isIdentifier() {
        return false;
    }

    /**
     * @return this token, if it represents an identifier
     */
    default IdentifierToken asIdentifier() {
        throw new UnsupportedOperationException();
    }

    /**
     * Tests whether or not this token represents a name. When true, {@link #asName()} is guaranteed to return a valid
     * token, and {@link #getType()} will return {@link Type#NAME}.
     * @return <tt>true</tt> when this token represents a name, <tt>false</tt> otherwise
     */
    default boolean isName() {
        return false;
    }

    /**
     * @return this token, if it represents a name
     */
    default NameToken asName() {
        throw new UnsupportedOperationException();
    }

    /**
     * Tests whether or not this token represents a boolean literal. When true, {@link #asDelimiter()} is guaranteed to
     * return a valid token, and {@link #getType()} will return {@link Type#BOOL}.
     * @return <tt>true</tt> when this token represents a bool literal, <tt>false</tt> otherwise
     */
    default boolean isBool() {
        return false;
    }

    /**
     * @return this token, if it represents a boolean literal
     */
    default BoolToken asBool() {
        throw new UnsupportedOperationException();
    }

    /**
     * Tests whether or not this token represents an integer literal. When true, {@link #asInt()} is guaranteed to
     * return a valid token, and {@link #getType()} will return {@link Type#INT}.
     * @return <tt>true</tt> when this token represents an integer literal, <tt>false</tt> otherwise
     */
    default boolean isInt() {
        return false;
    }

    /**
     * @return this token, if it represents an integer literal
     */
    default IntToken asInt() {
        throw new UnsupportedOperationException();
    }

    /**
     * Tests whether or not this token represents a float literal. When true, {@link #asFloat()} is guaranteed to return
     * a valid token, and {@link #getType()} will return {@link Type#FLOAT}.
     * @return <tt>true</tt> when this token represents a float literal, <tt>false</tt> otherwise
     */
    default boolean isFloat() {
        return false;
    }

    /**
     * @return this token, if it represents a float literal
     */
    default FloatToken asFloat() {
        throw new UnsupportedOperationException();
    }

    /**
     * Tests whether or not this token represents a string literal. When true, {@link #asString()} is guaranteed to
     * return a valid token, and {@link #getType()} will return {@link Type#STRING}.
     * @return <tt>true</tt> when this token represents a string literal, <tt>false</tt> otherwise
     */
    default boolean isString() {
        return false;
    }

    /**
     * @return this token, if it represents a string literal
     */
    default StringToken asString() {
        throw new UnsupportedOperationException();
    }

    /**
     * Tests whether or not this token represents a reference. When true, {@link #asRef()} is guaranteed to return a
     * valid token, and {@link #getType()} will return {@link Type#REF}.
     * @return <tt>true</tt> when this token represents a reference, <tt>false</tt> otherwise
     */
    default boolean isRef() {
        return false;
    }

    /**
     * @return this token, if it represents a reference
     */
    default RefToken asRef() {
        throw new UnsupportedOperationException();
    }

    /**
     * Tests whether or not this token is a permitted property value. When true, {@link #asPropertyValue()} is
     * guaranteed to return a valid token.
     * @return <tt>true</tt> when this token represents a valid property value, <tt>false</tt> otherwise
     */
    default boolean isPropertyValue() {
        return false;
    }

    /**
     * @return this token, if it is a permitted property value
     */
    default PropertyValueToken asPropertyValue() {
        throw new UnsupportedOperationException();
    }
}
