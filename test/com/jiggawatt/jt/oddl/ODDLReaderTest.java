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
import java.util.*;

import static org.junit.Assert.assertEquals;

public class ODDLReaderTest {

    // custom structures
    //==================================================================================================================
    @Test
    public void parseEmptyStructure() throws IOException, ODDLParseException, ODDLFormatException {
        Structure expect = struct("Empty");
        String text = "Empty {}";
        assertEquals(expect, parseSingle(text));
    }

    @Test
    public void parseCustomStructureName() throws IOException, ODDLParseException, ODDLFormatException {
        assertEquals("$foo", parseSingle("Empty $foo {}").name);
    }

    // list structures
    //==================================================================================================================
    @Test
    public void parseEmptyListStructure() throws IOException, ODDLParseException, ODDLFormatException {
        Structure expect = list("float", null);
        String text = "float {}";
        assertEquals(expect, parseSingle(text));
    }

    @Test
    public void parseListStructure() throws IOException, ODDLParseException, ODDLFormatException {
        Structure expect = list("float", null, 1.0, 2.0, 3.0, 4.0);
        String text = "float {1.0, 2.0, 3.0, 4.0}";
        assertEquals(expect, parseSingle(text));
    }

    @Test
    public void parseListStructureName() throws IOException, ODDLParseException, ODDLFormatException {
        assertEquals("$foo", parseSingle("float $foo {1.0, 2.0, 3.0}").name);
    }

    @Test
    public void parseArrayListStructure() throws IOException, ODDLParseException, ODDLFormatException {
        String text = "float [3] {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}}";
        assertEquals(List.of(List.of(1.0, 2.0, 3.0), List.of(4.0, 5.0, 6.0)), parseSingle(text).data);
    }

    @Test
    public void disambiguateListElementType() throws IOException, ODDLParseException, ODDLFormatException {
        // interpret int token as float for list of type float
        String text = "float {1.0, 2.0, 3}";
        assertEquals(List.of(1.0, 2.0, 3.0), parseSingle(text).data);
    }

    @Test
    public void parseConcatStringListElements() throws IOException, ODDLParseException, ODDLFormatException {
        String text = "string {\"foo\" \"bar\", \"baz\"}";
        assertEquals(List.of("foobar", "baz"), parseSingle(text).data);
    }

    @Test(expected=ListElementTypeMismatchException.class)
    public void failOnElementTypeMismatch() throws IOException, ODDLParseException, ODDLFormatException {
        parseSingle("float {1.0, 2.0, true}");
    }

    @Test(expected=IllegalSubarraySizeException.class)
    public void enforceArrayListSize() throws IOException, ODDLParseException, ODDLFormatException {
        parseSingle("float [3] {{1.0, 2.0, 3.0}, {4.0, 5.0}}");
    }

    @Test
    public void readsEmptyArrayList() throws ODDLParseException, ODDLFormatException, IOException {
        assertEquals(List.of(), parseSingle("float[3]{}").data);
    }

    @Test
    public void parseArrayListStructureName() throws IOException, ODDLParseException, ODDLFormatException {
        assertEquals("$foo", parseSingle("float [3] $foo {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}}").name);
    }

    // structure properties
    //==================================================================================================================
    @Test
    public void parseStructureIntProperty() throws IOException, ODDLParseException, ODDLFormatException {
        assertPropertiesEqual(
            Map.of("prop", 5L),
            "Props (prop=5) {}"
        );
    }

    @Test
    public void parseStructureFloatProperty() throws IOException, ODDLParseException, ODDLFormatException {
        assertPropertiesEqual(
            Map.of("prop", 5.0),
            "Props (prop=5.0) {}"
        );
    }

    @Test
    public void parseStructureBoolProperty() throws IOException, ODDLParseException, ODDLFormatException {
        assertPropertiesEqual(
            Map.of("prop", false),
            "Props (prop=false) {}"
        );
    }

    @Test
    public void parseStructureStringProperty() throws IOException, ODDLParseException, ODDLFormatException {
        assertPropertiesEqual(
            Map.of("prop", "foo"),
            "Props (prop=\"foo\") {}"
        );
    }

