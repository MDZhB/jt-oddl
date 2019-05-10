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
import java.util.Arrays;

/**
 * Splits text read from an input stream into OpenDDL tokens. Supports read-ahead.
 *
 * @author Nikita Leonidov
 */
class ODDLTokenizer {

    private static final int READAHEAD_SIZE = 3;

    private final ODDLInputStream in;

    private ODDLToken[] tokens = new ODDLToken[READAHEAD_SIZE];
    private int[] row = new int[READAHEAD_SIZE];
    private int[] col = new int[READAHEAD_SIZE];
    private int available;

    private int lastRow = -1;
    private int lastCol = -1;

    ODDLTokenizer(ODDLInputStream in) {
        this.in = in;
    }

    /**
     * Retrieves a token at the given offset from the tokenizer's current position in the input stream. If the offset
     * is greater than the number of tokens available in the stream, returns an EOF token.
     * @param ahead offset in tokens from the tokenizer's current position; must be 0 or greater
     * @return the token at the given offset, or an EOF token if the offset is greater than the number of
     * remaining tokens
     * @throws IOException when an IO exception occurs
     */
    ODDLToken peek(int ahead) throws IOException {
        doPeek(ahead);
        return tokens[ahead];
    }

    /**
     * Returns the token at the current position in the input stream, and advances position to the next token. If no
     * more tokens are available in the stream, returns an EOF token.
     * @return the token at the current position in the input stream, or an EOF token if none is available
     * @throws IOException when an IO exception occurs
     */
    ODDLToken read() throws IOException {
        // Consume from read-ahead queue first
        ODDLToken ret;
        if (available > 0) {
            ret     = tokens[0];
            lastRow = row[0];
            lastCol = col[0];

            System.arraycopy(tokens, 1, tokens, 0, --available);
        } else {
            consumeComments();
            lastRow = in.getLine();
            lastCol = in.getCol();
            ret     = readToken();
        }

        return ret;
    }

    /**
     * Retrieves the line number for the given token in the peek queue, or the last read token.
     * @param ahead the number of the token in the peek queue for which to retrieve a line number, or -1 for the
     *              last-read token.
     * @return the line number for the token at the given index
     * @throws IOException when an IO exception occurs
     */
    int getLine(int ahead) throws IOException {
        if (ahead==-1) {
            return lastRow;
        } else if (ahead<-1) {
            throw new IllegalArgumentException("expected -1 or a nonnegative integer, found "+ahead);
        } else {
            doPeek(ahead);
            return row[ahead];
        }
    }

    /**
     * Retrieves the column number for the given token in the peek queue, or the last read token.
     * @param ahead the number of the token in the peek queue for which to retrieve a column number, or -1 for the
     *              last-read token.
     * @return the column number for the token at the given index
     * @throws IOException when an IO exception occurs
     */
    int getCol(int ahead) throws IOException {
        if (ahead==-1) {
            return lastCol;
        } else if (ahead<-1) {
            throw new IllegalArgumentException("expected -1 or a nonnegative integer, found "+ahead);
        } else {
            doPeek(ahead);
            return col[ahead];
        }
    }

    /**
     * Reads the next token, failing if <tt>expectType</tt> is not assignable from the read token's type.
     * @param expectType  a class object representing the expected type of the next token
     * @param <T>         the expected type of the next token
     * @return the read token
     * @throws IOException when an IO exception occurs
     * @throws UnexpectedTokenException when <tt>expectType</tt> is not assignable from the read token's type
     */
    <T extends ODDLToken> T read(Class<T> expectType) throws IOException, UnexpectedTokenException {
        ODDLToken ret = read();

        if (ret.isEOF()) {
            throw new UnexpectedEOFException(in);
        }

        if (!expectType.isAssignableFrom(ret.getClass())) {
            throw new UnexpectedTokenException(getLine(-1), getCol(-1), ret, expectType);
        }

        return expectType.cast(ret);
    }

    /**
     * Reads the next token, failing if it is not a delimiter token with the given value.
     * @param expectValue the expected token value
     * @return a delimiter token with the expected value
     * @throws IOException when an IO exception occurs
     * @throws UnexpectedTokenException when the read token was not a delimiter token with value <tt>expectSame</tt>
     */
    DelimiterToken read(int expectValue) throws IOException, UnexpectedTokenException {
        ODDLToken ret = read();

        if (ret.isEOF()) {
            throw new UnexpectedEOFException(in);
        }

        if (!ret.isDelimiter(expectValue)) {
            throw new UnexpectedTokenException(getLine(-1), getCol(-1), ret.getText(), new StringBuilder().appendCodePoint(expectValue).toString());
        }

        return ret.asDelimiter();
    }

