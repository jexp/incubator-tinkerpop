package com.tinkerpop.gremlin.giraph.structure;

import com.tinkerpop.gremlin.giraph.process.olap.GiraphGraphComputerSideEffects;
import com.tinkerpop.gremlin.giraph.process.olap.KryoWritable;
import com.tinkerpop.gremlin.giraph.structure.io.EmptyOutEdges;
import com.tinkerpop.gremlin.process.computer.VertexProgram;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.io.kryo.KryoReader;
import com.tinkerpop.gremlin.structure.io.kryo.KryoWriter;
import com.tinkerpop.gremlin.structure.util.ElementHelper;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.apache.giraph.conf.LongConfOption;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class GiraphVertex extends Vertex<LongWritable, Text, NullWritable, KryoWritable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GiraphVertex.class);

    private VertexProgram vertexProgram;
    private Graph gremlinGraph;
    private com.tinkerpop.gremlin.structure.Vertex gremlinVertex;
    private GiraphGraphComputerSideEffects computerMemory = new GiraphGraphComputerSideEffects(this);

    public GiraphVertex() {
    }

    public GiraphVertex(final com.tinkerpop.gremlin.structure.Vertex gremlinVertex) {
        this.gremlinGraph = TinkerGraph.open();
        this.gremlinVertex = gremlinVertex;
        final com.tinkerpop.gremlin.structure.Vertex vertex = this.gremlinGraph.addVertex(Element.ID, Long.valueOf(this.gremlinVertex.id().toString()), Element.LABEL, this.gremlinVertex.label());
        this.gremlinVertex.properties().forEach((k, v) -> vertex.property(k, v.get()));
        this.gremlinVertex.outE().forEach(edge -> {
            final com.tinkerpop.gremlin.structure.Vertex otherVertex = ElementHelper.getOrAddVertex(this.gremlinGraph, edge.inV().id().next(), edge.inV().label().next());
            final Edge gremlinEdge = vertex.addEdge(edge.label(), otherVertex);
            edge.properties().forEach((k, v) -> gremlinEdge.property(k, v.get()));
        });
        this.gremlinVertex.inE().forEach(edge -> {
            final com.tinkerpop.gremlin.structure.Vertex otherVertex = ElementHelper.getOrAddVertex(this.gremlinGraph, edge.outV().id().next(), edge.outV().label().next());
            final Edge gremlinEdge = otherVertex.addEdge(edge.label(), vertex);
            edge.properties().forEach((k, v) -> gremlinEdge.property(k, v.get()));
        });
        this.initialize(new LongWritable(Long.valueOf(this.gremlinVertex.id().toString())), this.getTextOfSubGraph(), EmptyOutEdges.instance());
    }

        /* TODO: restore me
    @Override
    public void setConf(final org.apache.giraph.conf.ImmutableClassesGiraphConfiguration configuration) {
        this.vertexProgram = VertexProgram.createVertexProgram(ConfUtil.apacheConfiguration(configuration));
    }
        */

    public com.tinkerpop.gremlin.structure.Vertex getGremlinVertex() {
        return this.gremlinVertex;
    }

    /* TODO: restore me
    public void compute(final Iterable<KryoWritable> messages) {
        inflateGiraphVertex();
        this.vertexProgram.execute(this.gremlinVertex, new GiraphMessenger(this, messages), this.computerMemory);
    }*/

    public void compute(Iterable<KryoWritable> messages) {
        if (getSuperstep() == 0) {
            setValue(new Text(String.valueOf(Double.MAX_VALUE)));
        }
        double minDist = isSource() ? 0d : Double.MAX_VALUE;
        for (KryoWritable message : messages) {
            minDist = Math.min(minDist, (double) message.get());
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Vertex " + getId() + " got minDist = " + minDist +
                    " vertex value = " + getValue());
        }
        if (minDist < Double.valueOf(getValue().toString())) {
            setValue(new Text(String.valueOf(minDist)));
            for (org.apache.giraph.edge.Edge<LongWritable, NullWritable> edge : getEdges()) {
                double distance = minDist + 1;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Vertex " + getId() + " sent to " +
                            edge.getTargetVertexId() + " = " + distance);
                }
                sendMessage(edge.getTargetVertexId(), new KryoWritable(distance));
            }
        }
        voteToHalt();
    }
    public static final LongConfOption SOURCE_ID =
            new LongConfOption("SimpleShortestPathsVertex.sourceId", 1);
    private boolean isSource() {
        return getId().get() == SOURCE_ID.get(getConf());
    }

    ///////////////////////////////////////////////

    private Text getTextOfSubGraph() {
        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final KryoWriter writer = KryoWriter.create().build();
            writer.writeGraph(bos, this.gremlinGraph);
            bos.flush();
            bos.close();
            return new Text(bos.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void inflateGiraphVertex() {
        if (null == this.gremlinVertex) {
            try {
                final ByteArrayInputStream bis = new ByteArrayInputStream(this.getValue().getBytes());
                final KryoReader reader = KryoReader.create().build();
                this.gremlinGraph = TinkerGraph.open();
                reader.readGraph(bis, this.gremlinGraph);
                bis.close();
                this.gremlinVertex = this.gremlinGraph.v(this.getId().get());
            } catch (final Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    // TODO: Move back to read/writeVertex
}
