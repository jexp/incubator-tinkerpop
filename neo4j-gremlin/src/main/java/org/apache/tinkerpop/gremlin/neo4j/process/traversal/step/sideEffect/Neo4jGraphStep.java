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
package org.apache.tinkerpop.gremlin.neo4j.process.traversal.step.sideEffect;

import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jEdge;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jVertex;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jVertexProperty;
import org.apache.tinkerpop.gremlin.process.traversal.T;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.GraphStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.Compare;
import org.apache.tinkerpop.gremlin.structure.Contains;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.util.StreamFactory;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;
import org.javatuples.Pair;
import org.neo4j.tinkerpop.api.Neo4jDirection;
import org.neo4j.tinkerpop.api.Neo4jGraphAPI;
import org.neo4j.tinkerpop.api.Neo4jNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Stephen Mallette (http://stephen.genoprime.com)
 * @author Pieter Martin
 */
public class Neo4jGraphStep<S extends Element> extends GraphStep<S> {

    public final List<HasContainer> hasContainers = new ArrayList<>();

    public Neo4jGraphStep(final GraphStep<S> originalGraphStep) {
        super(originalGraphStep.getTraversal(), originalGraphStep.getReturnClass(), originalGraphStep.getIds());
        if (originalGraphStep.getLabel().isPresent())
            this.setLabel(originalGraphStep.getLabel().get());
        //No need to do anything if the first element is an Element, all elements are guaranteed to be an element and will be return as is
        if ((this.ids.length == 0 || !(this.ids[0] instanceof Element)))
            this.setIteratorSupplier(() -> (Iterator<S>) (Vertex.class.isAssignableFrom(this.returnClass) ? this.vertices() : this.edges()));
    }

    private Iterator<? extends Edge> edges() {
        final Neo4jGraph graph = (Neo4jGraph) this.getTraversal().getGraph().get();
        graph.tx().readWrite();
        // ids are present, filter on them first
        if (this.ids != null && this.ids.length > 0)
            return IteratorUtils.filter(graph.edges(this.ids), edge -> HasContainer.testAll((Edge) edge, this.hasContainers));
        final HasContainer hasContainer = this.getHasContainerForAutomaticIndex(Edge.class);
        return (null == hasContainer) ?
                IteratorUtils.filter(graph.edges(), edge -> HasContainer.testAll((Edge) edge, this.hasContainers)) :
                getEdgesUsingAutomaticIndex(hasContainer).filter(edge -> HasContainer.testAll((Edge) edge, this.hasContainers)).iterator();
    }

    private Iterator<? extends Vertex> vertices() {
        final Neo4jGraph graph = (Neo4jGraph) this.getTraversal().getGraph().get();
        graph.tx().readWrite();
        // ids are present, filter on them first
        if (this.ids != null && this.ids.length > 0)
            return IteratorUtils.filter(graph.vertices(this.ids), vertex -> HasContainer.testAll((Vertex) vertex, this.hasContainers));
        // a label and a property
        final Pair<String, HasContainer> labelHasPair = this.getHasContainerForLabelIndex();
        if (null != labelHasPair)
            return this.getVerticesUsingLabelAndProperty(labelHasPair.getValue0(), labelHasPair.getValue1())
                    .filter(vertex -> HasContainer.testAll((Vertex) vertex, this.hasContainers)).iterator();
        // use automatic indices
        final HasContainer hasContainer = this.getHasContainerForAutomaticIndex(Vertex.class);
        if (null != hasContainer)
            return this.getVerticesUsingAutomaticIndex(hasContainer)
                    .filter(vertex -> HasContainer.testAll((Vertex) vertex, this.hasContainers)).iterator();
        // only labels
        final List<String> labels = this.getLabels();
        if (null != labels)
            return this.getVerticesUsingOnlyLabels(labels).filter(vertex -> HasContainer.testAll((Vertex) vertex, this.hasContainers)).iterator();
        // linear scan
        return IteratorUtils.filter(graph.vertices(), vertex -> HasContainer.testAll((Vertex) vertex, this.hasContainers));
    }


    private Stream<Neo4jVertex> getVerticesUsingLabelAndProperty(final String label, final HasContainer hasContainer) {
        //System.out.println("labelProperty: " + label + ":" + hasContainer);
        final Neo4jGraph graph = (Neo4jGraph) this.getTraversal().getGraph().get();
        final Iterable<Neo4jNode> iterator1 = graph.getBaseGraph().findNodes(label, hasContainer.key, hasContainer.value);
        final Iterable<Neo4jNode> iterator2 = graph.getBaseGraph().findNodes(hasContainer.key, T.value.getAccessor(), hasContainer.value);
        final Stream<Neo4jVertex> stream1 = StreamFactory.stream(iterator1)
                .filter(node -> ElementHelper.idExists(node.getId(), this.ids))
                .map(node -> new Neo4jVertex(node, graph));
        final Stream<Neo4jVertex> stream2 = StreamFactory.stream(iterator2)
                .filter(node -> ElementHelper.idExists(node.getId(), this.ids))
                .filter(node -> node.getProperty(T.key.getAccessor()).equals(hasContainer.key))
                .map(node -> node.relationships(Neo4jDirection.INCOMING).iterator().next().start())
                .map(node -> new Neo4jVertex(node, graph));
        return Stream.concat(stream1, stream2);
    }

    private Stream<Neo4jVertex> getVerticesUsingOnlyLabels(final List<String> labels) {
        //System.out.println("labels: " + labels);
        final Neo4jGraph graph = (Neo4jGraph) this.getTraversal().getGraph().get();
        return labels.stream()
                .filter(label -> !label.equals(Neo4jVertexProperty.VERTEX_PROPERTY_LABEL))
                .flatMap(label -> StreamFactory.stream(graph.getBaseGraph().findNodes(label)))
                .filter(node -> !node.hasLabel(Neo4jVertexProperty.VERTEX_PROPERTY_LABEL))
                .filter(node -> ElementHelper.idExists(node.getId(), this.ids))
                .map(node -> new Neo4jVertex(node, graph));
    }

    private Stream<Neo4jVertex> getVerticesUsingAutomaticIndex(final HasContainer hasContainer) {
        //System.out.println("automatic index: " + hasContainer);
        final Neo4jGraph graph = (Neo4jGraph) this.getTraversal().getGraph().get();
        return StreamFactory.stream(graph.getBaseGraph().findNodes(hasContainer.key, hasContainer.value).iterator())
                .map(node -> node.hasLabel(Neo4jVertexProperty.VERTEX_PROPERTY_LABEL) ?
                        node.relationships(Neo4jDirection.INCOMING).iterator().next().start() :
                        node)
                .filter(node -> ElementHelper.idExists(node.getId(), this.ids))
                .map(node -> new Neo4jVertex(node, graph));
    }

    private Stream<Neo4jEdge> getEdgesUsingAutomaticIndex(final HasContainer hasContainer) {
        final Neo4jGraph graph = (Neo4jGraph) this.getTraversal().getGraph().get();
        return StreamFactory.stream(graph.getBaseGraph().findRelationships(hasContainer.key, hasContainer.value).iterator())
                .filter(relationship -> ElementHelper.idExists(relationship.getId(), this.ids))
                .filter(relationship -> !relationship.type().startsWith(Neo4jVertexProperty.VERTEX_PROPERTY_PREFIX))
                .map(relationship -> new Neo4jEdge(relationship, graph));
    }

    private Pair<String, HasContainer> getHasContainerForLabelIndex() {
        final Neo4jGraph graph = (Neo4jGraph) this.getTraversal().getGraph().get();
        Neo4jGraphAPI baseGraph = graph.getBaseGraph();
        for (final HasContainer hasContainer : this.hasContainers) {
            if (hasContainer.key.equals(T.label.getAccessor()) && hasContainer.predicate.equals(Compare.eq)) {
                if (baseGraph.hasSchemaIndex(
                        (String) hasContainer.value, hasContainer.key)) {
                    return Pair.with((String) hasContainer.value, hasContainer);
                }
            }
        }
        return null;
    }

    private List<String> getLabels() {
        for (final HasContainer hasContainer : this.hasContainers) {
            if (hasContainer.key.equals(T.label.getAccessor()) && hasContainer.predicate.equals(Compare.eq))
                return Arrays.asList(((String) hasContainer.value));
            else if (hasContainer.key.equals(T.label.getAccessor()) && hasContainer.predicate.equals(Contains.within))
                return new ArrayList<>((Collection<String>) hasContainer.value);
        }
        return null;
    }

    private HasContainer getHasContainerForAutomaticIndex(final Class<? extends Element> elementClass) {
        final Neo4jGraph graph = (Neo4jGraph) this.getTraversal().getGraph().get();
        Neo4jGraphAPI baseGraph = graph.getBaseGraph();
        boolean isNode = elementClass.equals(Vertex.class);
        for (final HasContainer hasContainer : this.hasContainers) {
            if (hasContainer.predicate.equals(Compare.eq) &&
                baseGraph.hasAutoIndex(isNode, hasContainer.key)) {
                return hasContainer;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        if (this.hasContainers.isEmpty())
            return super.toString();
        else
            return 0 == this.ids.length ?
                    TraversalHelper.makeStepString(this, this.hasContainers) :
                    TraversalHelper.makeStepString(this, Arrays.toString(this.ids), this.hasContainers);
    }

    /*private String makeCypherQuery() {
        final StringBuilder builder = new StringBuilder("MATCH node WHERE ");
        int counter = 0;
        for (final HasContainer hasContainer : this.hasContainers) {
            if (hasContainer.key.equals(T.label.getAccessor()) && hasContainer.predicate.equals(Compare.EQUAL)) {
                if (counter++ > 0) builder.append(" AND ");
                builder.append("node:").append(hasContainer.value);
            } else {
                if (counter++ > 0) builder.append(" AND ");
                builder.append("node.").append(hasContainer.key).append(" ");
                if (hasContainer.predicate instanceof Compare) {
                    builder.append(((Compare) hasContainer.predicate).asString()).append(" ").append(toStringOfValue(hasContainer.value));
                } else if (hasContainer.predicate.equals(Contains.IN)) {
                    builder.append("IN [");
                    for (Object object : (Collection) hasContainer.value) {
                        builder.append(toStringOfValue(object)).append(",");
                    }
                    builder.replace(builder.length() - 1, builder.length(), "").append("]");
                }
            }

        }
        System.out.println(builder);
        return builder.toString();
    }

    private String toStringOfValue(final Object value) {
        if (value instanceof String)
            return "'" + value + "'";
        else return value.toString();
    }*/
}
