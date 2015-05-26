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
package org.apache.tinkerpop.gremlin.neo4j;

import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

/**
 * This should only be used for Neo4j-specific testing that is not related to the Gremlin test suite.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class BaseNeo4jGraphTest {
    protected Configuration conf;
    protected final DefaultNeo4jGraphProvider graphProvider = new DefaultNeo4jGraphProvider();
    protected Neo4jGraph graph;
    protected GraphTraversalSource g;

    @Rule
    public TestName name = new TestName();

    @Before
    public void before() throws Exception {
        // tests that involve legacy indices need legacy indices turned on at startup of the graph.
        final Map<String, Object> neo4jSettings = new HashMap<>();
        if (name.getMethodName().contains("NoMultiProperties"))
            neo4jSettings.put(Neo4jGraph.CONFIG_MULTI_PROPERTIES, false);
        if (name.getMethodName().contains("NoMetaProperties"))
            neo4jSettings.put(Neo4jGraph.CONFIG_META_PROPERTIES, false);
        if (name.getMethodName().contains("Legacy")) {
            neo4jSettings.put("gremlin.neo4j.conf.node_auto_indexing", "true");
            neo4jSettings.put("gremlin.neo4j.conf.relationship_auto_indexing", "true");
        }

        this.conf = neo4jSettings.size() == 0 ?
                this.graphProvider.newGraphConfiguration("standard", this.getClass(), name.getMethodName()) :
                this.graphProvider.newGraphConfiguration("standard", this.getClass(), name.getMethodName(), neo4jSettings);

        this.graphProvider.clear(this.conf);
        this.graph = Neo4jGraph.open(this.conf);
        this.g = this.graph.traversal();

    }

    @After
    public void after() throws Exception {
        this.graphProvider.clear(this.graph, this.conf);
    }

    protected void tryCommit(final Neo4jGraph g, final Consumer<Neo4jGraph> assertFunction) {
        assertFunction.accept(g);
        if (g.features().graph().supportsTransactions()) {
            g.tx().commit();
            assertFunction.accept(g);
        }
    }

    protected static int countIterable(final Iterable iterable) {
        int count = 0;
        for (Object object : iterable) {
            count++;
        }
        return count;
    }

    protected static void validateCounts(final Neo4jGraph graph, int gV, int gE, int gN, int gR) {
        assertEquals(gV, IteratorUtils.count(graph.vertices()));
        assertEquals(gE, IteratorUtils.count(graph.edges()));
        assertEquals(gN, countIterable(graph.getBaseGraph().allNodes()));
        assertEquals(gR, countIterable(graph.getBaseGraph().allRelationships()));
    }
}
