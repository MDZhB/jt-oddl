package com.jiggawatt.jt.oddl;

import java.util.*;

/**
 * An immutable container for structure properties.
 *
 * @author Nikita
 */
public final class PropertyMap implements Iterable<PropertyMap.Entry> {

    private static final PropertyMap EMPTY_MAP = new PropertyMap(Collections.emptyMap());

    private final Map<String, PropertyValueToken> properties;

    PropertyMap(Map<IdentifierToken, PropertyValueToken> properties) {
        this.properties = new HashMap<>();

        for (Map.Entry<IdentifierToken, PropertyValueToken> e : properties.entrySet()) {
            this.properties.put(e.getKey().getText(), e.getValue());
        }
    }

    /**
     * Retrieves a property value by name.
     * @param name the identifier for the desired property
     * @return a token representing the given property's value, or <tt>null</tt> if the owning structure has no such
     * property
     */
    public PropertyValueToken get(String name) {
        return properties.get(name);
    }

    /**
     * @return an immutable view of the owning structure's property names
     */
    public Set<String> keys() {
        return Collections.unmodifiableSet(properties.keySet());
    }

    /**
     * @return an immutable view of the owning structure's property values
     */
    public Collection<PropertyValueToken> values() {
        return Collections.unmodifiableCollection(properties.values());
    }

    @Override
    public Iterator<Entry> iterator() {
        return new Itr(properties.entrySet().iterator());
    }

    /**
     * @return an empty property map
     */
    public static PropertyMap empty() {
        return EMPTY_MAP;
    }

    public static class Entry {
        private final String key;
        private final PropertyValueToken value;

        private Entry(Map.Entry<String, PropertyValueToken> w) {
            key = w.getKey();
            value = w.getValue();
        }

        public String getKey() {
            return key;
        }

        public PropertyValueToken getValue() {
            return value;
        }
    }

    private static class Itr implements Iterator<Entry> {
        private final Iterator<Map.Entry<String, PropertyValueToken>> wrap;

        private Itr(Iterator<Map.Entry<String, PropertyValueToken>> w) {
            wrap = w;
        }

        @Override
        public boolean hasNext() {
            return wrap.hasNext();
        }

        @Override
        public Entry next() {
            return new Entry(wrap.next());
        }
    }
}