    /**
     * Consumes the next token if it is a delimiter with the given value.
     * @param codePoint  the value of the desired token
     * @return true if the tokenizer read a token with the given value
     * @throws IOException when an IO exception occurs
     */
    boolean consumeIfPresent(int codePoint) throws IOException {
        if (peek(0).isDelimiter(codePoint)) {
            read();
            return true;
        } else {
            return false;
        }
    }

    private void doPeek(int ahead) throws IOException {
        if (tokens.length <= ahead) {
            tokens = Arrays.copyOf(tokens, ahead+1);
            row    = Arrays.copyOf(row, ahead+1);
            col    = Arrays.copyOf(col, ahead+1);
        }

        for (int i = available; i<=ahead; i++) {
            consumeComments();
            row   [i] = in.getLine();
            col   [i] = in.getCol();
            tokens[i] = readToken();

            available++;
        }
    }

    private ODDLToken readToken() throws IOException {
        int c = in.peek(0);
        if (c==-1) {
            return DelimiterToken.createEOF();
        }

        if (isLeadingIdentifierChar(c)) {
            return readIdentifierOrKeyword();
        }

        if (c=='+' || c=='-' || c=='.' || Character.isDigit(c)) {
            return readNumberLiteral();
        }

        switch (c) {
            case '\'':
                return readCharLiteral(new StringBuilder());
            case '$':
            case '%':
                return NameToken.create(readIdentifierText());
            case '"':
                return readStringLiteral();
        }

        if (DelimiterToken.isDelimiterCharacter(c)) {
            return DelimiterToken.create(in.read());
        }

        throw new UnexpectedCharacterException(in, c);
    }

    private void consumeComments() throws IOException {
        while (true) {
            consumeWhitespace();
            if (in.peek(1) == '/') {
                consumeSingleLineComment();
            } else if (in.peek(1) == '*') {
                consumeBlockComment();
            } else {
                break;
            }
        }
        consumeWhitespace();
    }

    private void consumeWhitespace() throws IOException {
        while (isWhitespace(in.peek(0))) {
            in.read();
        }
    }

    private boolean isLeadingIdentifierChar(int c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_';
    }

    private boolean isIdentifierChar(int c) {
        return isLeadingIdentifierChar(c) || (c >= '0' && c <= '9');
    }

    private String readIdentifierText() throws IOException {
        StringBuilder token = new StringBuilder();

        do {
            token.appendCodePoint(in.read());
        } while (isIdentifierChar(in.peek(0)));

        return token.toString();
    }

    /**
     * Reads identifier or identifier-like tokens (i.e. keywords).
     * @return the keyword or identifier token at the input stream's current position
     * @throws IOException when an IO exception occurs
     */
    private ODDLToken readIdentifierOrKeyword() throws IOException {
        String str = readIdentifierText();

        switch (str) {
            case "null":  return NameToken.NULL;
            case "true":  return BoolToken.TRUE;
            case "false": return BoolToken.FALSE;
            default:
                ODDLToken ret = DataTypeToken.create(str);
                if (ret==null) {
                    ret = new IdentifierToken(str);
                }
                return ret;
        }
    }

    private ODDLToken readNumberLiteral() throws IOException {
        StringBuilder text = new StringBuilder();

        // consume sign, if present
        if (in.peek(0) == '+' || in.peek(0) == '-') {
            text.appendCodePoint(in.read());
        }

        if (in.peek(0) == -1) {
            throw new UnexpectedEOFException(in, "number literal");
        }

        if (in.peek(0)=='0') { // could be decimal, hex, octal, binary
            int n = in.peek(1);
            switch (n) {
                case 'x':
                case 'X':
                    return readHexLiteral(text);
                case 'o':
                case 'O':
                    return readOctalLiteral(text);
                case 'b':
                case 'B':
                    return readBinaryLiteral(text);
            }
        } else if (in.peek(0)=='\'') {
            return readCharLiteral(text);
        }

        return readDecimalLiteral(text);
    }

