package bdp.reddit.temporalpattern;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class TpReducer extends Reducer<LongWritable, DoubleWritable, LongWritable, DoubleWritable> {
    @Override
    protected void reduce(LongWritable key, Iterable<DoubleWritable> freqValues, Context context)
            throws IOException, InterruptedException {
        double totalFrequency = 0;
        double totalDocument = 0;

        for(DoubleWritable freqValue : freqValues) {
            totalFrequency += freqValue.get();
            totalDocument++;
        }

        if (totalFrequency == 0 || totalDocument == 0) {
            return;
        }

        double hateWordsFreq = totalFrequency / totalDocument;

        context.write(key, new DoubleWritable(hateWordsFreq));
    }
}
