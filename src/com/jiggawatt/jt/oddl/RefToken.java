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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A token representing a reference. The first element of a reference may be a local or global name; all subsequent
 * names must be local.
 *
 * @author Nikita Leonidov
 */
public final class RefToken extends AbstractODDLToken implements PropertyValueToken, Iterable<NameToken> {

    private final List<NameToken> names;

    public RefToken() {
        super("null");
        this.names = Collections.emptyList();
    }

    public RefToken(List<NameToken> names) {
        super(namesToText(names));
        this.names = Collections.unmodifiableList(new ArrayList<>(names));
    }

    /**
     * @return an iterator over the names comprising this reference; all names but the first must be local
     */
    public Iterator<NameToken> iterator() {
        return names.iterator();
    }

    /**
     * @return an immutable view of the names comprising this reference; all names but the first must be local
     */
    public List<NameToken> getValue() {
        return names;
    }

    @Override
    public Object getValueAsObject() {
        if (names.isEmpty()) {
            return null;
        }

        List<String> ret = new ArrayList<>(names.size());
        for (int i=0; i<names.size(); i++) {
            ret.add(names.get(i).getText());
        }
        return ret;
    }

    private static String namesToText(List<NameToken> names) {
        StringBuilder sb = new StringBuilder();
        for (NameToken k : names) {
            sb.append(k.getText());
        }
        return sb.toString();
    }

    @Override
    public Type getType() {
        return Type.REF;
    }

    @Override
    public boolean isRef() {
        return true;
    }

    @Override
    public RefToken asRef() {
        return this;
    }
}
