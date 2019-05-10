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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.function.Predicate;

import static org.junit.Assert.*;

public class ODDLTokenizerTest {

    // eof
    //==================================================================================================================
    @Test
    public void readEofToken() throws IOException {
        final String text = "";
        assertTrue(readToken(text).isEOF());
    }

    // comments
    //==================================================================================================================
    @Test
    public void ignoreSingleLineComments() throws IOException {
        final String text = "// nothing here";
        assertTrue(readToken(text).isEOF());
    }

    @Test
    public void ignoreBlockComments() throws IOException {
        final String text =
                "/* Line one \n " +
                " * Line two \n " +
                " * Line three */";

        assertTrue(readToken(text).isEOF());
    }

    // identifiers
    //==================================================================================================================
    @Test
    public void identifyIdentifierToken() throws IOException {
        final String text = "ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz_0123456789";
        ODDLToken token = readToken(text);
        assertTrue(token.isIdentifier());
    }

    @Test
    public void readIdentifierToken() throws IOException {
        final String text = "ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz_0123456789";
        IdentifierToken token = readToken(text).asIdentifier();
        assertEquals(text, token.getText());
    }

    @Test(expected=UnexpectedCharacterException.class)
    public void identifyUnexpectedIdentifierCharacter() throws IOException {
        final String text = "ABCDEFG_abcdefg^0123456789";
        ODDLTokenizer lexer = getTokenizer(text);
        lexer.read();
        lexer.read();
    }

    @Test
    public void identifyIdentifierTokenFollowingSingleLineComment() throws IOException {
        final String text = "// nothing here\nABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz_0123456789";
        assertTrue(readToken(text).isIdentifier());
    }

    @Test
    public void readIdentifierTokenFollowingSingleLineComment() throws IOException {
        final String ident = "ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz_0123456789";
        final String text  = "// nothing here\n"+ident;
        assertEquals(ident, readToken(text).asIdentifier().getText());
    }

    // bools
    //==================================================================================================================
    @Test
    public void identifyBoolLiteral() throws IOException {
        assertTrue (readToken("true").asBool().getValue());
        assertFalse(readToken("false").asBool().getValue());
    }

    @Test
    public void readBoolLiteral() throws IOException {
        assertTrue(readToken("true").asBool().getValue());
        assertFalse(readToken("false").asBool().getValue());
    }

    // ints
    //==================================================================================================================
    // hex
    //------------------------------------------------------------------------------------------------------------------
    @Test
    public void identifyHexLiteral() throws IOException {
        identifyIntLiteral("0x41_42_43_44", IntToken.Format.HEX);
        identifyIntLiteral("0X41_42_43_44", IntToken.Format.HEX);
    }

    @Test
    public void readHexLiteral() throws IOException {
        readIntLiteral("0x41_42_43_44", 1094861636);
        readIntLiteral("0X41_42_43_44", 1094861636);
    }

    @Test
    public void identifyPositiveHexLiteral() throws IOException {
        identifyIntLiteral("+0x41_42_43_44", IntToken.Format.HEX);
        identifyIntLiteral("+0X41_42_43_44", IntToken.Format.HEX);
    }

    @Test
    public void readPositiveHexLiteral() throws IOException {
        readIntLiteral("+0x41_42_43_44", +1094861636);
        readIntLiteral("+0X41_42_43_44", +1094861636);
    }

    @Test
    public void identifyNegativeHexLiteral() throws IOException {
        identifyIntLiteral("-0x41_42_43_44", IntToken.Format.HEX);
        identifyIntLiteral("-0X41_42_43_44", IntToken.Format.HEX);
    }

    @Test
    public void readNegativeHexLiteral() throws IOException {
        readIntLiteral("-0x41_42_43_44", -1094861636);
        readIntLiteral("-0X41_42_43_44", -1094861636);
    }
    // oct
    //------------------------------------------------------------------------------------------------------------------
    // NB: no "o" in java -- leading 0 = octal
    @Test
    public void identifyOctLiteral() throws IOException {
        identifyIntLiteral("0o101_2044_1504", IntToken.Format.OCT);
        identifyIntLiteral("0O101_2044_1504", IntToken.Format.OCT);
    }

    @Test
    public void readOctLiteral() throws IOException {
        readIntLiteral("0o101_2044_1504", 1094861636);
        readIntLiteral("0O101_2044_1504", 1094861636);
    }

