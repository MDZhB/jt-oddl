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
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

/**
 * Parses the contents of an input stream as OpenDDL text, notifying an {@link ODDLListener} of all identified language
 * constructs in the order the reader encounters them.
 *
 * @author Nikita Leonidov
 */
public class ODDLReader {

    private final ODDLTokenizer tokenizer;

    public ODDLReader(ODDLInputStream in) {
        this.tokenizer = new ODDLTokenizer(in);
    }

    public ODDLReader(InputStream in) {
        this(new ODDLInputStream(in));
    }

    public ODDLReader(Reader in) {
        this(new ODDLInputStream(in));
    }

    /**
     * Reads a text file from the wrapped input stream, parsing it as an OpenDDL file. The given <tt>listener</tt> will
     * be notified of all identified language constructs in the order that they are encountered in the input text.
     *
     * @param listener  an object to which the reader will pass all parsed language constructs
     * @param <T>       the type of the result produced by the listener
     *
     * @return the object returned by <tt>listener</tt>'s {@link ODDLListener#end()} method.
     *
     * @throws IOException         when an IO exception occurs
     * @throws ODDLParseException  when the text read from the input stream does not conform to the OpenDDL grammar
     */
    public <T> T read(ODDLListener<T> listener) throws IOException, ODDLParseException {
        listener.begin();
        while (tokenizer.peek(0) != DelimiterToken.EOF) {
            tryReadStructure(listener, false);
        }
        return listener.end();
    }

    private void tryReadStructure(ODDLListener<?> listener, boolean nested) throws IOException, ODDLParseException {
        ODDLToken token = tokenizer.peek(0);

        switch (token.getType()) {
            case DATA_TYPE:
                readListStructure(listener);
                break;
            case IDENTIFIER:
                readCustomStructure(listener);
                break;
            case DELIMITER:
                if (token == DelimiterToken.EOF) {
                    return; // end reading here, else fallthrough to exception
                }
            default:
                if (!nested || token != DelimiterToken.RBRACE) {
                    throw new UnexpectedTokenException(
                            tokenizer.getLine(0), tokenizer.getCol(0),
                            token,
                            IdentifierToken.class, DataTypeToken.class
                    );
                }
        }
    }

    private void readListStructure(ODDLListener<?> listener) throws IOException, ODDLParseException {
        final DataTypeToken dataType = tokenizer.read(DataTypeToken.class);

        // this is a data-array-list iff a subarray size is specified
        //--------------------------------------------------------------------------------------------------------------
        final int subarraySize;
        if (tokenizer.peek(0) == DelimiterToken.LSQUARE) {
            tokenizer.read(); // consume lsquare
            subarraySize = (int) tokenizer.read(IntToken.class).getValue();
            tokenizer.read(DelimiterToken.RSQUARE);
        } else {
            subarraySize = -1;
        }

        // the name is optional & follows the subarray size
        //--------------------------------------------------------------------------------------------------------------
        final NameToken name;
        if (tokenizer.peek(0).isName()) {
            name = tokenizer.read().asName();
        } else {
            name = null;
        }

        // data-array-list iff subarray size set, else data-list
        //--------------------------------------------------------------------------------------------------------------
        tokenizer.read(DelimiterToken.LBRACE);
        if (subarraySize >= 0) {
            listener.beginArrayListStructure(dataType, subarraySize, name);
            readDataArrayList(dataType, subarraySize, listener);
            listener.endArrayListStructure(dataType, subarraySize, name);
        } else {
            listener.beginListStructure(dataType, name);
            readDataList(dataType, -1, listener);
            listener.endListStructure(dataType, name);
        }
        tokenizer.read(DelimiterToken.RBRACE);
    }

    private void readCustomStructure(ODDLListener<?> listener) throws IOException, ODDLParseException {
        final IdentifierToken identifier = tokenizer.read(IdentifierToken.class);

        // the name is optional
        //--------------------------------------------------------------------------------------------------------------
        final NameToken name;
        if (tokenizer.peek(0).isName()) {
            name = tokenizer.read().asName();
        } else {
            name = null;
        }

        // the property list is optional
        //--------------------------------------------------------------------------------------------------------------
        final PropertyMap properties;
        if (tokenizer.peek(0) == DelimiterToken.LPAREN) {
            Map<IdentifierToken, PropertyValueToken> props = new HashMap<>();
            tokenizer.read(); // consume lparen

            while (tokenizer.peek(0).isIdentifier()) {
                // lhs is property identifier, rhs is property value; separated by =
                IdentifierToken left = tokenizer.read(IdentifierToken.class);
                tokenizer.read(DelimiterToken.EQUALS);
                PropertyValueToken right;

                // strings, refs need special handling
                switch (tokenizer.peek(0).getType()) {
                    case STRING:
                        right = readString(false);
                        break;
                    case NAME:
                        right = readRef(false);
                        break;
                    default:
                        right = tokenizer.read(PropertyValueToken.class);
                }

                // finally, store property
                props.put(left, right);

                // properties separated by commas; if no comma, expect rparen as property list terminator
                if (tokenizer.peek(0) == DelimiterToken.COMMA) {
                    tokenizer.read();
                } else {
                    break;
                }
            }
            tokenizer.read(DelimiterToken.RPAREN); // consume property list terminator
            properties = new PropertyMap(props);
        } else {
            properties = PropertyMap.empty();
        }

        // pass the structure to the listener, then read any nested structures
        //--------------------------------------------------------------------------------------------------------------
        tokenizer.read(DelimiterToken.LBRACE);
        listener.beginCustomStructure(identifier, name, properties);

        while (tokenizer.peek(0)!=DelimiterToken.RBRACE) {
            tryReadStructure(listener, true);
        }

        tokenizer.read(DelimiterToken.RBRACE);
        listener.endCustomStructure(identifier, name, properties);
    }

