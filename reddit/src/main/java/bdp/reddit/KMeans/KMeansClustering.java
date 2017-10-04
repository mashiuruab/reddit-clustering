package bdp.reddit.KMeans;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KMeansClustering {
    private static final String JOB_NAME = "KMeans";
    private static final String SPLITTER = "\\s+";
    private static String HDFS_BASE_DIR =
            "/home/mashiur/projects/reddit-clustering/reddit/src/main/resources/k-means/";
    private static String DATA_FILE_NAME = "map-input.txt";
    private static String OUT_DIR = "k-means-out";
    private static String CENTER_FILE_NAME = "centroids.txt";


    public static class Map extends MapReduceBase
            implements Mapper<LongWritable, Text, DoubleWritable, Text> {

        List<Double> centroidList = new ArrayList<>();

        @Override
        public void configure(JobConf job) {
            try {
                Path centroidFilePath = new Path(HDFS_BASE_DIR, CENTER_FILE_NAME);

                BufferedReader reader = new BufferedReader(
                        new FileReader(centroidFilePath.toUri().toString()));
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] temp = line.split(SPLITTER);
                        centroidList.add(Double.parseDouble(temp[0]));
                    }
                } finally {
                    reader.close();
                }
            } catch (Exception ioe) {
                throw new RuntimeException(ioe);
            }
        }

        @Override
        public void map(LongWritable lineNumber, Text redditVector,
                        OutputCollector<DoubleWritable, Text> outputCollector, Reporter reporter)
                throws IOException {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.println(String.format("Mapper for Line %s", lineNumber));
            System.out.println(String.format("Reddit vector = %s", redditVector));

            for(Double centers : centroidList) {
                System.out.println(centers);
            }
        }
    }

    public static class Reduce extends MapReduceBase
            implements Reducer<DoubleWritable, Text, DoubleWritable, Text> {

        @Override
        public void reduce(DoubleWritable doubleWritable, Iterator<Text> iterator,
                           OutputCollector<DoubleWritable, Text> outputCollector, Reporter reporter)
                throws IOException {

        }
    }

    public static void main(String[] args) throws Exception {
        JobConf conf = new JobConf(KMeansClustering.class);

        conf.setJobName(JOB_NAME);
        conf.setMapOutputKeyClass(DoubleWritable.class);
        conf.setMapOutputValueClass(Text.class);
        conf.setOutputKeyClass(DoubleWritable.class);
        conf.setOutputValueClass(Text.class);
        conf.setMapperClass(Map.class);
        conf.setReducerClass(Reduce.class);
        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(conf,
                new Path(HDFS_BASE_DIR + DATA_FILE_NAME));
        FileOutputFormat.setOutputPath(conf, new Path(HDFS_BASE_DIR, OUT_DIR));

        JobClient.runJob(conf);
    }
}
