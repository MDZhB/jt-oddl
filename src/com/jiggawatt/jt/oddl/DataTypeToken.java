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

    public static final DataTypeToken BOOL = new DataTypeToken("bool", BoolToken.class);

    public static final DataTypeToken INT8  = new DataTypeToken("int8",  IntToken.class);
    public static final DataTypeToken INT16 = new DataTypeToken("int16", IntToken.class);
    public static final DataTypeToken INT32 = new DataTypeToken("int32", IntToken.class);
    public static final DataTypeToken INT64 = new DataTypeToken("int64", IntToken.class);

    public static final DataTypeToken UINT8  = new DataTypeToken("unsigned_int8",  IntToken.class);
    public static final DataTypeToken UINT16 = new DataTypeToken("unsigned_int16", IntToken.class);
    public static final DataTypeToken UINT32 = new DataTypeToken("unsigned_int32", IntToken.class);
    public static final DataTypeToken UINT64 = new DataTypeToken("unsigned_int64", IntToken.class);

    public static final DataTypeToken HALF   = new DataTypeToken("half",   FloatToken.class);
    public static final DataTypeToken FLOAT  = new DataTypeToken("float",  FloatToken.class);
    public static final DataTypeToken DOUBLE = new DataTypeToken("double", FloatToken.class);

    public static final DataTypeToken STRING = new DataTypeToken("string", StringToken.class);
    public static final DataTypeToken REF    = new DataTypeToken("ref",    RefToken.class);
    public static final DataTypeToken TYPE   = new DataTypeToken("type",   DataTypeToken.class);

    private final Class<? extends ODDLToken> tokenType;

    private DataTypeToken(String text, Class<? extends ODDLToken> tokenType) {
        super(text);
        this.tokenType = tokenType;
    }

    /**
     * @return the {@link ODDLToken} subclass that represents literals of this data type
     */
    public Class<? extends ODDLToken> getTokenType() {
        return tokenType;
    }

    @Override
    public Object getValueAsObject() {
        return getTokenType();
    }

    static DataTypeToken create(String text) {
        switch (text) {
            // long name                   | short name(s)       | data-type token
            //-----------------------------+---------------------+------------------------------------------------------
            case "bool":                    case "b":             return BOOL;
            //
            case "int8":                    case "i8":            return INT8;
            case "int16":                   case "i16":           return INT16;
            case "int32":                   case "i32":           return INT32;
            case "int64":                   case "i64":           return INT64;
            //
            case "unsigned_int8":           case "u8":            return UINT8;
            case "unsigned_int16":          case "u16":           return UINT16;
            case "unsigned_int32":          case "u32":           return UINT32;
            case "unsigned_int64":          case "u64":           return UINT64;
            //
            case "half":   case "float16":  case "h": case "f16": return HALF;
            case "float":  case "float32":  case "f": case "f32": return FLOAT;
            case "double": case "float64":  case "d": case "f64": return DOUBLE;
            //
            case "string":                  case "s":             return STRING;
            case "ref":                     case "r":             return REF;
            case "type":                    case "t":             return TYPE;

            default: return null;
        }
    }

    @Override
    public Type getType() {
        return Type.DATA_TYPE;
    }
}