    @Test
    public void identifyPositiveOctLiteral() throws IOException {
        identifyIntLiteral("+0o101_2044_1504", IntToken.Format.OCT);
        identifyIntLiteral("+0O101_2044_1504", IntToken.Format.OCT);
    }

    @Test
    public void readPositiveOctLiteral() throws IOException {
        readIntLiteral("+0o101_2044_1504", +1094861636);
        readIntLiteral("+0O101_2044_1504", +1094861636);
    }

    @Test
    public void identifyNegativeOctLiteral() throws IOException {
        identifyIntLiteral("-0o101_2044_1504", IntToken.Format.OCT);
        identifyIntLiteral("-0O101_2044_1504", IntToken.Format.OCT);
    }

    @Test
    public void readNegativeOctLiteral() throws IOException {
        readIntLiteral("-0o101_2044_1504", -1094861636);
        readIntLiteral("-0O101_2044_1504", -1094861636);
    }
    // bin
    //------------------------------------------------------------------------------------------------------------------
    @Test
    public void identifyBinLiteral() throws IOException {
        identifyIntLiteral("0b0100_0001_0100_0010_0100_0011_0100_0100", IntToken.Format.BIN);
        identifyIntLiteral("0B0100_0001_0100_0010_0100_0011_0100_0100", IntToken.Format.BIN);
    }

    @Test
    public void readBinLiteral() throws IOException {
        readIntLiteral("0b0100_0001_0100_0010_0100_0011_0100_0100", 1094861636);
        readIntLiteral("0B0100_0001_0100_0010_0100_0011_0100_0100", 1094861636);
    }

    @Test
    public void identifyPositiveBinLiteral() throws IOException {
        identifyIntLiteral("+0b0100_0001_0100_0010_0100_0011_0100_0100", IntToken.Format.BIN);
        identifyIntLiteral("+0B0100_0001_0100_0010_0100_0011_0100_0100", IntToken.Format.BIN);
    }

    @Test
    public void readPositiveBinLiteral() throws IOException {
        readIntLiteral("+0b0100_0001_0100_0010_0100_0011_0100_0100", +1094861636);
        readIntLiteral("+0B0100_0001_0100_0010_0100_0011_0100_0100", +1094861636);
    }

    @Test
    public void identifyNegativeBinLiteral() throws IOException {
        identifyIntLiteral("-0b0100_0001_0100_0010_0100_0011_0100_0100", IntToken.Format.BIN);
        identifyIntLiteral("-0B0100_0001_0100_0010_0100_0011_0100_0100", IntToken.Format.BIN);
    }

    @Test
    public void readNegativeBinLiteral() throws IOException {
        readIntLiteral("-0b0100_0001_0100_0010_0100_0011_0100_0100", -1094861636);
        readIntLiteral("-0B0100_0001_0100_0010_0100_0011_0100_0100", -1094861636);
    }
    // dec
    //------------------------------------------------------------------------------------------------------------------
    @Test
    public void identifyDecLiteral() throws IOException {
        identifyIntLiteral("1_094_861_636", IntToken.Format.DEC);
    }

    @Test
    public void readDecLiteral() throws IOException {
        readIntLiteral("1_094_861_636", 1094861636);
    }

    @Test
    public void identifyPositiveDecLiteral() throws IOException {
        identifyIntLiteral("+1_094_861_636", IntToken.Format.DEC);
    }

    @Test
    public void readPositiveDecLiteral() throws IOException {
        readIntLiteral("+1_094_861_636", +1094861636);
    }

    @Test
    public void identifyNegativeDecLiteral() throws IOException {
        identifyIntLiteral("-1_094_861_636", IntToken.Format.DEC);
    }

    @Test
    public void readNegativeDecLiteral() throws IOException {
        readIntLiteral("-1_094_861_636", -1094861636);
    }

    // char
    //------------------------------------------------------------------------------------------------------------------
    @Test
    public void identifyCharLiteral() throws IOException {
        identifyIntLiteral("'ABCD'", IntToken.Format.CHAR);
    }

    @Test
    public void readCharLiteral() throws IOException {
        readIntLiteral("'ABCD'", 1094861636);
    }

