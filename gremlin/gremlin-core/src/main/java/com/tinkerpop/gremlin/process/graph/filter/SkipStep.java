package com.tinkerpop.gremlin.process.graph.filter;

import com.tinkerpop.gremlin.process.Holder;
import com.tinkerpop.gremlin.process.Traversal;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Daniel Kuppitz <daniel at thinkaurelius.com>
 */
public class SkipStep<S> extends FilterStep<S> {

    public SkipStep(final Traversal traversal, final Predicate<Holder<S>> predicate) {
        super(traversal);
        final AtomicBoolean done = new AtomicBoolean(false);
        this.setPredicate(holder -> {
            boolean isDone;
            if (!(isDone = done.get())) {
                done.set(isDone = !predicate.test(holder));
            }
            return isDone;
        });
    }
}