    private void readDataList(DataTypeToken dataType, final int subarraySize, ODDLListener<?> listener) throws IOException, ODDLParseException {
        final Class<? extends ODDLToken> type = dataType.getTokenType();

        // skip list if empty
        if (tokenizer.peek(0) == DelimiterToken.RBRACE) {
            return;
        }

        int line = tokenizer.getLine(0);
        int row  = tokenizer.getCol(0);

        int count = 0;

        if (type == FloatToken.class) {
            do {
                listener.value(readFloatListElement());
                count++;
            } while (tokenizer.consumeIfPresent(DelimiterToken.COMMA));

        } else if (type == IntToken.class) {
            do {
                listener.value(readListElement(IntToken.class));
                count++;
            } while (tokenizer.consumeIfPresent(DelimiterToken.COMMA));

        } else if (type == StringToken.class) {
            do {
                listener.value(readString(true));
                count++;
            } while (tokenizer.consumeIfPresent(DelimiterToken.COMMA));

        } else if (type == RefToken.class) {
            do {
                listener.value(readRef(true));
                count++;
            } while (tokenizer.consumeIfPresent(DelimiterToken.COMMA));

        } else if (type == DataTypeToken.class) {
            do {
                listener.value(readListElement(DataTypeToken.class));
                count++;
            } while (tokenizer.consumeIfPresent(DelimiterToken.COMMA));

        } else if (type == BoolToken.class) {
            do {
                listener.value(readListElement(BoolToken.class));
                count++;
            } while (tokenizer.consumeIfPresent(DelimiterToken.COMMA));

        } else {
            throw new IllegalArgumentException(dataType.toString());
        }

        // enforce subarray size; unbounded if < 0
        if (subarraySize>=0 && count!=subarraySize) {
            throw new IllegalSubarraySizeException(line, row, count, subarraySize);
        }
    }

    private void readDataArrayList(DataTypeToken dataType, final int subarraySize, ODDLListener<?> listener) throws IOException, ODDLParseException {
        do {
            tokenizer.read(DelimiterToken.LBRACE);
            listener.beginSubArray(dataType, subarraySize);

            readDataList(dataType, subarraySize, listener);

            tokenizer.read(DelimiterToken.RBRACE);
            listener.endSubArray(dataType, subarraySize);
        } while (tokenizer.consumeIfPresent(DelimiterToken.COMMA));
    }

    private <T extends ODDLToken> T readListElement(Class<T> type) throws IOException, ODDLParseException {
        if (tokenizer.peek(0).getClass()!=type) {
            throw new ListElementTypeMismatchException(tokenizer.getLine(0), tokenizer.getCol(0), tokenizer.peek(0), type);
        }

        return type.cast(tokenizer.read());
    }

    private FloatToken readFloatListElement() throws IOException, ODDLParseException {
        if (!tokenizer.peek(0).isFloat() && !tokenizer.peek(0).isInt()) {
            throw new ListElementTypeMismatchException(tokenizer.getLine(0), tokenizer.getCol(0), tokenizer.peek(0), FloatToken.class);
        }

        ODDLToken token = tokenizer.read();
        if (token.isInt()) {
            return new FloatToken(token.getText(), token.asInt().getValue());
        } else {
            return token.asFloat();
        }
    }

    private RefToken readRef(boolean list) throws IOException, ODDLParseException {
        if (list && !tokenizer.peek(0).isName()) {
            throw new ListElementTypeMismatchException(tokenizer.getLine(0), tokenizer.getCol(0), tokenizer.peek(0), NameToken.class);
        }

        if (tokenizer.peek(0) == NameToken.NULL) {
            tokenizer.read();
            return new RefToken();
        } else {
            List<NameToken> names = new ArrayList<>();
            names.add(tokenizer.read(NameToken.class));

            // concatenate all consecutive names into one reference token
            while (tokenizer.peek(0).isName()) {
                // only the first name may be global ($); subsequent names must be local (%)
                if (tokenizer.peek(0).asName().isGlobal()) {
                    throw new UnexpectedTokenException(
                            tokenizer.getLine(0),
                            tokenizer.getCol(0),
                            "global "+NameToken.class.getSimpleName(),
                            "local  "+NameToken.class.getSimpleName()
                    );
                }

                names.add(tokenizer.read().asName());
            }

            return new RefToken(names);
        }
    }

    private StringToken readString(boolean list) throws IOException, ODDLParseException {
        if (list && !tokenizer.peek(0).isString()) {
            throw new ListElementTypeMismatchException(tokenizer.getLine(0), tokenizer.getCol(0), tokenizer.peek(0), NameToken.class);
        }

        StringToken ret = tokenizer.read().asString();

        // concatenate all following strings with this one
        if (tokenizer.peek(0).isString()) {
            StringBuilder text  = new StringBuilder();
            StringBuilder value = new StringBuilder();

            text.append(ret.getText());
            value.append(ret.getValue());

            while (tokenizer.peek(0).isString()) {
                StringToken next = tokenizer.read().asString();
                text.append(next.getText());
                value.append(next.getValue());
            }

            ret = new StringToken(text.toString(), value.toString());
        }

        return ret;
    }
}
