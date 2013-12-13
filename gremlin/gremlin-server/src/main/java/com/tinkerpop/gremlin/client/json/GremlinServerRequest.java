package com.tinkerpop.gremlin.client.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

/**
 * This class is a data holder for requests to the Gremlin Server
 */
@JsonPropertyOrder({"g", "s", "b"})
public class GremlinServerRequest {
    String graphName;
    String script;
    Map<String, Object> bindings;

    @JsonCreator
    public GremlinServerRequest(@JsonProperty("g") String graphName,
                                @JsonProperty("s") String script,
                                @JsonProperty("b") Map<String, Object> bindings) {
        this.graphName = graphName;
        this.bindings = bindings;
        this.script = script;
    }

    @JsonSerialize(typing=JsonSerialize.Typing.STATIC)
    @JsonProperty("b")
    public Map<String, Object> getBindings() {
        return bindings;
    }

    @JsonProperty("s")
    public String getScript() {
        return script;
    }

    @JsonProperty("g")
    public String getGraphName() {
        return graphName;
    }
}