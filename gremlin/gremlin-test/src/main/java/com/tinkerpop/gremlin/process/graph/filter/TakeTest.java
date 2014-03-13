package com.tinkerpop.gremlin.process.graph.filter;

import com.tinkerpop.gremlin.AbstractGremlinTest;
import com.tinkerpop.gremlin.LoadGraphWith;
import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.util.StreamFactory;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.tinkerpop.gremlin.LoadGraphWith.GraphData.GRATEFUL;
import static org.junit.Assert.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Stephen Mallette (http://stephen.genoprime.com)
 * @author Daniel Kuppitz (daniel at thinkaurelius.com)
 */
public abstract class TakeTest extends AbstractGremlinTest {

    public abstract Traversal<Vertex, Integer> get_g_V_hasXtype_songX_takeXperformances_LTE_10X_valueXperformanceX();

    @Test
    @LoadGraphWith(GRATEFUL)
    public void g_V_hasXtype_songX_takeXperformances_LTE_10X_valueXperformanceX() {
        final Iterator<Integer> traversal = get_g_V_hasXtype_songX_takeXperformances_LTE_10X_valueXperformanceX();
        System.out.println("Testing: " + traversal);
        final List<Integer> performances = StreamFactory.stream(traversal).collect(Collectors.toList());
        assertEquals(2, performances.size());
        assertTrue(performances.stream().filter(i -> i != null).reduce(0, (a, b) -> a + b) <= 10);
        assertFalse(traversal.hasNext());
    }

    public static class JavaTakeTest extends TakeTest {

        public Traversal<Vertex, Integer> get_g_V_hasXtype_songX_takeXperformances_LTE_10X_valueXperformanceX() {
            final AtomicInteger counter = new AtomicInteger(0);
            return g.V().has("type", "song").take(holder -> {
                final Vertex v = (Vertex) holder.get();
                return counter.addAndGet(v.<Integer>getProperty("performances").orElse(0)) <= 10;
            }).value("performances");
        }
    }
}
