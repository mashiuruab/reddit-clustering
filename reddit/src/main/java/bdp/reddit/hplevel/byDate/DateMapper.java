package bdp.reddit.hplevel.byDate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {
    private static final String BODY = "body";
    private static final String CREATED_UTC = "created_utc";

    private static String hateDictionaryPath = "/user/mashiur/vocabulary.txt";
    private static String [] hateWords;

    @Override
    protected void map(LongWritable key, Text json, Context context)
            throws IOException, InterruptedException {
        hateWords = readHateWordsFromFile(hateDictionaryPath, context);

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

            if (Arrays.binarySearch(hateWords,  term) >= 0) {
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

    private static String[] readHateWordsFromFile(String path, Context context) {
        ArrayList<String> words = new ArrayList<String>();

        if (hateWords != null && hateWords.length > 0) {
            return hateWords;
        }

        try {
            Path pt=new Path(path);
            FileSystem fs = FileSystem.get(context.getConfiguration());
            BufferedReader br =new BufferedReader(new InputStreamReader(fs.open(pt)));

            String line = "";

            while ((line = br.readLine()) != null) {
                String[] vocab  = line.split(";");
                words.add(vocab[0].toLowerCase().trim());
            }
            String [] arr = new String[words.size()];
            br.close();
            return words.toArray(arr);
        }
        catch (Exception ex) {
            return null;
        }
    }
}