    @Test
    public void identifyPositiveCharLiteral() throws IOException {
        identifyIntLiteral("+'ABCD'", IntToken.Format.CHAR);
    }

    @Test
    public void readPositiveCharLiteral() throws IOException {
        readIntLiteral("+'ABCD'", +1094861636);
    }

    @Test
    public void identifyNegativeCharLiteral() throws IOException {
        identifyIntLiteral("-'ABCD'", IntToken.Format.CHAR);
    }

    @Test
    public void readNegativeCharLiteral() throws IOException {
        readIntLiteral("-'ABCD'", -1094861636);
    }

    // float
    //==================================================================================================================
    @Test
    public void identifyFloatLiteral() throws IOException {
        assertTrue(readToken("1_280.1_024").isFloat());
    }

    @Test
    public void readFloatLiteral() throws IOException {
        assertEquals(1280.1024, readToken("1_280.1_024").asFloat().getValue(), 0.0001);
    }

    @Test
    public void identifyLeadingPeriodFloatLiteral() throws IOException {
        assertTrue(readToken(".1_024").isFloat());
    }

    @Test
    public void readLeadingPeriodFloatLiteral() throws IOException {
        assertEquals(.1024, readToken(".1_024").asFloat().getValue(), 0.0001);
    }

    @Test
    public void identifyExponentFloatLiteral() throws IOException {
        assertTrue(readToken("1.234567E3").isFloat());
        assertTrue(readToken("1.234567E+3").isFloat());
        assertTrue(readToken("1.234567E-3").isFloat());
    }

    @Test
    public void readExponentFloatLiteral() throws IOException {
        assertEquals(1.234567E3,  readToken("1.234567E3").asFloat().getValue(),  0.0001);
        assertEquals(1.234567E+3, readToken("1.234567E+3").asFloat().getValue(), 0.0001);
        assertEquals(1.234567E-3, readToken("1.234567E-3").asFloat().getValue(), 0.0001);
    }

    // name
    //==================================================================================================================
    @Test
    public void readNullName() throws IOException {
        assertTrue(readToken("null").isNullName());
    }

    @Test
    public void identifyGlobalName() throws IOException {
        String text = "$some_name_0";
        ODDLToken token = readToken(text);
        assertTrue(token.isName());
        assertTrue(token.asName().isGlobal());
    }

    @Test
    public void readGlobalName() throws IOException {
        String text = "$some_name_0";
        ODDLToken token = readToken(text);
        assertEquals(text, token.getText());
        assertEquals("some_name_0", token.asName().getValue());
    }

    @Test
    public void identifyLocalName() throws IOException {
        String text = "%some_name_0";
        ODDLToken token = readToken(text);
        assertTrue(token.isName());
        assertFalse(token.asName().isGlobal());
    }

    @Test
    public void readLocalName() throws IOException {
        String text = "%some_name_0";
        ODDLToken token = readToken(text);
        assertEquals(text, token.getText());
        assertEquals("some_name_0", token.asName().getValue());
    }

    // data types
    //==================================================================================================================
    @Test
    public void readBoolType() throws IOException {
        assertDataType("b",    isType(DataType.BOOL));
        assertDataType("bool", isType(DataType.BOOL));
    }

    @Test
    public void readInt8Type() throws IOException {
        assertDataType("i8",   isInt(8));
        assertDataType("int8", isInt(8));
    }

    @Test
    public void readInt16Type() throws IOException {
        assertDataType("i16",   isInt(16));
        assertDataType("int16", isInt(16));
    }

    @Test
    public void readInt32Type() throws IOException {
        assertDataType("i32",   isInt(32));
        assertDataType("int32", isInt(32));
    }

    @Test
    public void readInt64Type() throws IOException {
        assertDataType("i64",   isInt(64));
        assertDataType("int64", isInt(64));
    }

    @Test
    public void readUInt8Type() throws IOException {
        assertDataType("u8",            isUint(8));
        assertDataType("unsigned_int8", isUint(8));
    }

    @Test
    public void readUInt16Type() throws IOException {
        assertDataType("u16",            isUint(16));
        assertDataType("unsigned_int16", isUint(16));
    }

    @Test
    public void readUInt32Type() throws IOException {
        assertDataType("u32",            isUint(32));
        assertDataType("unsigned_int32", isUint(32));
    }

