package bdp.reddit.combine;

/*This is written mainly to combine all of the job result
* suppose result of comments and submission would be collected
* separately and then we need to merge those result to get the final
* data set. Then using those data set we would plot the graph
* */

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class CombineMain {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();

        Job job = new Job(conf, "Generic Combiner");

        job.setJarByClass(CombineMain.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setMapperClass(CombineMapper.class);
        job.setReducerClass(CombineReducer.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);


        for(int counter = 0; counter < args.length - 1; counter++) {
            MultipleInputs.addInputPath(job, new Path(args[counter]),
                    TextInputFormat.class, CombineMapper.class);
        }

        FileSystem.get(conf).delete(new Path(args[args.length - 1]), true);
        FileOutputFormat.setOutputPath(job, new Path(args[args.length - 1]));

        job.waitForCompletion(true);
    }
}
