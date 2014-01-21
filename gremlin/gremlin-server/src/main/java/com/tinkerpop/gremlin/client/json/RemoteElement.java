package com.tinkerpop.gremlin.client.json;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Property;
import com.tinkerpop.gremlin.client.GremlinClientErrorCodes;
import com.tinkerpop.gremlin.client.GremlinClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class captures a remote Element
 */
public class RemoteElement extends RemoteThing implements Element {
    String id;

    public RemoteElement(String id, RemotePropertyMap propertyMap) {
        super(propertyMap);

        this.id = id;
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void remove() {
        throw new GremlinClientException(GremlinClientErrorCodes.REMOTE_OBJECTS_ARE_IMMUTABLE, "remove() is not implemented");
    }

    // HACK: Cut-paste of implementation from RemoteThing -- avoids conflicts between Element and RemoteThing
    @Override
    public Map<String, Property> getProperties() {
        // TODO: Changes this to Map<String, RemoteProperty> after getProperties() is changed to Map<String, ? extends Property>
        return propertyMap;
    }

    @Override
    public <V> Property<V, ? extends Element> getProperty(String key) {
        return propertyMap.get(key);
    }

    @Override
    public <V> Property<V, ? extends Element> setProperty(String key, V value) {
        throw new GremlinClientException(GremlinClientErrorCodes.REMOTE_OBJECTS_ARE_IMMUTABLE, "setProperty() is not implemented");
    }

    @Override
    public <V> Property<V, ? extends Element> removeProperty(String key) {
        throw new GremlinClientException(GremlinClientErrorCodes.REMOTE_OBJECTS_ARE_IMMUTABLE, "removeProperty() is not implemented");
    }
}
