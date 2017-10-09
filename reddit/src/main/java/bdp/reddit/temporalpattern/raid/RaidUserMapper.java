package bdp.reddit.temporalpattern.raid;

import bdp.reddit.util.RedditUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

/*This mapper is written to find user hate frequency on a particular hour of a day
the hour is not supplied as a command line argument or from static variable initially
* */

public class RaidUserMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {
    private static final long HOUR_OF_DAY = 19;

    private static final String AUTHOR_KEY = "author";
    private static final String BODY = "body";
    private static final String CREATED_UTC = "created_utc";

    private Gson gson = new Gson();
    private Type type = new TypeToken<Map<String, String>>(){}.getType();
    private static Map<String, String> HATE_DB = RedditUtils.getHateDb();

    @Override
    protected void map(LongWritable key, Text json, Context context)
            throws IOException, InterruptedException {

        Map<String, String> jsonMap = getMap(json);

        String authorName = jsonMap.get(AUTHOR_KEY);
        String bodyAsString = jsonMap.get(BODY);
        String createdTime = jsonMap.get(CREATED_UTC);

        if (StringUtils.isEmpty(createdTime) || StringUtils.isEmpty(authorName)) {
            return;
        }

        long hourOfDay = calculateHours(createdTime);

        if (hourOfDay != HOUR_OF_DAY) {
            return;
        }

        StringTokenizer tokenizer = new StringTokenizer(bodyAsString);
        double totalToken = 0;
        double hateTermCount = 0;

        while (tokenizer.hasMoreTokens()) {
            String  term = tokenizer.nextToken();

            if (HATE_DB.containsKey(term)) {
                hateTermCount++;
            }

            totalToken++;
        }

        if (hateTermCount == 0 || totalToken == 0) {
            return;
        }

        double hateTermFrequency = hateTermCount / totalToken;

        context.write(new Text(authorName), new DoubleWritable(hateTermFrequency));
    }

    private long calculateHours(String timestampString) {
        long timestamp = (Long.parseLong(timestampString) * 1000);
        Date date = new Date(timestamp);
        Calendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        c.setTime(date);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH");
        String hour = dateFormat.format(c.getTime());
        return Long.parseLong(hour);
    }

    private Map<String, String> getMap(Text json) {
        return gson.fromJson(json.toString(), type);
    }
}
