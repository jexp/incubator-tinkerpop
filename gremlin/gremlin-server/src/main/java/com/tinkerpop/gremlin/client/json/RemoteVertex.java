package com.tinkerpop.gremlin.client.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Property;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.query.VertexQuery;
import com.tinkerpop.gremlin.client.GremlinClientErrorCodes;
import com.tinkerpop.gremlin.client.GremlinClientException;

import java.util.List;
import java.util.Map;

/**
 * This class captures a remote vertex
 */
public class RemoteVertex extends RemoteElement implements Vertex {
    @JsonCreator
    public RemoteVertex(@JsonProperty("id") String id, @JsonProperty("properties") RemotePropertyMap propertyMap) {
        super(id, propertyMap);
    }

    @Override
    public VertexQuery query() {
        throw new GremlinClientException(GremlinClientErrorCodes.REMOTE_OBJECTS_CANT_BE_QUERIED, "query() not supported");
    }

    @Override
    public Edge addEdge(String label, Vertex inVertex, Property... properties) {
        throw new GremlinClientException(GremlinClientErrorCodes.REMOTE_OBJECTS_ARE_IMMUTABLE, "addEdge() is not supported");
    }

    // HACK: Cut-paste of implementations from RemoteElement -- avoids conflicts between Vertex and RemoteElement
    @Override
    public Map<String, Property> getProperties() {
        // TODO: Changes this to Map<String, RemoteProperty> after getProperties() is changed to Map<String, ? extends Property>
        return propertyMap;
    }

    @Override
    public <V> Property<V, Vertex> getProperty(String key) {
        return propertyMap.get(key);
    }

    @Override
    public <V> Property<V, Vertex> setProperty(String key, V value) {
        throw new GremlinClientException(GremlinClientErrorCodes.REMOTE_OBJECTS_ARE_IMMUTABLE, "setProperty() is not implemented");
    }

    @Override
    public <V> Property<V, Vertex> removeProperty(String key) {
        throw new GremlinClientException(GremlinClientErrorCodes.REMOTE_OBJECTS_ARE_IMMUTABLE, "removeProperty() is not implemented");
    }
}
