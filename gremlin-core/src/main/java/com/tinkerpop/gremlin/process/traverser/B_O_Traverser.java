package com.tinkerpop.gremlin.process.traverser;

import com.tinkerpop.gremlin.process.Traverser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class B_O_Traverser<T> extends O_Traverser<T> {

    private final static Set<Component> COMPONENTS = new HashSet<>(Arrays.asList(Component.OBJECT, Component.STEP_ID, Component.BULK));

    protected long bulk = 1l;
    protected String stepId = HALT;

    protected B_O_Traverser() {
    }

    public B_O_Traverser(final T t, final long initialBulk) {
        super(t);
        this.bulk = initialBulk;
    }

    @Override
    public void setBulk(final long count) {
        this.bulk = count;
    }

    @Override
    public long bulk() {
        return this.bulk;
    }

    @Override
    public void merge(final Traverser.Admin<?> other) {
        this.bulk = this.bulk + other.bulk();
    }

    @Override
    public String getStepId() {
        return this.stepId;
    }

    @Override
    public void setStepId(final String stepId) {
        this.stepId = stepId;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof B_O_Traverser &&
                ((B_O_Traverser) object).get().equals(this.t) &&
                ((B_O_Traverser) object).getStepId().equals(this.stepId);
    }

    @Override
    public Set<Component> getComponents() {
        return COMPONENTS;
    }
}
