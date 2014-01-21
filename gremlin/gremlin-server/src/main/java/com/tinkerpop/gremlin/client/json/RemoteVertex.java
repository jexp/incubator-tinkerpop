package com.tinkerpop.gremlin.client.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tinkerpop.blueprints.Edge;
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
    public RemoteVertex(@JsonProperty("id") String id,
                        @JsonProperty("label") String label,
                        @JsonProperty("properties") RemotePropertyMap propertyMap) {
        super(id, label, propertyMap);
    }

    @Override
    public VertexQuery query() {
        throw new GremlinClientException(GremlinClientErrorCodes.REMOTE_OBJECTS_CANT_BE_QUERIED, "query() not supported");
    }

    @Override
    public Edge addEdge(String label, Vertex inVertex, Object... objects) {
        throw new GremlinClientException(GremlinClientErrorCodes.REMOTE_OBJECTS_ARE_IMMUTABLE, "addEdge() is not supported");
    }
}
