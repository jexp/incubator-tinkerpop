package com.tinkerpop.gremlin.giraph.structure.io.kryo;

import com.tinkerpop.gremlin.giraph.structure.GiraphVertex;
import com.tinkerpop.gremlin.structure.io.kryo.KryoReader;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.io.VertexReader;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class GremlinKryoVertexReader extends VertexReader {

    protected Iterator<com.tinkerpop.gremlin.structure.Vertex> vertices = Collections.emptyIterator();
    private Vertex currentVertex = null;
    private float progress = 0f;

    public void initialize(final InputSplit inputSplit,
                           final TaskAttemptContext context) throws IOException, InterruptedException {
        System.out.println("got InputSplit: " + inputSplit + " (" + (null == inputSplit ? "null" : inputSplit.getClass()) + ")");
        System.out.println("prop map.input.file: " + context.getConfiguration().get("map.input.file"));
        System.out.println("prop map.input.start: " + context.getConfiguration().get("map.input.start"));
        System.out.println("prop map.input.length: " + context.getConfiguration().get("map.input.length"));

        //((DBInputFormat.DBInputSplit) inputSplit).readFields(input);

        DataOutput output = new DataOutput() {
            public void write(int b) throws IOException {
                System.out.println("write int: " + b);
            }

            public void write(byte[] b) throws IOException {
                System.out.println("write byte[]: " + new String(b));
            }

            public void write(byte[] b, int off, int len) throws IOException {
                System.out.println("write byte[] int int: " + new String(b) + " " + off + " " + len);
            }

            public void writeBoolean(boolean v) throws IOException {
                System.out.println("writeBoolean : " + v);
            }

            public void writeByte(int v) throws IOException {
                System.out.println("writeByte : " + v);
            }

            public void writeShort(int v) throws IOException {
                System.out.println("writeShort : " + v);
            }

            public void writeChar(int v) throws IOException {
                System.out.println("writeChar : " + v);
            }

            public void writeInt(int v) throws IOException {
                System.out.println("writeInt : " + v);
            }

            public void writeLong(long v) throws IOException {
                System.out.println("writeLong : " + v);
            }

            public void writeFloat(float v) throws IOException {
                System.out.println("writeFloat : " + v);
            }

            public void writeDouble(double v) throws IOException {
                System.out.println("writeDouble : " + v);
            }

            public void writeBytes(String s) throws IOException {
                System.out.println("writeBytes : " + s);
            }

            public void writeChars(String s) throws IOException {
                System.out.println("writeChars : " + s);
            }

            public void writeUTF(String s) throws IOException {
                System.out.println("writeUTF : " + s);
            }
        };

        /*
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (null != inputSplit && inputSplit instanceof FileSplit) {
            System.out.println("inputsplit length = " + inputSplit.getLength());
            ((FileSplit) inputSplit).write(output);
            ((FileSplit) inputSplit).write(new DataOutputStream(bos));
        }
        bos.close();
        System.out.println("bos.size... = " + bos.toByteArray().length);
        ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
        System.out.println("bin available... = " + bin.available());
        */

        final FileSystem fileSystem = FileSystem.get(context.getConfiguration());

        KryoReader reader = KryoReader.create().build();
        final TinkerGraph g = TinkerGraph.open();

        //Path p = new Path(context.getConfiguration().get(GiraphGraphComputer.GREMLIN_INPUT_LOCATION));
        Path p = ((FileSplit) inputSplit).getPath();

        try (InputStream in = fileSystem.open(p)) {
            reader.readGraph(in, g);
        }

        this.vertices = g.V();
    }

    public boolean nextVertex() throws IOException, InterruptedException {
        System.out.println("nextVertex");
        if (this.vertices.hasNext()) {
            currentVertex = new GiraphVertex(this.vertices.next());
            progress = 0.5f;
            return true;
        } else {
            System.out.println("finished reading vertices in GremlinKryoVertexReader");
            currentVertex = null;
            progress = 1f;
            return false;
        }
    }

    public Vertex getCurrentVertex() throws IOException, InterruptedException {
        return currentVertex;
    }

    public void close() throws IOException {
        System.out.println("closing GremlinKryoVertexReader");
        // nothing to do
    }

    public float getProgress() throws IOException, InterruptedException {
        return progress;
    }
}
