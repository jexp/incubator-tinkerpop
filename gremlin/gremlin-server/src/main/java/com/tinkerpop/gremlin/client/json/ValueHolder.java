package com.tinkerpop.gremlin.client.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class ValueHolder {
    JsonNode value;

    @JsonCreator
    public ValueHolder(@JsonProperty("value") JsonNode value) {
        this.value = value;
    }

    public JsonNode get() {
        return value;
    }
}
