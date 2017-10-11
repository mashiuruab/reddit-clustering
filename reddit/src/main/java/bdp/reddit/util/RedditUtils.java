package bdp.reddit.util;

import bdp.reddit.KMeans.tfidf.MapMultipleValueMapers;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RedditUtils {
    private static RedditUtils instance;

    private RedditUtils() {
        init();
    }

    private void init() {
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

    private Map<String, String> HATE_DB = new HashMap<>();

    public Map<String, String> getHateDb() {
        return HATE_DB;
    }

    public static RedditUtils getInstance() {
        if (instance == null) {
            instance =  new RedditUtils();
        }

        return instance;
    }

}