    @Test
    public void readUInt64Type() throws IOException {
        assertDataType("u64",            isUint(64));
        assertDataType("unsigned_int64", isUint(64));
    }

    @Test
    public void readHalfType() throws IOException {
        assertDataType("h",       isFloat(16));
        assertDataType("f16",     isFloat(16));
        assertDataType("half",    isFloat(16));
        assertDataType("float16", isFloat(16));
    }

    @Test
    public void readFloatType() throws IOException {
        assertDataType("f",       isFloat(32));
        assertDataType("f32",     isFloat(32));
        assertDataType("float",   isFloat(32));
        assertDataType("float32", isFloat(32));
    }

    @Test
    public void readDoubleType() throws IOException {
        assertDataType("d",       isFloat(64));
        assertDataType("f64",     isFloat(64));
        assertDataType("double",  isFloat(64));
        assertDataType("float64", isFloat(64));
    }

    @Test
    public void readStringType() throws IOException {
        assertDataType("s",      isType(DataType.STRING));
        assertDataType("string", isType(DataType.STRING));
    }

    @Test
    public void readRefType() throws IOException {
        assertDataType("r",   isType(DataType.REF));
        assertDataType("ref", isType(DataType.REF));
    }

    @Test
    public void readLongType() throws IOException {
        assertDataType("t",    isType(DataType.TYPE));
        assertDataType("type", isType(DataType.TYPE));
    }

    // strings
    //==================================================================================================================
    @Test
    public void identifyString() throws IOException {
        assertTrue(readToken("\"ABCDEFG abcdefg 0123456789\"").isString());
    }

    @Test
    public void readString() throws IOException {
        String text = "\"ABCDEFG abcdefg 0123456789\"";
        assertEquals("ABCDEFG abcdefg 0123456789", readToken(text).asString().getValue());
        assertEquals("\"ABCDEFG abcdefg 0123456789\"", readToken(text).asString().getText());
    }

    @Test(expected=UnexpectedCharacterException.class)
    public void readIllegalString() throws IOException {
        readToken(new StringBuilder().appendCodePoint(0xfffe).toString());
    }

    @Test
    public void readEscapeCharacterString() throws IOException {
        assertEquals("\uD83D\uDC80", readToken("\"\\uD83D\\uDC80\"").asString().getValue());
    }

    // delimiters
    //==================================================================================================================
    @Test
    public void identifyDelimiters() throws IOException {
        testDelimiter('{');
        testDelimiter('}');
        testDelimiter('[');
        testDelimiter(']');
        testDelimiter('(');
        testDelimiter(')');
        testDelimiter(',');
        testDelimiter('=');
    }

    // helpers
    //==================================================================================================================
    private static ODDLTokenizer getTokenizer(String oddlText) {
        return new ODDLTokenizer(new ODDLInputStream(new ByteArrayInputStream(oddlText.getBytes())));
    }

    private static void readIntLiteral(String text, long value) throws IOException {
        ODDLToken token = readToken(text);
        assertEquals(value, token.asInt().getValue());
    }

    private static void identifyIntLiteral(String text, IntToken.Format format) throws IOException {
        ODDLToken token = readToken(text);
        assertTrue(token.isInt());
        assertEquals(format, token.asInt().getFormat());
    }

    private static ODDLToken readToken(String text) throws IOException {
        return getTokenizer(text).read();
    }

    private static void assertDataType(String text, Predicate<DataTypeToken> getter) throws IOException {
        ODDLToken tok = readToken(text);
        assertTrue(tok instanceof DataTypeToken);
        assertTrue(getter.test((DataTypeToken)tok));
    }

    private static void testDelimiter(char token) throws IOException {
        assertTrue(readToken(Character.toString(token)).isDelimiter(token));
    }

    private Predicate<DataTypeToken> isInt(final int bits) {
        return t->t.getValue()==DataType.INT && t.getTypeBits()==bits;
    }

    private Predicate<DataTypeToken> isUint(final int bits) {
        return t->t.isTypeUnsigned() && t.getValue()==DataType.INT && t.getTypeBits()==bits;
    }

    private Predicate<DataTypeToken> isFloat(final int bits) {
        return t->t.getValue()==DataType.FLOAT && t.getTypeBits()==bits;
    }

    private Predicate<DataTypeToken> isType(DataType type) {
        return t->t.getValue()==type;
    }
}
