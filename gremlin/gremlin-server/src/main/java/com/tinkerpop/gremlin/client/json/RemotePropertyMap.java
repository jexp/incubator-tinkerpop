package com.tinkerpop.gremlin.client.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;

/**
 * This is a convenience class to get around the type-erasure issue in Jackson deserializatoin
 */
public class RemotePropertyMap extends HashMap<String, ValueHolder> {
    public Object getValue(String key) {
        return get(key).get();
    }
}
