package bdp.reddit.util;

import bdp.reddit.mapwritable.MapMultipleValueMapers;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class RedditUtils {
    private RedditUtils() {

    }

    private static Map<String, String> HATE_DB = new HashMap<>();

    public static Map<String, String> getHateDb() {
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

        return HATE_DB;
    }
}
