package com.tinkerpop.gremlin.client.executor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.gremlin.client.GremlinClient;
import com.tinkerpop.gremlin.client.GremlinClientErrorCodes;
import com.tinkerpop.gremlin.client.GremlinClientException;
import com.tinkerpop.gremlin.client.json.GremlinServerResponse;
import com.tinkerpop.gremlin.pipes.util.Holder;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * This class executes gremlin on a local graph, but de-serializes the bindings and serializes the results like a Gremlin server
 */
public class LocalGremlinClient implements GremlinClient {
    ScriptEngine scriptEngine;
    Map<String, Graph> graphs;

    public LocalGremlinClient(Map<String, Graph> graphs, ScriptEngine scriptEngine) {
        this.graphs = graphs;
        this.scriptEngine = scriptEngine;
    }

    @Override
    public Iterator<Object> eval(String graphName, Map<String, Object> bindingObjs, String script) throws RuntimeException {
        final Bindings bindings = new SimpleBindings();
        bindings.putAll(bindingObjs);

        boolean commitSuccess = false;
        GremlinServerResponse result;
        try {
            // Only one graph can eval the transaction -- Blueprints doesn't do distributed transactions
            Graph targetGraph = graphs.get(graphName);
            if (targetGraph == null) {
                throw new GremlinClientException(GremlinClientErrorCodes.UNDEFINED_GRAPH, "Looking for " + graphName + ". But the configured graph names are " + graphs.keySet());
            }
            bindings.put(graphName, targetGraph);

            // TODO: 1. Find a way to throw exception from a transaction. 2. Allow selection of retry strategy
            // Object resultObj = targetGraph.tx().submit(this).oneAndDone();
            Object resultObj = scriptEngine.eval("com.tinkerpop.gremlin.pipes." + script + ".toList()", bindings);

            ArrayList<Object> output = new ArrayList<Object>();
            if (resultObj instanceof Iterable) {
                for (Object item : ((Iterable)resultObj)) {
                    output.add(item);
                }
            } else {
                output.add(resultObj);
            }

            final Iterator<Object> outputIter = output.iterator();

            return new Iterator<Object>() {
                @Override
                public boolean hasNext() {
                    return outputIter.hasNext();
                }

                @Override
                public Object next() {
                    Object ans = outputIter.next();
                    if (ans instanceof Holder) {
                        return ((Holder)ans).get();
                    } else {
                        return ans;
                    }
                }
            };
        } catch (GremlinClientException gce) {
            throw gce;
        } catch (ScriptException se) {
            throw new GremlinClientException(GremlinClientErrorCodes.SCRIPT_EXEC_ERROR, se.getMessage());
        }
    }

    @Override
    public Iterator<Object> eval(String graphName, Map<String, Object> bindings, String query, Class itemClass) throws GremlinClientException {
        // Ignore itemClass
        return eval(graphName, bindings, query);
    }

    @Override
    public Iterator<Object> eval(String graphName, Map<String, Object> bindings, String query, TypeReference itemType) throws GremlinClientException {
        // Ignore itemClass
        return eval(graphName, bindings, query);
    }
}
