package com.tinkerpop.gremlin.client.json;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerpop.blueprints.Property;
import com.tinkerpop.blueprints.Thing;
import com.tinkerpop.gremlin.client.GremlinClientErrorCodes;
import com.tinkerpop.gremlin.client.GremlinClientException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the root class for de-serialization of Gremlin Server responses using Jackson's polymorphic typing
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value=RemoteEdge.class, name="edge"),
        @JsonSubTypes.Type(value=RemoteVertex.class, name="vertex")
})
public class RemoteThing implements Thing {
    private static final ObjectMapper mapper = new ObjectMapper();

    Map<String, Property> propertyMap;

    public RemoteThing(RemotePropertyMap rpm) {
        this.propertyMap = new HashMap<String, Property>();

        // Call setThing on each property, and add it to the propertyMap
        rpm.forEach((key, value) -> {
            // TODO: Support meta-properties
            propertyMap.put(key, new RemoteProperty(key, deserializeValue(value.get()), null, this));
        } );
    }

    private Object deserializeValue(JsonNode valueNode) {
        // TODO: Rename "type" to something less used in ResponseSerializer
        if (valueNode.isNull()) {
            return null;
        } else if (valueNode.isTextual()) {
            return valueNode.asText();
        } else if (valueNode.isInt()) {
            return new Integer(valueNode.asInt());
        } else if (valueNode.isLong()) {
            return new Long(valueNode.asLong());
        } else if (valueNode.isNumber()) {
            return new BigDecimal(valueNode.asText());
        } else if (valueNode.isObject()) {
            Map<String, Object> ans = new HashMap<String, Object>();
            valueNode.fields().forEachRemaining(entry -> ans.put(entry.getKey(), deserializeValue(entry.getValue())));
            return ans;
        } else if (valueNode.isArray()) {
            Object[] ans = new Object[valueNode.size()];
            for (int i=0; i < ans.length; i++) {
                ans[i] = deserializeValue(valueNode.get(i));
            }

            return ans;
        } else {
            throw new GremlinClientException(GremlinClientErrorCodes.RESPONSE_DESERIALIZATION_ERROR, "Could not deserialize " + valueNode);
        }
    }

    @Override
    public Map<String, Property> getProperties() {
        // TODO: Changes this to Map<String, RemoteProperty> after getProperties() is changed to Map<String, ? extends Property>
        return propertyMap;
    }

    @Override
    public <V> Property<V, ? extends Thing> getProperty(String key) {
        return propertyMap.get(key);
    }

    @Override
    public <V> Property<V, ? extends Thing> setProperty(String key, V value) {
        throw new GremlinClientException(GremlinClientErrorCodes.REMOTE_OBJECTS_ARE_IMMUTABLE, "setProperty() is not implemented");
    }

    @Override
    public <V> Property<V, ? extends Thing> removeProperty(String key) {
        throw new GremlinClientException(GremlinClientErrorCodes.REMOTE_OBJECTS_ARE_IMMUTABLE, "removeProperty() is not implemented");
    }
}
