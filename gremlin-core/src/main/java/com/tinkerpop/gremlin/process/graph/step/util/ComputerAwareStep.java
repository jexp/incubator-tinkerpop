package com.tinkerpop.gremlin.process.graph.step.util;

import com.tinkerpop.gremlin.process.Step;
import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.process.TraversalEngine;
import com.tinkerpop.gremlin.process.Traverser;
import com.tinkerpop.gremlin.process.graph.marker.EngineDependent;
import com.tinkerpop.gremlin.process.util.AbstractStep;
import com.tinkerpop.gremlin.process.util.TraversalHelper;
import com.tinkerpop.gremlin.process.util.TraverserSet;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class ComputerAwareStep<S, E> extends AbstractStep<S, E> implements EngineDependent {

    protected boolean onGraphComputer;
    private TraverserSet<E> toEmit;
    private Iterator<Traverser<E>> previousIterator = Collections.emptyIterator();

    public ComputerAwareStep(final Traversal traversal) {
        super(traversal);
    }

    @Override
    public <A> void teleport(final Traverser<A> traverser, final Step<A, ?> destination) {
        if (this.onGraphComputer) {
            traverser.asAdmin().setStepId(destination.getId());
            this.toEmit.add((Traverser.Admin) traverser.asAdmin());

        } else {
            // TODO addStart should setStepId()
            traverser.asAdmin().setStepId(destination.getId());
            destination.addStart(traverser);
        }
    }

    public <A> void teleport(final Traverser<A> traverser, final Traversal<A, ?> destination) {
        this.teleport(traverser, TraversalHelper.getStart(destination.asAdmin()));
    }

    @Override
    protected Traverser<E> processNextStart() throws NoSuchElementException {
        while (true) {
            if (null != this.toEmit && !this.toEmit.isEmpty()) return this.toEmit.remove();
            if (this.previousIterator.hasNext()) return this.previousIterator.next();
            this.previousIterator = this.onGraphComputer ? this.computerAlgorithm() : this.standardAlgorithm();
        }
    }

    @Override
    public void onEngine(final TraversalEngine engine) {
        if (engine.equals(TraversalEngine.COMPUTER)) {
            this.onGraphComputer = true;
            this.traverserStepIdSetByChild = true;
            this.toEmit = new TraverserSet<>();
        }
    }

    @Override
    public ComputerAwareStep<S, E> clone() throws CloneNotSupportedException {
        final ComputerAwareStep<S, E> clone = (ComputerAwareStep<S, E>) super.clone();
        clone.previousIterator = Collections.emptyIterator();
        if (this.onGraphComputer)
            clone.toEmit = new TraverserSet<>();
        return clone;
    }

    protected abstract Iterator<Traverser<E>> standardAlgorithm() throws NoSuchElementException;

    protected abstract Iterator<Traverser<E>> computerAlgorithm() throws NoSuchElementException;
}
