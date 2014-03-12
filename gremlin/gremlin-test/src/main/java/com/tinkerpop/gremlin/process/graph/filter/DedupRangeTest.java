package com.tinkerpop.gremlin.process.graph.filter;

import com.tinkerpop.gremlin.AbstractGremlinTest;
import com.tinkerpop.gremlin.LoadGraphWith;
import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.util.StreamFactory;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.tinkerpop.gremlin.LoadGraphWith.GraphData.CLASSIC;
import static org.junit.Assert.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public abstract class DedupRangeTest extends AbstractGremlinTest {

    public abstract Traversal<Vertex, String> get_g_V_both_dedup_0_3_name();

    public abstract Traversal<Vertex, String> get_g_V_both_dedupXlangX_0_0_name();

    @Test
    @LoadGraphWith(CLASSIC)
    public void g_V_both_dedup_0_3_name() {
        final Iterator<String> traversal = get_g_V_both_dedup_0_3_name();
        System.out.println("Testing: " + traversal);
        final List<String> names = StreamFactory.stream(traversal).collect(Collectors.toList());
        assertEquals(4, names.size());
        final int matches = (names.contains("marko") ? 1 : 0) +
                (names.contains("vadas") ? 1 : 0) +
                (names.contains("lop") ? 1 : 0) +
                (names.contains("josh") ? 1 : 0) +
                (names.contains("ripple") ? 1 : 0) +
                (names.contains("peter") ? 1 : 0);
        assertEquals(4, matches);
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(CLASSIC)
    public void g_V_both_dedupXlangX_0_0_name() {
        final Iterator<String> traversal = get_g_V_both_dedupXlangX_0_0_name();
        System.out.println("Testing: " + traversal);
        final List<String> names = StreamFactory.stream(traversal).collect(Collectors.toList());
        assertEquals(1, names.size());
        assertTrue(names.contains("marko") || names.contains("peter") || names.contains("josh") || names.contains("vadas") || names.contains("lop") || names.contains("ripple"));
        assertFalse(traversal.hasNext());
    }

    public static class JavaDedupRangeTest extends DedupRangeTest {

        public Traversal<Vertex, String> get_g_V_both_dedup_0_3_name() {
            return g.V().both().dedup(0, 3).value("name");
        }

        public Traversal<Vertex, String> get_g_V_both_dedupXlangX_0_0_name() {
            return g.V().both().dedup(v -> v.getProperty("lang").orElse(null), 0, 0).value("name");
        }
    }
}
