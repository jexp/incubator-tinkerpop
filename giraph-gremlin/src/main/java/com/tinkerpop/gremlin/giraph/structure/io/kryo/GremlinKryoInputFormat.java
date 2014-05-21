package com.tinkerpop.gremlin.giraph.structure.io.kryo;

import org.apache.giraph.io.VertexInputFormat;
import org.apache.giraph.io.VertexReader;
import org.apache.giraph.io.formats.GiraphTextInputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.db.DBInputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class GremlinKryoInputFormat extends VertexInputFormat {
    private static final Logger LOGGER = LoggerFactory.getLogger(GremlinKryoInputFormat.class);

    protected GiraphTextInputFormat textInputFormat = new GiraphTextInputFormat();

    public void checkInputSpecs(Configuration conf) {

    }

    public List<InputSplit> getSplits(final JobContext context,
                                      final int minSplitCountHint) throws IOException, InterruptedException {
        //return Arrays.<InputSplit>asList(new DBInputFormat.DBInputSplit());
        return textInputFormat.getVertexSplits(context);
    }

    public VertexReader createVertexReader(final InputSplit split,
                                           final TaskAttemptContext context) throws IOException {
        return new GremlinKryoVertexReader();
    }
}
