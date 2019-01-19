package com.jiggawatt.jt.oddl;

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

import java.util.Objects;

/**
 * A token representing a float literal.
 *
 * @author Nikita Leonidov
 */
public final class FloatToken extends AbstractODDLToken implements PropertyValueToken {

    private final double value;

    FloatToken(String text, String value) {
        this(text, Double.valueOf(value));
    }

    FloatToken(String text, double value) {
        super(text);
        this.value = value;
    }

    /**
     * @return the floating-point value represented by this token
     */
    public double getValue() {
        return value;
    }

    @Override
    public Object getValueAsObject() {
        return getValue();
    }

    @Override
    public Type getType() {
        return Type.FLOAT;
    }

    @Override
    public boolean isFloat() {
        return true;
    }

    @Override
    public FloatToken asFloat() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FloatToken that = (FloatToken) o;
        return Double.compare(that.getValue(), getValue()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getValue());
    }
}
