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

import java.util.Map;

/**
 * An interface for classes that consume OpenDDL constructs identified by an {@link ODDLReader}. Instead of generating a
 * data structure to represent the fully parsed file, the reader calls the appropriate listener methods as it parses its
 * input.
 *
 * @author Nikita Leonidov
 */
public interface ODDLListener<T> {

    /**
     * Called by the owning {@link ODDLReader} at the beginning of the document.
     */
    void begin();

    /**
     * Called by the owning {@link ODDLReader} at the end of the document.
     * @return a result that will be returned by {@link ODDLReader#read(ODDLListener)}
     */
    T end();

    /**
     * Called by the owning {@link ODDLReader} when it encounters a boolean literal within a list structure.
     * @param value a boolean literal parsed by the reader
     */
    void value(BoolToken value);

    /**
     * Called by the owning {@link ODDLReader} when it encounters an integer literal within a list structure.
     * @param value an integer literal parsed by the reader
     */
    void value(IntToken value);

    /**
     * Called by the owning {@link ODDLReader} when it encounters a float literal within a list structure.
     * @param value a float literal parsed by the reader
     */
    void value(FloatToken value);

    /**
     * Called by the owning {@link ODDLReader} when it encounters one or more consecutive string literals within a list
     * structure.
     * @param value a (possibly concatenated) string parsed by the reader
     */
    void value(StringToken value);

    /**
     * Called by the owning {@link ODDLReader} when it encounters a reference within a list structure.
     * @param value a reference parsed by the reader
     */
    void value(RefToken value);

    /**
     * Called by the owning {@link ODDLReader} when it encounters a data type keyword within a list structure.
     * @param value a data type parsed by the reader
     */
    void value(DataTypeToken value);

    /**
     * Called by the owning {@link ODDLReader} when it encounters the beginning of a data list structure.
     * @param dataType the type of the elements stored in this list
     * @param name     the name specified for this list; <tt>null</tt> if none
     */
    void beginListStructure(DataTypeToken dataType, NameToken name);

    /**
     * Called by the owning {@link ODDLReader} when it encounters the end of a data list structure.
     * @param dataType the type of the elements stored in this list
     * @param name     the name specified for this list; <tt>null</tt> if none
     */
    void endListStructure(DataTypeToken dataType, NameToken name);

    /**
     * Called by the owning {@link ODDLReader} when it encounters the beginning of a data array list structure.
     * @param dataType      the type of the elements stored in this array list
     * @param subarraySize  the number of elements in this list's sub-arrays
     * @param name          an optional name by which other structures can refer to this one, or <tt>null</tt> if none
     *                      was specified
     */
    void beginArrayListStructure(DataTypeToken dataType, int subarraySize, NameToken name);

    /**
     * Called by the owning {@link ODDLReader} when it encounters the end of a data array list structure.
     * @param dataType      the type of the elements stored in this array list
     * @param subarraySize  the number of elements in this list's sub-arrays
     * @param name          an optional name by which other structures can refer to this one, or <tt>null</tt> if none
     *                      was specified
     */
    void endArrayListStructure(DataTypeToken dataType, int subarraySize, NameToken name);

    /**
     * Called by the owning {@link ODDLReader} when it encounters the beginning of an array list structure's sub-array.
     * @param dataType      the type of the elements stored in this sub-array
     * @param subarraySize  the number of elements in this sub-array
     */
    void beginSubArray(DataTypeToken dataType, int subarraySize);

    /**
     * Called by the owning {@link ODDLReader} when it encounters the end of an array list structure's sub-array.
     * @param dataType      the type of the elements stored in this sub-array
     * @param subarraySize  the number of elements in this sub-array
     */
    void endSubArray(DataTypeToken dataType, int subarraySize);

    /**
     * Called by the owning {@link ODDLReader} when it encoutners the beginning of a custom data structure.
     * @param identifier   the custom data type's name
     * @param name        an optional name by which other structures can refer to this one, or <tt>null</tt> if none was
     *                    specified
     * @param properties  the contents of the structure's property list
     */
    void beginCustomStructure(IdentifierToken identifier, NameToken name, PropertyMap properties);

    /**
     * Called by the owning {@link ODDLReader} when it encoutners the end of a custom data structure.
     * @param identifier  the custom data type's name
     * @param name        an optional name by which other structures can refer to this one, or <tt>null</tt> if none was
     *                    specified
     * @param properties  the contents of the structure's property list
     */
    void endCustomStructure(IdentifierToken identifier, NameToken name, PropertyMap properties);
}
