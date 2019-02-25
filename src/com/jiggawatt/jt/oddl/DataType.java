package com.jiggawatt.jt.oddl;

public enum DataType {

    INT(IntToken.class),
    FLOAT  (FloatToken.class),
    BOOL   (BoolToken.class),
    STRING (StringToken.class),
    REF    (RefToken.class),
    TYPE   (DataTypeToken.class);

    private final Class<? extends ODDLToken> tokenType;
    private static final DataType[] VALUES = values();

    DataType(Class<? extends ODDLToken> t) {
        tokenType = t;
    }

    public static DataType get(int ordinal) {
        return VALUES[ordinal];
    }

    public Class<? extends ODDLToken> getTokenType() {
        return tokenType;
    }

}
