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
 * A token representing a delimiter.
 *
 * @author Nikita Leonidov
 */
public final class DelimiterToken extends AbstractODDLToken {

    private static final String DELIMITERS = "\u0003{}[](),=";
    private final int codePoint;

    private DelimiterToken(int row, int col, int c) {
        super(row, col, tokenString(c));
        codePoint = c;
    }

    static boolean isDelimiterCharacter(int c) {
        return c==-1 || DELIMITERS.indexOf(c) >= 0;
    }

    static DelimiterToken createEOF(int row, int col) {
        return new DelimiterToken(row, col, '\u0003');
    }

    static DelimiterToken create(int row, int col, int c) {
        if (DELIMITERS.indexOf(c)>=0) {
            return new DelimiterToken(row, col, c);
        }

        throw new IllegalArgumentException(new StringBuilder().appendCodePoint(c).toString());
    }

    public int getCodePoint() {
        return codePoint;
    }

    @Override
    public Type getType() {
        return Type.DELIMITER;
    }

    @Override
    public boolean isDelimiter() {
        return true;
    }

    @Override
    public boolean isDelimiter(int c) {
        return codePoint == c;
    }

    @Override
    public boolean isEOF() {
        return codePoint == '\u0003';
    }

    @Override
    public DelimiterToken asDelimiter() {
        return this;
    }

    private static String tokenString(int c) {
        if (c=='\u0003') {
            return null;
        } else {
            int idx = DELIMITERS.indexOf(c);
            return DELIMITERS.substring(idx, idx + 1);
        }
    }
}