    private ODDLToken readCharLiteral(StringBuilder text) throws IOException {
        // copy sign, if present
        StringBuilder value = new StringBuilder().append(text);

        // consume leading quote
        readSingleQuote(text);

        while (isCharLiteralCharacter(in.peek(0))) {
            if (in.peek(0)=='\\') {
                readEscapeChar(text, value);
            } else {
                value.appendCodePoint(in.peek(0));
                text.appendCodePoint(in.read());
            }
        }
        // consume trailing quote
        readSingleQuote(text);

        return new IntToken(text.toString(), value.toString(), IntToken.Format.CHAR);
    }

    private IntToken readBinaryLiteral(StringBuilder text) throws IOException {
        // omit prefix from value string
        StringBuilder value = new StringBuilder().append(text.toString());

        // consume 0b or 0B prefix
        text.appendCodePoint(in.read());
        text.appendCodePoint(in.read());

        if (in.peek(0) == -1) {
            throw new UnexpectedEOFException(in, "binary literal");
        }

        // consume binary digits
        int c;
        while ((c=in.peek(0))=='0' || c=='1' || c=='_') {
            if (c!='_') {
                value.appendCodePoint(in.peek(0));
            }
            text.appendCodePoint(in.read());
        }
        requireValidNumberTerminator("binary");

        return new IntToken(text.toString(), value.toString(), IntToken.Format.BIN);
    }

    private ODDLToken readOctalLiteral(StringBuilder text) throws IOException {
        // omit prefix from value string
        StringBuilder value = new StringBuilder().append(text.toString());

        // consume 0o or 0O prefix
        text.appendCodePoint(in.read());
        text.appendCodePoint(in.read());

        if (in.peek(0) == -1) {
            throw new UnexpectedEOFException(in, "octal literal");
        }

        // consume octal digits
        int c;
        while (((c=in.peek(0))>='0' && c <= '7') || c=='_') {
            if (c!='_') {
                value.appendCodePoint(in.peek(0));
            }
            text.appendCodePoint(in.read());
        }

        requireValidNumberTerminator("octal");

        return new IntToken(text.toString(), value.toString(), IntToken.Format.OCT);
    }

    private ODDLToken readHexLiteral(StringBuilder text) throws IOException {
        // omit prefix from value string
        StringBuilder value = new StringBuilder().append(text.toString());

        // consume 0o or 0O prefix
        text.appendCodePoint(in.read());
        text.appendCodePoint(in.read());

        if (in.peek(0) == -1) {
            throw new UnexpectedEOFException(in, "hex literal");
        }

        // consume hex digits
        int c;
        while (isHexCharacter(c=in.peek(0)) || c=='_') {
            if (c!='_') {
                value.appendCodePoint(c);
            }
            text.appendCodePoint(in.read());
        }

        requireValidNumberTerminator("hex");

        return new IntToken(text.toString(), value.toString(), IntToken.Format.HEX);
    }

    private ODDLToken readDecimalLiteral(StringBuilder text) throws IOException {
        StringBuilder value = new StringBuilder().append(text);

        readDigits(text, value);

        boolean integer = true;

        if (in.peek(0)=='.') {
            integer = false;

            text.appendCodePoint(in.peek(0));
            value.appendCodePoint(in.read());

            readDigits(text, value);
        }

        if (in.peek(0)=='e' || in.peek(0)=='E') { // read optional exponent, if present
            integer = false;

            text.appendCodePoint(in.peek(0));
            value.appendCodePoint(in.read());

            if (in.peek(0)=='+' || in.peek(0)=='-') {
                text.appendCodePoint(in.peek(0));
                value.appendCodePoint(in.read());
            }

            readDigits(text, value);
        }

        requireValidNumberTerminator("decimal");

        return integer
            ? new IntToken(text.toString(), value.toString(), IntToken.Format.DEC)
            : new FloatToken(text.toString(), value.toString());
    }

    private void readDigits(StringBuilder text, StringBuilder value) throws IOException {
        int c;
        while (Character.isDigit(c=in.peek(0)) || c=='_') {
            if (c!='_') {
                value.appendCodePoint(c);
            }
            text.appendCodePoint(in.read());
        }
    }

    private void requireValidNumberTerminator(String type) throws IOException {
        int c = in.peek(0);
        if (!isWhitespace(c) && !DelimiterToken.isDelimiterCharacter(c)) {
            throw new UnexpectedCharacterException(in, "in "+type+" literal", c);
        }
    }

