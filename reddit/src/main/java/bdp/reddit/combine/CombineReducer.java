package bdp.reddit.combine;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class CombineReducer extends Reducer<Text, Text, Text, Text> {
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
        double sumFrequency = 0; /// most of the case it would be sum of frequency
        double totalItemToMerge = 0;

        for(Text value : values) {
            sumFrequency += Double.parseDouble(value.toString());
            totalItemToMerge++;
        }

        /*Though this case would never happen. Just doing extra checking
        * to avoid any kind of uncertain situation
        * */

        if (sumFrequency == 0 || totalItemToMerge == 0) {
            return;
        }

        double calculatedFreq = sumFrequency / totalItemToMerge;

        context.write(key, new Text(String.valueOf(calculatedFreq)));
    }
}
