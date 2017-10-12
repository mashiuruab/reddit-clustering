package bdp.reddit.hplevel.user;

import com.google.gson.Gson;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;

public class CPUserFilteredMapper extends Mapper<Object, Text, Text, DoubleWritable> {
    private static final String AUTHOR_KEY = "author";
    private static final String BODY_KEY = "body";

    @Override
    protected void map(Object key, Text line, Context context)
            throws IOException, InterruptedException {
        String[] filteredParts =  line.toString().split("\t");

        Gson gson = new Gson();

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(filteredParts[0]);

        Map<String,Object> jsonMap = gson.fromJson(filteredParts[0], Map.class);
        double hateWordCount =  Double.parseDouble(filteredParts[1]);

        String bodyAsString = (String) jsonMap.get(BODY_KEY);
        String authorName = (String) jsonMap.get(AUTHOR_KEY);

        StringTokenizer tokenizer = new StringTokenizer(bodyAsString);
        double totalToken = 0;

        while (tokenizer.hasMoreTokens()) {
            totalToken++;
        }

        if(totalToken == 0) {
            return;
        }

        double hateTermFrequency = hateWordCount / totalToken;

        context.write(new Text(authorName), new DoubleWritable(hateTermFrequency));
    }
}

