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

import java.io.*;

import static java.util.Objects.requireNonNull;

public final class ODDLInputStream {

    private static final int READAHEAD_SIZE = 3;

    private final PushbackReader in;

    private final int[] queue = new int[READAHEAD_SIZE];
    private int available;

    private int row;
    private int col;

    /** when true, increment row on next {@link #read()} */
    private boolean hasNewLine;

    /**
     * Wraps an input stream in an ODDLInputStream.
     * @param in input stream; may not be null
     */
    public ODDLInputStream(InputStream in) {
        this.in = new PushbackReader(new InputStreamReader(requireNonNull(in, "input stream")));
    }

    /**
     * Wraps a reader in an ODDLInputStream.
     * @param in input reader; may not be null
     */
    public ODDLInputStream(Reader in) {
        this.in = (in instanceof PushbackReader)
                ? (PushbackReader) in
                : new PushbackReader(requireNonNull(in, "input reader"));
    }

    /**
     * Returns a character at the given offset from the input stream's current position. If the offset position exceeds
     * the length of the input, returns -1
     *
     * @param ahead offset in characters from the input stream's current position; must be 0 or greater
     * @return the character read at the given offset, as an integer in the range 0 to 65535 (0x00-0xffff), or -1 if the
     *         offset position is past the end of the stream
     * @throws IOException when an IO exception occurs
     */
    int peek(int ahead) throws IOException {
        for (int i=available; i<=ahead; i++) {
            queue[i] = doRead();
            available++;
        }

        return queue[ahead];
    }

    /**
     * Reads a character, converting carriage returns and CRLF sequences into single newlines.
     *
     * @return the character read, as an integer in the range 0 to 65535 (0x00-0xffff), or -1 if the end of the stream
     *         has been reached
     * @throws IOException when an IO exception occurs
     */
    int read() throws IOException {
        if (hasNewLine) {
            hasNewLine = false;
            row++;
            col = 0;
        }

        int c;

        if (available > 0) {
            c = queue[0];
            System.arraycopy(queue, 1, queue, 0, --available);
        } else {
            c = doRead();
        }

        if (c==-1) {
            return -1;
        } else if (c=='\n') {
            hasNewLine = true;
        } else {
            col++;
        }

        return c;
    }

    /**
     * @return the current row number (1 or greater)
     */
    int getLine() {
        return row;
    }

    /**
     * @return the current column number (1 or greater)
     */
    int getCol() {
        return col;
    }

    /**
     * Reads a character, converting <tt>\r\n</tt> or <tt>\r</tt> to <tt>\n</tt>.
     *
     * @return a character from the stream, or -1 to indicate EOF
     * @throws IOException when an IO exception occurs
     */
    private int doRead() throws IOException {
        int c = in.read();

        if (c == '\r') {
            c = in.read();
            if (c != '\n') { // this is a single \r
                in.unread(c);
            }
            return '\n';
        }

        return c;
    }

}

