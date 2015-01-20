package com.tinkerpop.gremlin.process.traverser.util;

import com.tinkerpop.gremlin.process.Path;
import com.tinkerpop.gremlin.process.Step;
import com.tinkerpop.gremlin.process.TraversalSideEffects;
import com.tinkerpop.gremlin.process.Traverser;
import com.tinkerpop.gremlin.structure.Vertex;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class DetachedTraverser<T> implements Serializable, Traverser<T>, Traverser.Admin<T> {

    private HashMap<Traverser.Admin.Component, Object> components = new HashMap<>();
    private final Class<? extends Traverser.Admin> traverserClass;

    public DetachedTraverser(final Traverser.Admin<T> traverser) {
        this.traverserClass = traverser.getClass();
        for (final Traverser.Admin.Component component : traverser.asAdmin().getComponents()) {
            this.components.put(component, component.apply(traverser));
        }
    }

    @Override
    public void merge(final Traverser.Admin<?> other) {
        throw new UnsupportedOperationException("DetachedTraversers can not be merged");
    }

    @Override
    public <R> Admin<R> split(final R r, final Step<T, R> step) {
        throw new UnsupportedOperationException("DetachedTraversers can not be split");
    }

    @Override
    public Admin<T> split() {
        throw new UnsupportedOperationException("DetachedTraversers can not be split");
    }

    @Override
    public void set(final T t) {
        throw new UnsupportedOperationException("DetachedTraversers can not have their object set");
    }

    @Override
    public void incrLoops(final String stepLabel) {
        throw new UnsupportedOperationException("DetachedTraversers can not have their loops incremented");
    }

    @Override
    public void resetLoops() {
        throw new UnsupportedOperationException("DetachedTraversers can not have their loops reset");
    }

    @Override
    public String getStepId() {
        if (this.components.containsKey(Component.STEP_ID))
            return (String) this.components.get(Component.STEP_ID);
        else
            throw new UnsupportedOperationException("The detached traverser does not have a step id: " + this.traverserClass.getCanonicalName());
    }

    @Override
    public void setStepId(final String stepId) {
        throw new UnsupportedOperationException("DetachedTraversers can not have their step id set");
    }

    @Override
    public void setBulk(final long count) {
        throw new UnsupportedOperationException("DetachedTraversers can not have their bulk set");
    }

    @Override
    public Admin<T> detach() {
        return null;
    }

    @Override
    public Admin<T> attach(final Vertex hostVertex) {
        try {
            final Traverser.Admin<T> traverser = this.traverserClass.getConstructor().newInstance();
        } catch (final Exception e) {

        }
    }

    @Override
    public void setSideEffects(final TraversalSideEffects sideEffects) {
        throw new UnsupportedOperationException("DetachedTraversers can not have their sideEffects set");
    }

    @Override
    public TraversalSideEffects getSideEffects() {
        throw new UnsupportedOperationException("DetachedTraversers do not have associated sideEffects");
    }

    @Override
    public Set<Component> getComponents() {
        return this.components.keySet();
    }

    @Override
    public T get() {
        if (this.components.containsKey(Component.OBJECT))
            return (T) this.components.get(Component.OBJECT);
        else
            throw new UnsupportedOperationException("The detached traverser does not have an object: " + this.traverserClass.getCanonicalName());
    }

    @Override
    public <S> S sack() {
        if (this.components.containsKey(Component.SACK))
            return (S) this.components.get(Component.SACK);
        else
            throw new UnsupportedOperationException("The detached traverser does not have a sack: " + this.traverserClass.getCanonicalName());
    }

    @Override
    public <S> void sack(final S object) {
        throw new UnsupportedOperationException("DetachedTraversers can not have their sack set");
    }

    @Override
    public Path path() {
        if (this.components.containsKey(Component.PATH))
            return (Path) this.components.get(Component.PATH);
        else
            throw new UnsupportedOperationException("The detached traverser does not have a path: " + this.traverserClass.getCanonicalName());
    }

    @Override
    public int loops() {
        if (this.components.containsKey(Component.LOOPS))
            return (int) this.components.get(Component.LOOPS);
        else
            throw new UnsupportedOperationException("The detached traverser does not have loops: " + this.traverserClass.getCanonicalName());
    }

    @Override
    public long bulk() {
        if (this.components.containsKey(Component.BULK))
            return (long) this.components.get(Component.BULK);
        else
            throw new UnsupportedOperationException("The detached traverser does not have bulk: " + this.traverserClass.getCanonicalName());
    }

    @Override
    public DetachedTraverser<T> clone() throws CloneNotSupportedException {
        final DetachedTraverser<T> clone = (DetachedTraverser<T>) super.clone();
        clone.components = (HashMap<Traverser.Admin.Component, Object>) this.components.clone();
        return clone;
    }
}
