package com.tinkerpop.gremlin.process.graph.map;

import com.tinkerpop.gremlin.process.Path;
import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.process.util.PathConsumer;
import com.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Vertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Daniel Kuppitz <daniel at thinkaurelius.com>
 */
public class OtherVertexStep extends MapStep<Edge, Vertex> implements PathConsumer {

    public OtherVertexStep(final Traversal traversal) {
        super(traversal);
        this.setFunction(holder -> {
            final Edge edge = holder.get();
            final Path path = holder.getPath();
            final Vertex prev = path.get(path.size() - 2);
            final Vertex other = edge.getVertex(Direction.IN);
            return other.equals(prev) ? edge.getVertex(Direction.OUT) : other;
        });
    }
}