package com.tinkerpop.gremlin.structure;

import com.tinkerpop.gremlin.process.Holder;
import com.tinkerpop.gremlin.process.T;
import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.process.graph.DefaultGraphTraversal;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.process.graph.filter.FilterStep;
import com.tinkerpop.gremlin.process.graph.filter.HasAnnotationStep;
import com.tinkerpop.gremlin.process.graph.filter.HasStep;
import com.tinkerpop.gremlin.process.graph.filter.IntervalStep;
import com.tinkerpop.gremlin.process.graph.map.AnnotatedValueStep;
import com.tinkerpop.gremlin.process.graph.map.AnnotationValueStep;
import com.tinkerpop.gremlin.process.graph.map.AnnotationValuesStep;
import com.tinkerpop.gremlin.process.graph.map.JumpStep;
import com.tinkerpop.gremlin.process.graph.map.PropertyStep;
import com.tinkerpop.gremlin.process.graph.map.StartStep;
import com.tinkerpop.gremlin.structure.util.HasContainer;
import com.tinkerpop.gremlin.util.function.SConsumer;
import com.tinkerpop.gremlin.util.function.SFunction;
import com.tinkerpop.gremlin.util.function.SPredicate;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * A {@link Vertex} maintains pointers to both a set of incoming and outgoing {@link Edge} objects. The outgoing edges
 * are those edges for  which the {@link Vertex} is the tail. The incoming edges are those edges for which the
 * {@link Vertex} is the head.
 * <p/>
 * Diagrammatically:
 * <pre>
 * ---inEdges---> vertex ---outEdges--->.
 * </pre>
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Vertex extends Element {

    public Edge addEdge(final String label, final Vertex inVertex, final Object... keyValues);

    public GraphTraversal<Vertex, Vertex> out(final int branchFactor, final String... labels);

    public GraphTraversal<Vertex, Vertex> in(final int branchFactor, final String... labels);

    public GraphTraversal<Vertex, Vertex> both(final int branchFactor, final String... labels);

    public GraphTraversal<Vertex, Edge> outE(final int branchFactor, final String... labels);

    public GraphTraversal<Vertex, Edge> inE(final int branchFactor, final String... labels);

    public GraphTraversal<Vertex, Edge> bothE(final int branchFactor, final String... labels);

    // vertex-specific steps ///////////////////////////////////////////////////

    public default GraphTraversal<Vertex, Vertex> out(final String... labels) {
        return this.out(Integer.MAX_VALUE, labels);
    }

    public default GraphTraversal<Vertex, Vertex> in(final String... labels) {
        return this.in(Integer.MAX_VALUE, labels);
    }

    public default GraphTraversal<Vertex, Vertex> both(final String... labels) {
        return this.both(Integer.MAX_VALUE, labels);
    }

    public default GraphTraversal<Vertex, Edge> outE(final String... labels) {
        return this.outE(Integer.MAX_VALUE, labels);
    }

    public default GraphTraversal<Vertex, Edge> inE(final String... labels) {
        return this.inE(Integer.MAX_VALUE, labels);
    }

    public default GraphTraversal<Vertex, Edge> bothE(final String... labels) {
        return this.bothE(Integer.MAX_VALUE, labels);
    }

    ////////////////////////////////////////////////////////////////////////////

    public static class Exceptions {
        public static UnsupportedOperationException userSuppliedIdsNotSupported() {
            return new UnsupportedOperationException("Vertex does not support user supplied identifiers");
        }
    }
}
