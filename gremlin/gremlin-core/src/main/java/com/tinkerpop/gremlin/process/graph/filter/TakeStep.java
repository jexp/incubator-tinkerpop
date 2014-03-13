package com.tinkerpop.gremlin.process.graph.filter;

import com.tinkerpop.gremlin.process.Holder;
import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.process.util.FastNoSuchElementException;

import java.util.function.Predicate;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Daniel Kuppitz <daniel at thinkaurelius.com>
 */
public class TakeStep<S> extends FilterStep<S> {

    public TakeStep(final Traversal traversal, final Predicate<Holder<S>> predicate) {
        super(traversal);
        this.setPredicate(holder -> {
            if (predicate.test(holder)) return true;
            throw FastNoSuchElementException.instance();
        });
    }
}