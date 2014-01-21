package com.tinkerpop.gremlin.client.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Property;
import com.tinkerpop.gremlin.client.GremlinClientErrorCodes;
import com.tinkerpop.gremlin.client.GremlinClientException;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * This class captures a remote property, typically a Property object in the response from a Gremlin Server
 */
public class RemoteProperty<V> implements Property<V> {
    private String key;
    private V value;
    private Element thing;

    public RemoteProperty(String key, V value, /* Map<String, Property> metaPropertyMap, */ Element thing) {
        this.key = key;
        this.value = value;
        this.thing = thing;
    }

    @Override
    public Element getElement() {
        return thing;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public V get() throws NoSuchElementException {
        return value;
    }

    @Override
    public boolean isPresent() {
        return this.value != null;
    }

    @Override
    public void remove() {
        throw new GremlinClientException(GremlinClientErrorCodes.REMOTE_OBJECTS_ARE_IMMUTABLE, "remove() is not implemented");
    }
}
