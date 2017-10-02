package bdp.reddit.mapwritable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uttesh.exude.ExudeData;
import com.uttesh.exude.exception.InvalidDataException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


public class MapMultipleValueMapers extends Mapper<LongWritable, Text, Text, MapWritable> {
    public static final String CONTENTS = "contents";
    public static final String FILE_NAME = "filename";
    public static final String FILE_PATH = "filepath";
    public static final int MAX_SEARCH = 10;

    private static final String BODY = "body";
    private static final String PARENT_ID = "parent_id";
    private static final String CREATED_UTE = "created_ute";
    private static final String AUTHOR = "author";
    private static final String SUBREDDIT = "subreddit";
    private static final String SUBREDDIT_ID = "subreddit_id";
    private static final String RETRIEVED_ON = "retrieved_on";
    private static final String ID = "id";


    private static Map<String, String> HATE_DB = new HashMap<>();

    static {
        try {
            InputStream inputStream =  MapMultipleValueMapers.class.getClassLoader().getResourceAsStream("vocabulary.csv");
            Reader in = new InputStreamReader(inputStream);
            Iterable<CSVRecord> records = CSVFormat.EXCEL.withDelimiter(';').parse(in);
            for (CSVRecord record : records) {
                String vocabulary = record.get(0);
                String variant_of = record.get(1);

                HATE_DB.put(vocabulary, "");
                HATE_DB.put(variant_of, "");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void map(LongWritable lineNumber, Text value, Context context) throws IOException, InterruptedException {
        Text subRedditId  = new Text();
        MapWritable lineTermMap = new MapWritable();
        DoubleWritable one =  new DoubleWritable(1);
        MapWritable termFrequencyMap  =  new MapWritable();

        Map<String, String> jsonMap = get(value);

        String bodyAsString = removeStopWords(jsonMap.get(BODY));

        MapWritable  termMap = new MapWritable();

        StringTokenizer tokenizer = new StringTokenizer(bodyAsString);

        int totalToken = 0;

        while (tokenizer.hasMoreTokens()) {
            String  term = tokenizer.nextToken();

            if (HATE_DB.containsKey(term)) {
                Text termToInsert = new Text(term);
                if (termMap.containsKey(termToInsert)) {
                    DoubleWritable counter = (DoubleWritable) termMap.get(termToInsert);
                    double totalCount =  counter.get() + 1;
                    termMap.put(termToInsert, new DoubleWritable(totalCount));
                } else {
                    termMap.put(termToInsert, one);
                }
            }

            totalToken++;
        }


        for(Map.Entry<Writable, Writable> termPair : termMap.entrySet()) {
            double tf = ((DoubleWritable) termPair.getValue()).get() / totalToken;
            termFrequencyMap.put(termPair.getKey(),  new DoubleWritable(tf));
            System.out.println(String.format("key = %s  , value = %s", termPair.getKey(), tf));
        }

        lineTermMap.put(lineNumber, termFrequencyMap);
        subRedditId.set(jsonMap.get(SUBREDDIT_ID));

        System.out.println("Debug..............");
        System.out.println(new Gson().toJson(lineTermMap));

        context.write(subRedditId, lineTermMap);
    }

    private Map<String, String> get(Text json) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        return gson.fromJson(json.toString(), type);
    }

    private String removeStopWords(String bodyText) {
        String cleanString = "";
        try {
            cleanString = ExudeData.getInstance().filterStoppings(bodyText);
        } catch (InvalidDataException  ivle) {
            throw new RuntimeException(ivle);
        }

        return cleanString;
    }
}
