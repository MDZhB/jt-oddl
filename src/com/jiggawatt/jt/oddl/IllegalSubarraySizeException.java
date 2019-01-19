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

import java.io.IOException;

/**
 * Thrown when the parser encounters a sub-array of a size other than that specified for the enclosing array list
 * structure.
 *
 * @author Nikita Leonidov
 */
public class IllegalSubarraySizeException extends IOException {
    private static final long serialVersionUID = 4475058997483723650L;

    public IllegalSubarraySizeException(int line, int row, int actual, int expect) {
        super("at "+line+", "+row+"; expected "+expect+" but found "+actual+" elements");
    }
}
