package com.tinkerpop.gremlin.client.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Property;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.client.GremlinClientErrorCodes;
import com.tinkerpop.gremlin.client.GremlinClientException;

import java.util.List;
import java.util.Map;

/**
 * This class captures a remote Edge
 */
public class RemoteEdge extends RemoteElement implements Edge {
    @JsonCreator
    public RemoteEdge(@JsonProperty("id") String id,
                        @JsonProperty("label") String label,
                        @JsonProperty("properties") RemotePropertyMap propertyMap) {
        super(id, label, propertyMap);
    }

    @Override
    public Vertex getVertex(Direction direction) throws IllegalArgumentException {
        throw new GremlinClientException(GremlinClientErrorCodes.REMOTE_OBJECTS_CANT_BE_QUERIED, "getVertex() not supported");
    }
    // HACK: Cut-paste of implementations from RemoteElement -- avoids conflicts between Vertex and RemoteElement
    @Override
    public Map<String, Property> getProperties() {
        // TODO: Changes this to Map<String, RemoteProperty> after getProperties() is changed to Map<String, ? extends Property>
        return propertyMap;
    }
}
