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

import java.util.Objects;

/**
 * A token representing an integer literal.
 *
 * @author Nikita Leonidov
 */
public final class IntToken extends AbstractODDLToken implements PropertyValueToken {

    public enum Format {
        /** The integer is written in decimal format. */
        DEC  (10),
        /** The integer is written in hexadecimal format. */
        HEX  (16),
        /** The integer is written in octal format. */
        OCT  (8),
        /** The integer is written in binary format. */
        BIN  (2),
        /** The integer is written as a character literal. */
        CHAR (-1);

        private final int radix;

        Format(int radix) {
            this.radix = radix;
        }

        public int getRadix() {
            return radix;
        }
    }

    private final long   value;
    private final Format format;

    IntToken(String text, String value, Format format) {
        super(text);
        this.format = format;
        this.value  = format==Format.CHAR ? parseCharLiteral(value) : Long.parseLong(value, format.getRadix());
    }

    /**
     * @return the integer value represented by this token
     */
    public long getValue() {
        return value;
    }

    /**
     * @return the format in which this integer literal was specified
     */
    public Format getFormat() {
        return format;
    }

    @Override
    public Object getValueAsObject() {
        return getValue();
    }

    @Override
    public Type getType() {
        return Type.INT;
    }

    @Override
    public boolean isInt() {
        return true;
    }

    @Override
    public IntToken asInt() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        IntToken intToken = (IntToken) o;
        return getValue() == intToken.getValue() &&
                getFormat() == intToken.getFormat();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getValue(), getFormat());
    }

    private static long parseCharLiteral(String value) {
        long v = 0;
        int off = value.codePointAt(0) == '-' || value.codePointAt(0) == '+' ? 1 : 0;

        for (int i=off; i<value.length(); i++) {
            int c = value.charAt(i);
            v = (v<<8) | c;
        }

        if (value.codePointAt(0)=='-') {
            v *= -1;
        }

        return v;
    }
}
