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
 * A token representing a data type keyword.
 *
 * @author Nikita Leonidov
 */
public final class DataTypeToken extends AbstractODDLToken implements PropertyValueToken {

    private static final int
            U = 1 << 0,
            // 8-64 reserved for bits
            I = 1 << 7,
            F = 1 << 8,
            B = 1 << 9,
            S = 1 << 10,
            R = 1 << 11,
            T = 1 << 12;

    private final DataType type;
    private final int bits;

    private DataTypeToken(String text, int bits) {
        super(text);
        this.bits = bits;
        int ordinal = Integer.numberOfTrailingZeros((bits&~0x79)>>>7);
        type = DataType.get(ordinal);
    }

    public boolean isTypeUnsigned() {
        return (bits&U)!=0;
    }

    public int getTypeBits() {
        return bits&0x78;
    }

    public DataType getValue() {
        return type;
    }

    @Override
    public Object getValueAsObject() {
        return getValue();
    }

    @Override
    public Type getType() {
        return Type.DATA_TYPE;
    }

    static DataTypeToken create(String text) {
        int bits = 0;
        switch (text) {
            // long name                   | short name(s)       | data-type token
            //-----------------------------+---------------------+------------------------------------------------------
            case "bool":                    case "b":             bits = B; break;
            //
            case "int8":                    case "i8":            bits = I|8; break;
            case "int16":                   case "i16":           bits = I|16; break;
            case "int32":                   case "i32":           bits = I|32; break;
            case "int64":                   case "i64":           bits = I|64; break;
            //
            case "unsigned_int8":           case "u8":            bits = U|I|8; break;
            case "unsigned_int16":          case "u16":           bits = U|I|16; break;
            case "unsigned_int32":          case "u32":           bits = U|I|32; break;
            case "unsigned_int64":          case "u64":           bits = U|I|64; break;
            //
            case "half":   case "float16":  case "h": case "f16": bits = F|16; break;
            case "float":  case "float32":  case "f": case "f32": bits = F|32; break;
            case "double": case "float64":  case "d": case "f64": bits = F|64; break;
            //
            case "string":                  case "s":             bits = S; break;
            case "ref":                     case "r":             bits = R; break;
            case "type":                    case "t":             bits = T; break;

            default: return null;
        }

        return new DataTypeToken(text, bits);
    }
}