    @Test
    public void parseStructureStringConcatProperty() throws IOException, ODDLParseException, ODDLFormatException {
        assertPropertiesEqual(
            Map.of("prop", "foobar"),
            "Props (prop=\"foo\" \"bar\") {}"
        );
    }

    @Test
    public void parseStructureRefProperty() throws IOException, ODDLParseException, ODDLFormatException {
        assertPropertiesEqual(
            Map.of("prop", List.of("$foo","%bar", "%baz")),
            "Props (prop=$foo%bar%baz) {}"
        );
    }

    @Test
    public void parseStructureNullRefProperty() throws IOException, ODDLParseException, ODDLFormatException {
        Map<String, Object> expect = new HashMap<>();
        expect.put("prop", null);
        assertPropertiesEqual(expect, "Props (prop=null) {}");
    }

    @Test(expected=UnexpectedTokenException.class)
    public void structureRefPropertyRequiresLocalNames() throws IOException, ODDLParseException, ODDLFormatException {
        parseSingle("Props (prop=$foo$bar$baz");
    }

    @Test
    public void parsePropertyList() throws IOException, ODDLParseException, ODDLFormatException {
        assertPropertiesEqual(
            Map.of("foo", 1L, "bar", 2.0, "baz", "string", "qux", List.of("$name"), "quux", true),
            "Props (foo=1, bar=2.0, baz=\"string\", qux=$name, quux=true) {}"
        );
    }

    @Test
    public void parseEmptyPropertyList() throws IOException, ODDLParseException, ODDLFormatException {
        assertPropertiesEqual(
            Map.of(),
            "Props () {}"
        );
    }

    // child structures
    //==================================================================================================================
    @Test
    public void parseCustomChildStructure() throws IOException, ODDLParseException, ODDLFormatException {
        List<Structure> expect = List.of(
            struct("Child")
            .property("key", "value")
        );

        assertEquals(expect, parseSingle("Parent { Child (key=\"value\") {} }").children);
    }

    @Test
    public void parseMultipleCustomChildStructures() throws IOException, ODDLParseException, ODDLFormatException {
        List<Structure> expect = List.of(
            struct("Child").property("index", 0L),
            struct("Child").property("index", 1L),
            struct("Child").property("index", 2L)
        );

        assertEquals(expect, parseSingle("Parent { Child (index=0) {} Child (index=1) {} Child (index=2) {} }").children);
    }

    @Test
    public void parseListChildStructure() throws IOException, ODDLParseException, ODDLFormatException {
        List<Structure> expect = List.of(
                list("float", null, 0.0, 1.0, 2.0)
        );

        assertEquals(expect, parseSingle("Parent { float {0.0, 1.0, 2.0} }").children);
    }

    @Test
    public void parseMultipleListChildStructures() throws IOException, ODDLParseException, ODDLFormatException {
        List<Structure> expect = List.of(
            list("float", null, 0.0, 1.0, 2.0),
            list("int32", null, 3L,  4L,  5L ),
            list("bool",  null, true, false  )
        );

        assertEquals(expect, parseSingle("Parent { float {0.0, 1.0, 2.0} int32 {3, 4, 5} bool {true, false} }").children);
    }

    @Test(expected=ListElementTypeMismatchException.class)
    public void forbidChildStructureInList() throws IOException, ODDLParseException, ODDLFormatException {
        parseSingle("float { Child {}}");
    }

    // test helpers
    //==================================================================================================================
    private static List<Structure> parse(String text) throws IOException, ODDLParseException, ODDLFormatException {
        ODDLReader reader = new ODDLReader(new ByteArrayInputStream(text.getBytes()));
        TestListener listener = new TestListener();
        return reader.read(listener);
    }

    private static Structure parseSingle(String text) throws IOException, ODDLParseException, ODDLFormatException {
        List<Structure> ret = parse(text);
        assertEquals(1, ret.size());
        return ret.get(0);
    }

    private static void assertPropertiesEqual(Map<String, Object> expect, String text) throws IOException, ODDLParseException, ODDLFormatException {
        assertEquals(expect, parseSingle(text).properties);
    }

    private static final class TestListener implements ODDLListener<List<Structure>> {
        private final List<Structure> structures = new ArrayList<>();
        private final Deque<Structure> stack = new ArrayDeque<>();
        private List<Object> subArray;

