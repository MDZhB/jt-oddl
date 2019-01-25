# jt-oddl
[![Javadocs](https://www.javadoc.io/badge/com.github.mdzhb/jt-oddl.svg)](https://www.javadoc.io/doc/com.github.mdzhb/jt-oddl)

## Overview
`jt-oddl` is an [Open Data Description Language](http://www.openddl.org/) parser written in Java.

## Getting started
### Maven
Add the following to the `<dependencies>` element in your `pom.xml`:
```xml
<dependency>
  <groupId>com.github.mdzhb</groupId>
  <artifactId>jt-oddl</artifactId>
  <version>1.0.0</version>
</dependency>
```
### In Code
To parse an OpenDDL file, you need an `ODDLReader` and an implementation of the `ODDLListener` interface. The `ODDLReader` parses text read from a `Reader` or `InputStream`. Every time the `ODDLReader` encounters a valid language construct in the input, it calls the appropriate method in its `ODDLListener`.

Consider the following example listener. For brevity, it only implements three of the methods specified in the `ODDLListener` interface.
```java
// this class generates a list of Structure objects; imagine the Structure class is defined elsewhere in your code
class StructureListener implements ODDLListener<List<Structure>> {
    
    /** this is the collection of structures our listener will return */
    private final List<Structure>  structures = new ArrayList<>();
    
    /** a stack helps us handle nested structures */
    private final Deque<Structure> stack = new ArrayDeque<>();
    
    // this method is called when the reader encounters the beginning of a custom structure
    @Override
    public void beginCustomStructure(IdentifierToken identifier, NameToken name, PropertyMap properties) {
        stack.push(new Structure(identifier, name, properties));
    }
    
    // this method is called when the reader reaches the end of a custom structure (i.e. the closing brace)
    @Override
    public void endCustomStructure(IdentifierToken identifier, NameToken name, PropertyMap properties) {
        Structure struct = stack.pop();
        if (stack.isEmpty()) { // this is a top-level structure
            structures.add(struct);
        } else { // this is a child structure
            stack.peek().addChildStructure(struct);
        }
    }
    
    // this method is called when the reader reaches end of file
    @Override
    public List<Structure> end() {
        return structures; // this return value is the object returned by ODDLReader.read()
    }
}
```
You would use the `StructureListener` like this:
```java
try (InputStream in = Files.newInputStream(Paths.get("myfile.oddl"))) {
    List<Structure> structures = new ODDLReader(in).read(new StructureListener());
    // operate on your list of Structures...
}
```
`ODDLToken` defines methods that convert tokens to more specific types without casting. This is useful when handling structure properties in your listener implementation, for example.
```java
@Override
public void beginCustomStructure(IdentifierToken identifier, NameToken name, PropertyMap properties) {
    PropertyValueToken token = properties.get("propertyName");
    if (token.isFloat()) { // is this a float literal?
        FloatToken ft = token.asFloat(); // it is, so this line is valid
    }
}
```

If handling a large number of token types, it may be wiser to use a switch statement:
```java
switch (token.getType()) {
    case BOOL:
        BoolToken bt = token.asBool().getValue();
        break;
    case FLOAT:
        FloatToken ft = token.asFloat().getValue();
        break;
    // etc.
}
```
These "downcasting" methods throw an `IllegalArgumentException` when the token is not an instance of the desired class.
