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
 * A token representing a boolean literal.
 *
 * @author Nikita Leonidov
 */
public final class BoolToken extends AbstractODDLToken implements PropertyValueToken {

    private final boolean value;

    BoolToken(int row, int col, String text) {
        super(row, col, text);
        value = Boolean.valueOf(text);
    }

    /**
     * @return the boolean value represented by this token
     */
    public boolean getValue() {
        return value;
    }

    @Override
    public Object getValueAsObject() {
        return getValue();
    }

    @Override
    public Type getType() {
        return Type.BOOL;
    }

    @Override
    public boolean isBool() {
        return true;
    }

    @Override
    public BoolToken asBool() {
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
