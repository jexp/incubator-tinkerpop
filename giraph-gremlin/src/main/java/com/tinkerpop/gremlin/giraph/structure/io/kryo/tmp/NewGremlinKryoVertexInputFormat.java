/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tinkerpop.gremlin.giraph.structure.io.kryo.tmp;

import com.google.common.collect.Lists;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.edge.EdgeFactory;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.util.List;

public class NewGremlinKryoVertexInputFormat extends
        NewTextVertexInputFormat<LongWritable, Text, NullWritable> {

    @Override
    public TextVertexReader createVertexReader(InputSplit split,
                                               TaskAttemptContext context) {
        return new NewGremlinKryoVertexReader();
    }

    class NewGremlinKryoVertexReader extends
            TextVertexReaderFromEachLineProcessedHandlingExceptions<com.tinkerpop.gremlin.structure.Vertex,
                    Exception> {

        @Override
        protected com.tinkerpop.gremlin.structure.Vertex preprocessLine(Text line) throws Exception {
            /* TODO
            final ByteArrayInputStream bis = new ByteArrayInputStream(line.getBytes());
            final KryoReader reader = KryoReader.create().build();
            Graph g = TinkerGraph.open();
            reader.readGraph(bis, g);
            bis.close();
            return g.v(   this.getId().get());
            */
            return null;
        }

        @Override
        protected LongWritable getId(com.tinkerpop.gremlin.structure.Vertex gremlinVertex) throws Exception {
            return new LongWritable(Long.valueOf(gremlinVertex.id().toString()));
        }

        @Override
        protected Text getValue(com.tinkerpop.gremlin.structure.Vertex line) throws Exception {
            // value is initially null
            return null;
        }

        @Override
        protected Iterable<Edge<LongWritable, NullWritable>> getEdges(com.tinkerpop.gremlin.structure.Vertex gremlinVertex) throws Exception {
            List<Edge<LongWritable, NullWritable>> edges =
                    Lists.newArrayListWithCapacity(gremlinVertex.inE().toList().size() + gremlinVertex.outE().toList().size());

            // note: ignore in-edges here

            for (com.tinkerpop.gremlin.structure.Edge e : gremlinVertex.outE().toList()) {
                Edge<LongWritable, NullWritable> edge = EdgeFactory.create(new LongWritable(Long.valueOf(e.id().toString())), NullWritable.get());
                edges.add(edge);
            }

            return edges;
        }

        @Override
        protected Vertex<LongWritable, Text, NullWritable,
                Text> handleException(Text line, com.tinkerpop.gremlin.structure.Vertex gremlinVertex,
                                      Exception e) {
            throw new IllegalArgumentException(
                    "Couldn't get vertex from line " + line, e);
        }
    }
}
