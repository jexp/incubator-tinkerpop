package com.tinkerpop.gremlin.client.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.tinkerpop.blueprints.Property;
import com.tinkerpop.blueprints.Thing;
import com.tinkerpop.gremlin.client.GremlinClientErrorCodes;
import com.tinkerpop.gremlin.client.GremlinClientException;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * This class captures a remote property, typically a Property object in the response from a Gremlin Server
 */
public class RemoteProperty<V, T extends RemoteThing> implements Property<V, T> {
    private String key;
    private V value;
    private T thing;
    private Map<String, Property> metaPropertyMap;

    public RemoteProperty(String key, V value, Map<String, Property> metaPropertyMap, T thing) {
        this.key = key;
        this.value = value;
        this.metaPropertyMap = metaPropertyMap;
        this.thing = thing;
    }

    @Override
    public T getThing() {
        return thing;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public V getValue() throws NoSuchElementException {
        return value;
    }

    @Override
    public boolean isPresent() {
        return this.value != null;
    }

    @Override
    public Property removeProperty(String key) throws IllegalStateException {
        throw new GremlinClientException(GremlinClientErrorCodes.REMOTE_OBJECTS_ARE_IMMUTABLE, "remoteProperty() is not implemented");
    }

    @Override
    public <V2> Property<V2, Property> getProperty(String key) throws IllegalStateException {
        final Property<V2, Property> property = this.metaPropertyMap.get(key);
        return null == property ? Property.empty() : property;
    }

    @Override
    public Property setProperty(String key, Object value) throws IllegalStateException {
        throw new GremlinClientException(GremlinClientErrorCodes.REMOTE_OBJECTS_ARE_IMMUTABLE, "setProperty() is not implemented");
    }

    @Override
    public Map<String, Property> getProperties() {
        return metaPropertyMap;
    }
}
