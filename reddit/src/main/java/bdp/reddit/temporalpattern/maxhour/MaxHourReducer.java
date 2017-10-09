package bdp.reddit.temporalpattern.maxhour;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class MaxHourReducer extends Reducer<Text, Text, Text, Text> {
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
        String hourOfDay = "";
        Double maxFrequency = Double.MIN_VALUE;

        for (Text line : values) {
            String[] items = line.toString().split("\\s+");
            double freqValue = Double.parseDouble(items[1]);

            if (freqValue > maxFrequency) {
                hourOfDay = items[0];
                maxFrequency = Double.parseDouble(items[1]);
            }
        }

        context.write(new Text(hourOfDay), new Text(String.valueOf(maxFrequency)));
    }
}
