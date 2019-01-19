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
 * A token representing a name.
 *
 * @author Nikita Leonidov
 */
public final class NameToken extends AbstractODDLToken {

    public static final NameToken NULL = new NameToken("null", null, true);

    private final boolean global;
    private final String  value;

    static NameToken create(String text) {
        if (NULL.getText().equals(text)) {
            return NULL;
        }

        if (text.codePointAt(0)=='$') {
            return new NameToken(text, text.substring(1), true);
        }

        if (text.codePointAt(0)=='%') {
            return new NameToken(text, text.substring(1), false);
        }

        throw new IllegalArgumentException(text);
    }

    private NameToken(String text, String value, boolean global) {
        super(text);
        this.value = value;
        this.global = global;
    }

    public boolean isGlobal() {
        return global;
    }

    /**
     * @return this name, without a global (<tt>$</tt>) or local (<tt>%</tt>) prefix
     */
    public String getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return Type.NAME;
    }

    @Override
    public boolean isName() {
        return true;
    }

    @Override
    public NameToken asName() {
        return this;
    }
}
