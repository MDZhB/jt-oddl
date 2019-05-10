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
 * Thrown when the parser encounters a literal that does not match the type of the enclosing list structure.
 */
public class ListElementTypeMismatchException extends ODDLParseException {
    private static final long serialVersionUID = 5393244921440496342L;

    @SafeVarargs
    public ListElementTypeMismatchException(ODDLToken actual, Class<? extends ODDLToken>... expect) {
        super(createMessage(actual.getRow(), actual.getCol(), actual.toString(), createExpectMessage(expect)));
    }

    private static String createMessage(int line, int col, String actual, String expect) {
        return  "unexpected token " + actual + "; " +
                "expected " + expect +
                " at " + line + ", " + col;
    }

    private static String createExpectMessage(Class<? extends ODDLToken>[] expect) {
        StringBuilder sb = new StringBuilder();

        sb.append(expect[0].getSimpleName());

        for (int i=1; i<expect.length-1; i++) {
            sb.append(", ");
            sb.append(expect[i].getSimpleName());
        }

        if (expect.length>1) {
            sb.append(" or ");
            sb.append(expect[expect.length-1].getSimpleName());
        }

        return sb.toString();
    }
}
