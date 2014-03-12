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

import static com.tinkerpop.gremlin.LoadGraphWith.GraphData.GRATEFUL;
import static org.junit.Assert.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Stephen Mallette (http://stephen.genoprime.com)
 * @author Daniel Kuppitz (daniel at thinkaurelius.com)
 */
public abstract class DedupRangeTest extends AbstractGremlinTest {

    public abstract Traversal<Vertex, String> get_first_3_distinct_artists_following_Hunter();

    public abstract Traversal<Vertex, String> get_next_3_distinct_artists_following_Hunter();

    public abstract Traversal<Vertex, String> get_first_6_distinct_artists_by_first_name_following_Hunter();

    @Test
    @LoadGraphWith(GRATEFUL)
    public void first_3_distinct_artists_following_Hunter() {
        final Iterator<String> traversal = get_first_3_distinct_artists_following_Hunter();
        System.out.println("Testing: " + traversal);
        final List<String> names = StreamFactory.stream(traversal).collect(Collectors.toList());
        assertEquals(3, names.size());
        assertTrue(names.contains("Weir"));
        assertTrue(names.contains("Donna_Godchaux"));
        assertTrue(names.contains("Garcia"));
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(GRATEFUL)
    public void next_3_distinct_artists_following_Hunter() {
        final Iterator<String> traversal = get_next_3_distinct_artists_following_Hunter();
        System.out.println("Testing: " + traversal);
        final List<String> names = StreamFactory.stream(traversal).collect(Collectors.toList());
        assertEquals(3, names.size());
        assertTrue(names.contains("Lesh"));
        assertTrue(names.contains("Weir_Hart"));
        assertTrue(names.contains("Garcia_Lesh"));
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(GRATEFUL)
    public void first_6_distinct_artists_by_first_name_following_Hunter() {
        final Iterator<String> traversal = get_first_6_distinct_artists_by_first_name_following_Hunter();
        System.out.println("Testing: " + traversal);
        final List<String> names = StreamFactory.stream(traversal).collect(Collectors.toList());
        assertEquals(5, names.size());
        assertTrue(names.contains("Weir"));
        assertTrue(names.contains("Donna_Godchaux"));
        assertTrue(names.contains("Garcia"));
        assertTrue(names.contains("Lesh"));
        assertTrue(names.contains("Pigpen_Weir"));
        assertFalse(traversal.hasNext());
    }

    public static class JavaDedupRangeTest extends DedupRangeTest {

        public Traversal<Vertex, String> get_first_3_distinct_artists_following_Hunter() {
            return g.V().has("name","Hunter").in("sung_by").out("followed_by").out("sung_by").dedup(0, 2).value("name");
        }

        public Traversal<Vertex, String> get_next_3_distinct_artists_following_Hunter() {
            return g.V().has("name","Hunter").in("sung_by").out("followed_by").out("sung_by").dedup(3, 5).value("name");
        }

        public Traversal<Vertex, String> get_first_6_distinct_artists_by_first_name_following_Hunter() {
            return g.V().has("name","Hunter").in("sung_by").out("followed_by").out("sung_by").dedup(v ->
                    v.getProperty("name").get().toString().split("_")[0], 0, 5).value("name");
        }
    }
}
