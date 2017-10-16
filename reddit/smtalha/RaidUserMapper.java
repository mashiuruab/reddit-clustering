package com.reddithate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TimeZone;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.google.gson.Gson;

public class RaidUserMapper extends Mapper<Object, Text, Text, DoubleWritable> {
	/*topmost : 12
	* same  : 2, 18, 21, 22*/

	private static final long TOP_HOUR_OF_DAY = 12;
	private static final Long[] targetHours = new Long[]{2L, 18L, 21L, 22L};
	
	protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {

		String [] arr = value.toString().split("\t");
		
		Gson gson = new Gson();
		
		Map<String,Object> submission = gson.fromJson(arr[0].toString(), Map.class);
		double hateTermCount = Double.parseDouble(arr[1]);
		
		if (submission.get("author") == null || submission.get("body") == null || submission.get("created_utc") == null) {
			return;
		}

        String authorName = submission.get("author").toString();
        String bodyAsString = submission.get("body").toString();
        String createdTime = submission.get("created_utc").toString();


        long hourOfDay = calculateHours(createdTime);

        ///>>>>>>>>>>>>>>>>>>>>>>>>>>>
        /*comment out this  portion and uncomment the next one for next pass*/
        if (hourOfDay != TOP_HOUR_OF_DAY) {
            return;
        }


        ////>>>>>>>>>>>>>>>>>>>>>>>>>>>

        //////////////uncomment following  portion for next pass
        /*boolean targetHourDocument = false;

        for(Long hour : targetHours) {
            if (hourOfDay == hour) {
                targetHourDocument = true;
                break;
            }
        }

        if (!targetHourDocument) {
            return;
        }*/

        double total = bodyAsString.length() / 4;
		
		if (total == 0) {
			return;
		}

        double hateTermFrequency = hateTermCount / total;

        context.write(new Text(authorName), new DoubleWritable(hateTermFrequency));
    }
	
	private long calculateHours(String timestampString) {
		long timestamp = (Double.valueOf(timestampString).longValue() * 1000);
        Date date = new Date(timestamp);
        Calendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        c.setTime(date);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH");
        String hour = dateFormat.format(c.getTime());
        return Long.parseLong(hour);
    }
}
