/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.neo4j.structure;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.neo4j.tinkerpop.api.Neo4jEntity;
import org.neo4j.tinkerpop.api.Neo4jNode;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class Neo4jProperty<V> implements Property<V> {

    private final Element element;
    private final String key;
    private final Neo4jGraph graph;
    private V value;

    public Neo4jProperty(final Element element, final String key, final V value) {
        this.element = element;
        this.key = key;
        this.value = value;
        this.graph = element instanceof Neo4jVertexProperty ?
                ((Neo4jVertex) (((Neo4jVertexProperty) element).element())).graph :
                ((Neo4jElement) element).graph;
    }

    @Override
    public Element element() {
        return this.element;
    }

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public V value() {
        return this.value;
    }

    @Override
    public boolean isPresent() {
        return null != this.value;
    }

    @Override
    public String toString() {
        return StringFactory.propertyString(this);
    }

    @Override
    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    @Override
    public int hashCode() {
        return ElementHelper.hashCode(this);
    }

    @Override
    public void remove() {
        this.graph.tx().readWrite();
        if (this.element instanceof VertexProperty) {
            final Neo4jNode node = ((Neo4jVertexProperty) this.element).getBaseVertex();
            if (null != node && node.hasProperty(this.key)) {
                node.removeProperty(this.key);
            }
        } else {
            final Neo4jEntity entity = ((Neo4jElement) this.element).getBaseElement();
            if (entity.hasProperty(this.key)) {
                entity.removeProperty(this.key);
            }
        }
    }

}
