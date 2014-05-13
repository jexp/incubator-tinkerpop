package com.tinkerpop.gremlin.structure;

import com.tinkerpop.gremlin.process.Holder;
import com.tinkerpop.gremlin.process.T;
import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.process.graph.DefaultGraphTraversal;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.process.graph.map.StartStep;
import com.tinkerpop.gremlin.structure.util.ElementHelper;
import com.tinkerpop.gremlin.util.function.SConsumer;
import com.tinkerpop.gremlin.util.function.SFunction;
import com.tinkerpop.gremlin.util.function.SPredicate;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * An {@link Element} is the base class for both {@link Vertex} and {@link Edge}. An {@link Element} has an identifier
 * that must be unique to its inheriting classes ({@link Vertex} or {@link Edge}). An {@link Element} can maintain a
 * collection of {@link Property} objects.  Typically, objects are Java primitives (e.g. String, long, int, boolean,
 * etc.)
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public abstract interface Element<E extends Element> {

    public static final String ID = "id";
    public static final String LABEL = "label";
    public static final String DEFAULT_LABEL = "default";

    public Object getId();

    public String getLabel();

    // todo: make sure id/label get returned as properties

    public default Set<String> getPropertyKeys() {
        return this.getProperties().keySet();
    }

    public default Set<String> getHiddenKeys() {
        return this.getHiddens().keySet();
    }

    public Map<String, Property> getProperties();

    public Map<String, Property> getHiddens();

    public <V> Property<V> getProperty(final String key);

    public <V> void setProperty(final String key, final V value);

    public default void setProperties(final Object... keyValues) {
        ElementHelper.legalPropertyKeyValueArray(keyValues);
        ElementHelper.attachProperties(this, keyValues);
    }

    public default <V> V getValue(final String key) throws NoSuchElementException {
        final Property<V> property = this.getProperty(key);
        if (property.isPresent())
            return property.get();
        else throw Property.Exceptions.propertyDoesNotExist(key);
    }

    /*
    // TODO: Are we going down the right road with property as a first-class citizen?
    public default <V> V getValue(final String key, final V orElse) {
        final Property<V> property = this.getProperty(key);
        return property.orElse(orElse);
    }*/

    // steps ///////////////////////////////////////////////////////////////////

    public default <E extends Element> GraphTraversal<E, E> aggregate(final String variable, final SFunction... preAggregateFunctions) {
        return this.<E>start().aggregate(variable, preAggregateFunctions);
    }

    public default <E extends Element, E2> GraphTraversal<E, AnnotatedValue<E2>> annotatedValues(final String propertyKey) {
        return this.<E>start().<E2>annotatedValues(propertyKey);
    }

    // TODO: test
    public default <E extends Element, E2> GraphTraversal<E, E2> annotation(final String annotationKey) {
        return this.<E>start().annotation(annotationKey);
    }

    // TODO: test
    public default <E extends Element> GraphTraversal<E, Map<String, Object>> annotations(final String... annotationKeys) {
        return this.<E>start().annotations(annotationKeys);
    }

    public default <E extends Element> GraphTraversal<E, E> as(final String as) {
        return this.<E>start().as(as);
    }

    public default <E extends Element> GraphTraversal<E, E> filter(final SPredicate<Holder<E>> predicate) {
        return this.<E>start().filter(predicate);
    }

    // TODO: test
    public default <E extends Element, E2> GraphTraversal<E, E2> flatMap(final SFunction<Holder<E>, Iterator<E2>> function) {
        return this.<E>start().flatMap(function);
    }

    public default <E extends Element, E2> GraphTraversal<E, E2> has(final String key) {
        return this.<E>start().has(key);
    }

    public default <E extends Element, E2> GraphTraversal<E, E2> has(final String key, final Object value) {
        return this.<E>start().has(key, value);
    }

    public default <E extends Element, E2> GraphTraversal<E, E2> has(final String key, final T t, final Object value) {
        return this.<E>start().has(key, t, value);
    }

    // TODO: test
    public default <E extends Element, E2> GraphTraversal<E, E2> has(final String key, final BiPredicate predicate, final Object value) {
        return this.<E>start().has(key, predicate, value);
    }

    // TODO: test
    public default <E extends Element> GraphTraversal<E, Element> has(final String propertyKey, final String annotationKey, final BiPredicate biPredicate, final Object annotationValue) {
        return this.<E>start().has(propertyKey, annotationKey, biPredicate, annotationValue);
    }

    // TODO: test
    public default <E extends Element> GraphTraversal<E, Element> has(final String propertyKey, final String annotationKey, final T t, final Object annotationValue) {
        return this.<E>start().has(propertyKey, annotationKey, t, annotationValue);
    }

    // TODO: test
    public default <E extends Element> GraphTraversal<E, Element> has(final String propertyKey, final String annotationKey, final Object annotationValue) {
        return this.<E>start().has(propertyKey, annotationKey, annotationValue);
    }

    public default <E extends Element, E2> GraphTraversal<E, E2> hasNot(final String key) {
        return this.<E>start().hasNot(key);
    }

    public default <E extends Element> GraphTraversal<E, E> identity() {
        return this.<E>start().identity();
    }

    // TODO: intersect

    // TODO: test
    public default <E extends Element, E2> GraphTraversal<E, E2> interval(final String key, final Comparable startValue, final Comparable endValue) {
        return this.<E>start().interval(key, startValue, endValue);
    }

    // TODO: test
    public default <E extends Element> GraphTraversal<E, E> jump(final String as) {
        return this.<E>start().jump(as);
    }

    // TODO: test
    public default <E extends Element> GraphTraversal<E, E> jump(final String as, final SPredicate<Holder<E>> ifPredicate) {
        return this.<E>start().jump(as, ifPredicate);
    }

    // TODO: test
    public default <E extends Element> GraphTraversal<E, E> jump(final String as, final SPredicate<Holder<E>> ifPredicate, final SPredicate<Holder<E>> emitPredicate) {
        return this.<E>start().jump(as, ifPredicate, emitPredicate);
    }

    // TODO: test
    public default <E extends Element, E2> GraphTraversal<E, E2> map(final SFunction<Holder<E>, E2> function) {
        return this.<E>start().map(function);
    }

    // TODO: test
    public default <E extends Element, E2> GraphTraversal<E, E2> match(final String inAs, final String outAs, final Traversal... traversals) {
        return this.<E>start().match(inAs, outAs, traversals);
    }

    // TODO: pageRank

    /**
     * public default <E2> GraphTraversal<Vertex, Property<E2>> properties() {
     * return (GraphTraversal) this.start().values;
     * }*
     */

    public default <E extends Element, E2> GraphTraversal<E, Property<E2>> property(final String propertyKey) {
        return this.<E>start().<E2>property(propertyKey);
    }

    // TODO: test
    public default void remove() {
        this.start().remove();
    }

    public default GraphTraversal<E, E> sideEffect(final SConsumer<Holder<E>> consumer) {
        return this.<E>start().sideEffect(consumer);
    }

    public default <E extends Element> GraphTraversal<E, E> start() {
        final GraphTraversal<E, E> traversal = new DefaultGraphTraversal<>();
        return traversal.<E, E, GraphTraversal<E, E>>addStep(new StartStep<>(traversal, this));
    }

    // TODO: union

    public default <E extends Element, E2> GraphTraversal<E, E2> value(final String propertyKey) {
        return this.<E>start().value(propertyKey);
    }

    // TODO: test
    public default <E extends Element> GraphTraversal<E, Map<String, Object>> values(final String... propertyKeys) {
        return this.<E>start().values(propertyKeys);
    }

    public default GraphTraversal<E, E> with(final Object... variableValues) {
        return this.<E>start().with(variableValues);
    }

    ////////////////////////////////////////////////////////////////////////////

    public static class Exceptions {
        public static IllegalArgumentException bothIsNotSupported() {
            return new IllegalArgumentException("A direction of BOTH is not supported");
        }

        public static IllegalArgumentException providedKeyValuesMustBeAMultipleOfTwo() {
            return new IllegalArgumentException("The provided key/value array must be a multiple of two");
        }

        public static IllegalArgumentException providedKeyValuesMustHaveALegalKeyOnEvenIndices() {
            return new IllegalArgumentException("The provided key/value array must have a String key or Property.Key on even array indices");
        }

        public static IllegalStateException elementHasAlreadyBeenRemovedOrDoesNotExist(final Class<? extends Element> type, final Object id) {
            return new IllegalStateException(String.format("The %s with id [%s] has already been removed or does not exist", type.getClass().getSimpleName(), id));
        }
    }

}
