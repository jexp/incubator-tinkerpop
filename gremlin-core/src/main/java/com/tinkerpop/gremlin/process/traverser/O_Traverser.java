package com.tinkerpop.gremlin.process.traverser;

import com.tinkerpop.gremlin.process.traverser.util.AbstractTraverser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class O_Traverser<T> extends AbstractTraverser<T> {

    private final static Set<Component> COMPONENTS = new HashSet<>(Arrays.asList(Component.OBJECT));

    protected O_Traverser() {
    }

    public O_Traverser(final T t) {
        super(t);
    }

    @Override
    public Set<Component> getComponents() {
        return COMPONENTS;
    }
}
