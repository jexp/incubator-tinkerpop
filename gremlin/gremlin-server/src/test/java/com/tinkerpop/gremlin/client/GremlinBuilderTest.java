package com.tinkerpop.gremlin.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.tinkergraph.TinkerFactory;
import com.tinkerpop.gremlin.client.executor.LocalGremlinClient;
import com.tinkerpop.gremlin.client.executor.LocalGremlinClientWithSerialization;
import com.tinkerpop.gremlin.client.executor.WebSocketClient;
import com.tinkerpop.gremlin.client.json.RemoteVertex;
import com.tinkerpop.gremlin.client.json.ValueHolder;
import com.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngineFactory;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.tinkerpop.gremlin.client.GremlinBuilder.var;
import static org.junit.Assert.assertEquals;

/**
 * Test cases for the Gremlin Client
 */
public class GremlinBuilderTest {
    private static final GremlinGroovyScriptEngineFactory gremlinGroovyScriptEngineFactory = new GremlinGroovyScriptEngineFactory();

    @Test
    public void testExpressionString() {
        assertEquals("Gremlin.of(g).V()", GremlinBuilder.of("g").V().toString());

        assertEquals("Gremlin.of(g).V().out('knows').out('created').has('name').value('name').path()",
                GremlinBuilder.of("g").V()
                        .out("knows").out("created")
                        .has("name")
                        .value("name").path().toString());

        assertEquals("Gremlin.of(g).V().as('x').out('knows').back('x')",
                GremlinBuilder.of("g").V().as("x").out("knows").back("x").toString());

        // TODO: Implement g.query().ids("1")
        assertEquals("Gremlin.of(g).V().as('x').out().loop('x',o -> ((Holder) o).getLoops() < 2,o -> false)",
                GremlinBuilder.of("g").V().as("x").out().loop("x", "o -> ((Holder) o).getLoops() < 2", "o -> false").toString());

        assertEquals("Gremlin.of(g).V().both().dedup({e -> ((Element) ((Holder) e).get()).getProperty('name').isPresent()})",
                GremlinBuilder.of("g").V().both().dedup("e -> ((Element) ((Holder) e).get()).getProperty('name').isPresent()").toString());

        assertEquals("Gremlin.of(g).V().as('x').out().as('y').select('x','y')",
                GremlinBuilder.of("g").V().as("x").out().as("y").select("x", "y").toString());

        // TODO: Support match -- need to know Groovy syntax
//        assertEquals("g.V().match('a','d',?.as('a').out('knows').as('b'),?.as('b')...",
//                GremlinBuilder.of("g").V().match("a", "d",
//                        GremlinBuilder.of().as("a").out("knows").as("b"),
//                        GremlinBuilder.of().as("b").out("created").as("c"),
//                        GremlinBuilder.of().as("c").value("name").as("d")).toString());

        assertEquals("Gremlin.of(g).V().as('a').out('knows').as('b').out('created').as('c').value('name').as('d')",
                GremlinBuilder.of("g").V().as("a").out("knows").as("b").out("created").as("c").value("name").as("d").toString());

        // TODO: Match tests case #2
//        assertEquals("g.V().out('knows').has('name','josh').out('created').has('name','lop').out('created').has('lang','java').in('created').has('name','peter').value('name')",
//                GremlinBuilder.of("g").V()
//                    .match("a", "b",
//                            GremlinBuilder.of().as("a").out("knows").has("name", "josh"),
//                            GremlinBuilder.of().as("a").out("created").has("name", "lop"),
//                            GremlinBuilder.of().as("a").out("created").as("b"),
//                            GremlinBuilder.of().as("b").has("lang","java"),
//                            GremlinBuilder.of().as("b").in("created").has("name","peter"))
//                    .value("name").path()
//                    .sideEffect(System.out::println).iterate();

        assertEquals("Gremlin.of(g).V().out('knows').has('name','josh').out('created').has('name','lop').out('created').has('lang','java').in('created').has('name','peter').value('name')",
                GremlinBuilder.of("g").V().out("knows").has("name", "josh").out("created").has("name", "lop").out("created").has("lang","java").in("created").has("name","peter").value("name").toString());

        // Using variable java
        assertEquals("Gremlin.of(g).V().out('knows').has('name','josh').out('created').has('name','lop').out('created').has('lang',java).in('created').has('name','peter').value('name')",
                GremlinBuilder.of("g").V().out("knows").has("name", "josh").out("created").has("name", "lop").out("created").has("lang", var("java")).in("created").has("name", "peter").value("name").toString());
    }

    @Test
    public void testExecute() throws Exception {
        Map<String, Graph> graphs = new HashMap<String, Graph>();
        graphs.put("g", TinkerFactory.createClassic());
        GremlinClient executor = new LocalGremlinClient(graphs, gremlinGroovyScriptEngineFactory.getScriptEngine());
        assertEquals(6,
                GremlinBuilder.of("g").V().submit(executor, new HashMap<String, Object>()).count());
        assertEquals(1,
                GremlinBuilder.of("g").V().has("name", "marko").submit(executor, new HashMap<String, Object>()).count());
        assertEquals(29,
                GremlinBuilder.of("g").V().has("name", "marko").value("age").submit(executor, new HashMap<String, Object>()).next());
    }

    @Test
    public void testExecuteWithSerDeser() throws Exception {
        Map<String, Graph> graphs = new HashMap<String, Graph>();
        graphs.put("g", TinkerFactory.createClassic());
        GremlinClient executor = new LocalGremlinClientWithSerialization(graphs, gremlinGroovyScriptEngineFactory.getScriptEngine(), new ObjectMapper());
        assertEquals(6,
                GremlinBuilder.of("g").V().submit(executor, new HashMap<String, Object>()).count());
        assertEquals(1,
                GremlinBuilder.of("g").V().has("name", "marko").submit(executor, new HashMap<String, Object>()).count());
        assertEquals(29,
                GremlinBuilder.of("g").V().has("name", "marko").value("age").submit(executor, new HashMap<String, Object>(), Integer.class).next());
    }

    @Test
    public void testExecuteWithWebsocketClient() throws Exception {
        WebSocketClient executor = null;
        try {
            executor = new WebSocketClient("ws://localhost:8182/gremlin/");
        } catch (Exception e) {
            // ignore
            System.out.println("Skipping web-socket tests");
            return;
        }

        assertEquals(6,
                GremlinBuilder.of("tg").V().submit(executor, new HashMap<String, Object>()).count());
        assertEquals(1,
                GremlinBuilder.of("tg").V().has("name", "marko").submit(executor).count());

        assertEquals(29,
                GremlinBuilder.of("tg").V().has("name", "marko").value("age").submit(executor, Integer.class).next());

        Vertex marko = (Vertex) GremlinBuilder.of("tg").V().has("name", "marko").submit(executor).next();
        assertEquals(29, marko.getProperty("age").get());

        Vertex lop = (Vertex) GremlinBuilder.of("tg").V().has("name", "marko").out("created").submit(executor).next();
        assertEquals("lop", lop.getProperty("name").get());

        // TODO: Implement de-serialization class for properties
//        ValueHolder markoName = (ValueHolder) ((Holder) (GremlinBuilder.of("tg").V().has("name", "josh").in("knows").property("name").submit(executor, ValueHolder.class).next())).get();
//        assertEquals("marko", markoName.get());
        marko = (Vertex) GremlinBuilder.of("tg").V().has("name", "josh").in("knows").submit(executor, RemoteVertex.class).next();
        assertEquals("marko", marko.getProperty("name").get());
        assertEquals(29, marko.getProperty("age").get());

        executor.close();
    }
}
