package com.tinkerpop.gremlin.client;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Iterator;
import java.util.Map;

/**
 * This interface exposes methods to submit Gremlin queries on local or remote systems.
 */
public interface GremlinClient {
    /** This method evaluates a query on the server after initializing the bindings.
     * @param graphName name of the graph that must submit the transaction
     * @param bindings The binding map takes a map of variable names to objects, which will be serialized using Jackson's mapper.writeValue()
     * @param query The query to be executed on the server. The value of the last statement will be returned back  */
    public Iterator<Object> eval(String graphName, Map<String, Object> bindings, String query) throws GremlinClientException;

    /** This method evaluates a query on the server after initializing the bindings.
     * @param graphName name of the graph that must submit the transaction
     * @param bindings The binding map takes a map of variable names to objects, which will be serialized using Jackson's mapper.writeValue()
     * @param query The query to be executed on the server. The value of the last statement will be returned back
     * @param itemClass The class of the response iterator -- useful as a hint for deserialization */
    public Iterator<Object> eval(String graphName, Map<String, Object> bindings, String query, Class itemClass) throws GremlinClientException;

    /** This method evaluates a query on the server after initializing the bindings.
     * @param graphName name of the graph that must submit the transaction
     * @param bindings The binding map takes a map of variable names to objects, which will be serialized using Jackson's mapper.writeValue()
     * @param query The query to be executed on the server. The value of the last statement will be returned back
     * @param itemType The TypeReference to be used to de-serialize the response list */
    public Iterator<Object> eval(String graphName, Map<String, Object> bindings, String query, TypeReference itemType) throws GremlinClientException;
}
