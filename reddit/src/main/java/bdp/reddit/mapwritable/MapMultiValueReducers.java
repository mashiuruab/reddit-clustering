package bdp.reddit.mapwritable;

import com.google.gson.Gson;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapMultiValueReducers extends Reducer<Text, MapWritable, Text, Text> {
    Map<String, List> tfIdfVector;
    int totalNumberOfDocuments;

    @Override
    protected void reduce(Text redditId, Iterable<MapWritable> values, Context context) throws IOException, InterruptedException {
        setTFIDF(redditId, values);


        String jsonString = new Gson().toJson(tfIdfVector);
        context.write(redditId, new Text(jsonString));
    }

    private void setTFIDF(Text redditId, Iterable<MapWritable> docList) {
        tfIdfVector = new HashMap<>();
        totalNumberOfDocuments = 0;

        Map<String, TfDocCountContainer> termDocCountMap = new HashMap<>();
        Map<String, Double> TF = new HashMap<>();
        Map<String, Double> IDF =  new HashMap<>();
        TfDocCountContainer container;

        for(MapWritable docMap : docList) {
            /*document iteration*/
            for(Map.Entry<Writable, Writable> entry : docMap.entrySet()) {
                LongWritable lineNumber =  (LongWritable) entry.getKey();
                MapWritable individualTFMap = (MapWritable) entry.getValue();



                for(Map.Entry<Writable, Writable> tfEntry : individualTFMap.entrySet()) {
                    Text termKey = (Text) tfEntry.getKey();
                    DoubleWritable termFreqValue = (DoubleWritable) tfEntry.getValue();


                    if (termDocCountMap.containsKey(termKey.toString())) {
                        container = termDocCountMap.get(termKey.toString());
                        container.setAggregatedTF(container.getAggregatedTF() + termFreqValue.get());
                        container.setTotalDoc(container.getTotalDoc() + 1);

                    } else {
                        container = new TfDocCountContainer();
                        container.setTotalDoc(1);
                        container.setAggregatedTF(termFreqValue.get());
                    }


                    termDocCountMap.put(termKey.toString(),  container);
                }
                totalNumberOfDocuments++;
            }
        }

        for(Map.Entry<String, TfDocCountContainer> entry : termDocCountMap.entrySet()) {
            TfDocCountContainer valueContainer = entry.getValue();
            double termFrequency =  valueContainer.getAggregatedTF() / valueContainer.getTotalDoc();
            TF.put(entry.getKey(), termFrequency);
        }


        for(Map.Entry<String, TfDocCountContainer> entry : termDocCountMap.entrySet()) {
            double idfValue = totalNumberOfDocuments / entry.getValue().getTotalDoc();
            idfValue = Math.log10(idfValue);
            IDF.put(entry.getKey(), idfValue);
        }

        System.out.println("Debug................");
        System.out.println(TF);
        System.out.println(IDF);

        List<Double> tfidfList = new ArrayList<>();

        for(String key : TF.keySet()) {
            double value = TF.get(key) * IDF.get(key);
            tfidfList.add(value);
        }

        tfIdfVector.put(redditId.toString(),  tfidfList);

    }
}
