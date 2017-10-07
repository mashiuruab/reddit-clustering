package bdp.reddit.hplevel.user;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class CommentsPerUserReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable>{
    @Override
    protected void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException {
        double totalComment = 0;
        double totalFreq = 0;
        for(DoubleWritable hateFreq : values) {
            totalFreq += hateFreq.get();
            totalComment++;
        }

        double hateFreqPerUser = totalFreq / totalComment;

        if (hateFreqPerUser == 0) {
            return;
        }

        context.write(key, new DoubleWritable(hateFreqPerUser));
    }
}