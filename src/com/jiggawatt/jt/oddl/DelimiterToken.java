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

    public static final DelimiterToken EOF     = new DelimiterToken(null);

    public static final DelimiterToken LBRACE  = new DelimiterToken("{");
    public static final DelimiterToken RBRACE  = new DelimiterToken("}");
    public static final DelimiterToken LSQUARE = new DelimiterToken("[");
    public static final DelimiterToken RSQUARE = new DelimiterToken("]");
    public static final DelimiterToken LPAREN  = new DelimiterToken("(");
    public static final DelimiterToken RPAREN  = new DelimiterToken(")");
    public static final DelimiterToken COMMA   = new DelimiterToken(",");
    public static final DelimiterToken EQUALS  = new DelimiterToken("=");

    private DelimiterToken(String text) {
        super(text);
    }

    static boolean isDelimiter(int c) {
        return c==-1 || "{}[](),=".indexOf(c) >= 0;
    }

    public static ODDLToken create(int c) {
        switch (c) {
            case '{':
                return DelimiterToken.LBRACE;
            case '}':
                return DelimiterToken.RBRACE;
            case '[':
                return DelimiterToken.LSQUARE;
            case ']':
                return DelimiterToken.RSQUARE;
            case '(':
                return DelimiterToken.LPAREN;
            case ')':
                return DelimiterToken.RPAREN;
            case ',':
                return DelimiterToken.COMMA;
            case '=':
                return DelimiterToken.EQUALS;
        }

        throw new IllegalArgumentException(new StringBuilder().appendCodePoint(c).toString());
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
    public DelimiterToken asDelimiter() {
        return this;
    }

    @Override
    public boolean equals(Object other) {
        return this==other;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