    private ODDLToken readStringLiteral() throws IOException {
        StringBuilder text  = new StringBuilder();
        StringBuilder value = new StringBuilder();

        readDoubleQuote(text);

        int c;
        while ((c=in.peek(0))!='"') {
            if (c==-1) {
                throw new UnexpectedEOFException(in, "string literal");
            } else if (c=='\\') {
                readStringEscape(text, value);
            } else {
                if (!isStringLiteralCharacter(c)) {
                    throw new UnexpectedCharacterException(in, "string literal", c);
                }
                value.appendCodePoint(c);
                text.appendCodePoint(in.read());
            }
        }

        readDoubleQuote(text);

        return new StringToken(text.toString(), value.toString());
    }

    private void readStringEscape(StringBuilder text, StringBuilder value) throws IOException {
        if (in.peek(1)=='u') {
            text.appendCodePoint(in.read()); // slash
            text.appendCodePoint(in.read()); // u
            int uc16 =
                (readHexDigit(text) << 12) |
                (readHexDigit(text) << 8) |
                (readHexDigit(text) << 4) |
                readHexDigit(text);
            value.appendCodePoint(uc16);
        } else if (in.peek(1)=='U') {
            text.appendCodePoint(in.read()); // slash
            text.appendCodePoint(in.read()); // U
            int uc24 =
                (readHexDigit(text) << 20) |
                (readHexDigit(text) << 16) |
                (readHexDigit(text) << 12) |
                (readHexDigit(text) << 8) |
                (readHexDigit(text) << 4) |
                readHexDigit(text);
            value.appendCodePoint(uc24);
        } else {
            readEscapeChar(text, value);
        }
    }

    private void readEscapeChar(StringBuilder text, StringBuilder value) throws IOException {
        text.appendCodePoint(in.read()); // slash
        int c = in.read();
        text.appendCodePoint(c);
        switch (c) {
            case '\"':
            case '\'':
            case '?':
            case '\\':
                value.appendCodePoint(c);
                break;
            case 'a':
                value.appendCodePoint(0x22);
                break;
            case 'b':
                value.append('\b');
                break;
            case 'f':
                value.append('\f');
                break;
            case 'n':
                value.append('\n');
                break;
            case 'r':
                value.append('\r');
                break;
            case 't':
                value.append('\t');
                break;
            case 'v':
                value.appendCodePoint(0x0b);
                break;
            case 'x':
                int hi = readHexDigit(text);
                int lo = readHexDigit(text);
                value.appendCodePoint((hi << 4) | lo);
                break;
            default:
                throw new UnexpectedCharacterException(in, "escape character", c);
        }
    }

    private int readHexDigit(StringBuilder text) throws IOException {
        int c = in.read();
        text.appendCodePoint(c);
        int ret = Character.digit(c, 16);
        if (ret<0) {
            throw new UnexpectedCharacterException(in, c);
        }
        return ret;
    }

    private void consumeSingleLineComment() throws IOException {
        // consume "//"
        in.read();
        in.read();
        int c;
        while ((c=in.read())!=-1 && c!='\n'); // nada
    }

    private void consumeBlockComment() throws IOException {
        // consume "/*"
        in.read();
        in.read();
        int c;
        while ((c=in.read())!=-1) {
            // end at "*/"
            if (c=='*' && in.peek(0)=='/') {
                in.read(); // consume trailing slash
                return;
            }
        }
    }

    private void readSingleQuote(StringBuilder text) throws IOException {
        int c = in.peek(0);
        if (c!='\'') {
            throw new UnexpectedCharacterException(in, "character literal", c);
        }
        text.appendCodePoint(in.read());
    }

    private void readDoubleQuote(StringBuilder text) throws IOException {
        int c = in.peek(0);
        if (c!='"') {
            throw new UnexpectedCharacterException(in, "string literal", c);
        }
        text.appendCodePoint(in.read());
    }

    private static boolean isWhitespace(int c) {
        return c>=1 && c<=32;
    }

    private boolean isHexCharacter(int c) {
        return (c>='0' && c <= '9') || (c>='a' && c<='f') || (c>='A' && c<='F');
    }

    private boolean isCharLiteralCharacter(int c) {
        return c=='\\' || (c>=0x20 && c<=0x26) || (c>=0x28 && c<0x5b) || (c>=0x5d && c<=0x7e);
    }

    private boolean isStringLiteralCharacter(int c) {
        return (c>=0x20   && c<=0x21)   || (c>=0x23     && c<=0x5b   ) ||
               (c>=0x5d   && c<=0x7e)   || (c>=0xa0     && c<=0xd7ff ) ||
               (c>=0xe000 && c<=0xfffd) || (c>=0x010000 && c<0x10ffff);
    }
}
