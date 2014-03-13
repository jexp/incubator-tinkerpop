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
 * @author Daniel Kuppitz <daniel at thinkaurelius.com>
 */
public abstract class SkipTest extends AbstractGremlinTest {

    public abstract Traversal<Vertex, String> get_g_v3_outEXfollowed_byX_orderXa_weightXb_weightX_skipXweight_LT_50X_inV_valueXnameX();

    @Test
    @LoadGraphWith(GRATEFUL)
    public void g_v3_outEXfollowed_byX_orderXa_weightXb_weightX_skipXweight_LT_50X_inV_valueXnameX() {
        final Iterator<String> traversal = get_g_v3_outEXfollowed_byX_orderXa_weightXb_weightX_skipXweight_LT_50X_inV_valueXnameX();
        System.out.println("Testing: " + traversal);
        final List<String> names = StreamFactory.stream(traversal).collect(Collectors.toList());
        assertEquals(2, names.size());
        assertTrue(names.contains("BLACK PETER"));
        assertTrue(names.contains("GOING DOWN THE ROAD FEELING BAD"));
        assertFalse(traversal.hasNext());
    }

    public static class JavaSkipTest extends SkipTest {

        public Traversal<Vertex, String> get_g_v3_outEXfollowed_byX_orderXa_weightXb_weightX_skipXweight_LT_50X_inV_valueXnameX() {
            return g.v(3).outE("followed_by").order((a, b) -> a.get().<Integer>getValue("weight").compareTo(b.get().getValue("weight")))
                    .skip(holder -> holder.get().<Integer>getValue("weight") < 50).inV().value("name");
        }
    }
}
