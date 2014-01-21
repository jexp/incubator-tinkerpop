package com.tinkerpop.gremlin.client.json;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Property;
import com.tinkerpop.gremlin.client.GremlinClientErrorCodes;
import com.tinkerpop.gremlin.client.GremlinClientException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class captures a remote Element
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value=RemoteEdge.class, name="edge"),
        @JsonSubTypes.Type(value=RemoteVertex.class, name="vertex")
})
public class RemoteElement implements Element {
    private static final ObjectMapper mapper = new ObjectMapper();
    Map<String, Property> propertyMap;

    String id;
    String label;

    public RemoteElement(String id, String label, RemotePropertyMap pm) {
        this.id = id;
        this.label = label;
        this.propertyMap = new HashMap<String, Property>();

        // Call setThing on each property, and add it to the propertyMap
        pm.forEach((key, value) -> {
            // TODO: Support meta-properties
            propertyMap.put(key, new RemoteProperty<Object>(key, deserializeValue(value.get()), this));
        } );
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void remove() {
        throw new GremlinClientException(GremlinClientErrorCodes.REMOTE_OBJECTS_ARE_IMMUTABLE, "remove() is not implemented");
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
    public <V> Property<V> getProperty(String key) {
        return propertyMap.get(key);
    }

    @Override
    public <V> void setProperty(final String key, final V value) {
        throw new GremlinClientException(GremlinClientErrorCodes.REMOTE_OBJECTS_ARE_IMMUTABLE, "setProperty() is not implemented");
    }
}
