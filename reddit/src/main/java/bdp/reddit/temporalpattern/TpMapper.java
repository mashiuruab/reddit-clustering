package bdp.reddit.temporalpattern;

import bdp.reddit.util.RedditUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public class TpMapper extends Mapper<LongWritable, Text, LongWritable, DoubleWritable> {
    private static final String BODY = "body";
    private static final String CREATED_UTE = "created_ute";

    private Gson gson = new Gson();
    private Type type = new TypeToken<Map<String, String>>(){}.getType();
    private static Map<String, String> HATE_DB = RedditUtils.getHateDb();


    @Override
    protected void map(LongWritable lineNumber, Text json, Context context)
            throws IOException, InterruptedException {
        Map<String, String> jsonMap = getMap(json);
        String bodyAsString = jsonMap.get(BODY);
        long hourOfDay = calculateHours(jsonMap.get(CREATED_UTE));

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

        double hateTermFrequency = hateTermCount / totalToken;

        context.write(new LongWritable(hourOfDay), new DoubleWritable(hateTermFrequency));
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