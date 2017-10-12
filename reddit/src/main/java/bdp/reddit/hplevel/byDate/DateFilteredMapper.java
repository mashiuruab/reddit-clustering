package bdp.reddit.hplevel.byDate;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateFilteredMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {
    private static final String BODY = "body";
    private static final String CREATED_UTC = "created_utc";

    @Override
    protected void map(LongWritable key, Text line, Context context)
            throws IOException, InterruptedException {

        String[] filteredParts =  line.toString().split("\t");

        Gson gson = new Gson();

        Map<String,Object> jsonMap = gson.fromJson(filteredParts[0], Map.class);
        double hateWordCount =  Double.parseDouble(filteredParts[1]);


        String bodyAsString = (String) jsonMap.get(BODY);
        String createdTime = (String) jsonMap.get(CREATED_UTC);

        if (StringUtils.isEmpty(createdTime)) {
            return;
        }

        String date = calculateDate(createdTime);

        StringTokenizer tokenizer = new StringTokenizer(bodyAsString);
        double totalToken = 0;

        while (tokenizer.hasMoreTokens()) {
            totalToken++;
        }

        if(totalToken == 0) {
            return;
        }

        double hateTermFrequency = hateWordCount / totalToken;


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

}

