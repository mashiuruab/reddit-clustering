package bdp.reddit.combine;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class CombineMapper extends Mapper<LongWritable, Text, Text, Text> {
    @Override
    protected void map(LongWritable lineNumber, Text lineText, Context context)
            throws IOException, InterruptedException {
        String[] items = lineText.toString().split("\\s+");

        if (items.length != 2) {
            return;
        }

        context.write(new Text(items[0]), new Text(items[1]));
    }
}
