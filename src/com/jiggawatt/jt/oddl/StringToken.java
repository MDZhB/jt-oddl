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
 * A token representing a string literal, or multiple adjacent string literals. If this object is formed from multiple
 * adjacent literals, the token's {@link #getValue() value} is the concatenation of these literals.
 *
 * @author Nikita Leonidov
 */
public final class StringToken extends AbstractODDLToken implements PropertyValueToken {

    private final String value;

    StringToken(String text, String value) {
        super(text);
        this.value = value;
    }

    /**
     * @return the string value represented by one or more adjacent string literals corresponding to this token
     */
    public String getValue() {
        return value;
    }

    @Override
    public Object getValueAsObject() {
        return getValue();
    }
    @Override
    public Type getType() {
        return Type.STRING;
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public StringToken asString() {
        return this;
    }
}
