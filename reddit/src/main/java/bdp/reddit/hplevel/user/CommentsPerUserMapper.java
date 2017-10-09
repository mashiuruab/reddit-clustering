package bdp.reddit.hplevel.user;

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
import java.util.Map;
import java.util.StringTokenizer;

public class CommentsPerUserMapper  extends Mapper<LongWritable, Text, Text, DoubleWritable>{
    private static final String AUTHOR_KEY = "author";
    private static final String BODY_KEY = "body";

    private Gson gson = new Gson();
    private Type type = new TypeToken<Map<String, String>>(){}.getType();
    private static Map<String, String> HATE_DB = RedditUtils.getHateDb();

    @Override
    protected void map(LongWritable key, Text json, Context context) throws IOException, InterruptedException {
        Map<String, String> jsonMap = getMap(json);
        String bodyAsString = jsonMap.get(BODY_KEY);
        String authorName = jsonMap.get(AUTHOR_KEY);

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

        if(hateTermCount == 0 ||  totalToken == 0) {
            return;
        }

        double hateTermFrequency = hateTermCount / totalToken;

        context.write(new Text(authorName), new DoubleWritable(hateTermFrequency));
    }

    private Map<String, String> getMap(Text json) {
        return gson.fromJson(json.toString(), type);
    }
}
