package bdp.reddit.hplevel.byDate;

import bdp.reddit.KMeans.tfidf.MapMultipleValueMapers;
import bdp.reddit.util.RedditUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DateMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {
    private static final String BODY = "body";
    private static final String CREATED_UTC = "created_utc";

    private static Map<String, String> HATE_DB = RedditUtils.getInstance().getHateDb();

    @Override
    protected void map(LongWritable key, Text json, Context context) throws IOException, InterruptedException {
        Map<String, String> jsonMap = getMap(json);
        String bodyAsString = jsonMap.get(BODY);
        String createdTime = jsonMap.get(CREATED_UTC);
        if (StringUtils.isEmpty(createdTime)) {
            return;
        }

        String date = calculateDate(createdTime);

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

        if (hateTermCount  == 0 || totalToken == 0) {
            return;
        }

        double hateTermFrequency = hateTermCount / totalToken;


        context.write(new Text(date), new DoubleWritable(hateTermFrequency));
    }

    private String calculateDate(String timestampString) {
        long timestamp = (Long.parseLong(timestampString) * 1000);
        Date date = new Date(timestamp);
        Calendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        c.setTime(date);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy");
        return dateFormat.format(c.getTime());
    }

    private Map<String, String> getMap(Text json) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        return gson.fromJson(json.toString(), type);
    }
}