        @Override
        public void begin() {

        }

        @Override
        public List<Structure> end(Position pos) {
            return structures;
        }

        @Override
        public void value(BoolToken value) {
            pushValue(value);
        }

        @Override
        public void value(IntToken value) {
            pushValue(value);
        }

        @Override
        public void value(FloatToken value) {
            pushValue(value);
        }

        @Override
        public void value(StringToken value) {
            pushValue(value);
        }

        @Override
        public void value(RefToken value) {
            pushValue(value);
        }

        @Override
        public void value(DataTypeToken value) {
            pushValue(value);
        }

        @Override
        public void beginListStructure(DataTypeToken dataType, NameToken name) {
            Structure struct = new Structure(null, dataType.getText(), name==null?null:name.getText());
            stack.push(struct);
        }

        @Override
        public void endListStructure(DataTypeToken dataType, NameToken name) {
            popStructure();
        }

        @Override
        public void beginArrayListStructure(DataTypeToken dataType, int subarraySize, NameToken name) {
            Structure struct = new Structure(null, dataType.getText(), name==null?null:name.getText());
            stack.push(struct);
        }

        @Override
        public void endArrayListStructure(DataTypeToken dataType, int subarraySize, NameToken name) {
            popStructure();
        }

        @Override
        public void beginSubArray(DataTypeToken dataType, int subarraySize) {
            subArray = new ArrayList<>();
        }

        @Override
        public void endSubArray(DataTypeToken dataType, int subarraySize) {
            stack.peek().data.add(subArray);
            subArray = null;
        }

        @Override
        public void beginCustomStructure(IdentifierToken identifier, NameToken name, PropertyMap properties) {
            Structure struct = new Structure(identifier.getText(), null, name==null?null:name.getText());

            for (PropertyMap.Entry e : properties) {
                struct.property(e.getKey(), e.getValue().getValueAsObject());
            }

            stack.push(struct);
        }

        @Override
        public void endCustomStructure(IdentifierToken identifier, NameToken name, PropertyMap properties) {
            popStructure();
        }

        private void pushValue(PropertyValueToken value) {
            if (subArray!=null) {
                subArray.add(value.getValueAsObject());
            } else {
                stack.peek().element(value.getValueAsObject());
            }
        }

        private void popStructure() {
            Structure last = stack.pop();
            if (stack.isEmpty()) {
                structures.add(last);
                if (!stack.isEmpty()) {
                    stack.push(last);
                }
            } else {
                stack.peek().child(last);
            }
        }
    }

    static Structure struct(String ident) {
        return new Structure(ident, null, null);
    }

    static Structure struct(String ident, String name) {
        return new Structure(ident, null, name);
    }

    static Structure list(String type, String name, Object...elements) {
        Structure ret = new Structure(null, type, name);
        for (Object k : elements) {
            ret.element(k);
        }
        return ret;
    }

    static Structure subList(Object...elements) {
        Structure ret = new Structure(null, null, null);
        for (Object k : elements) {
            ret.element(k);
        }
        return ret;
    }

    private static final class Structure {
        final String identifier;
        final String name;
        final String type;

        final Map<String, Object> properties = new HashMap<>();
        final List<Structure>     children   = new ArrayList<>();
        final List<Object>        data       = new ArrayList<>();

        Structure(String identifier, String type, String name) {
            this.identifier = identifier;
            this.type       = type;
            this.name       = name;
        }

        Structure property(String name, Object value) {
            properties.put(name, value);
            return this;
        }

        Structure child(Structure child) {
            children.add(child);
            return this;
        }

        Structure element(Object element) {
            data.add(element);
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Structure structure = (Structure) o;
            return Objects.equals(identifier, structure.identifier) &&
                    Objects.equals(name, structure.name) &&
                    Objects.equals(type, structure.type) &&
                    Objects.equals(properties, structure.properties) &&
                    Objects.equals(children, structure.children) &&
                    Objects.equals(data, structure.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier, name, type, properties, children, data);
        }

        @Override
        public String toString() {
            return "Structure{" +
                    "identifier='" + identifier + '\'' +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", properties=" + properties +
                    ", children=" + children +
                    ", data=" + data +
                    '}';
        }
    }
}
