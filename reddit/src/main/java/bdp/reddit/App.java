package bdp.reddit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        long timestamp = 1464739200000L;
        Date date = new Date(timestamp);
        Calendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        c.setTime(date);

        System.out.println(c.getTime());

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH");
        String hour = dateFormat.format(c.getTime());
        System.out.println(hour);
    }
}
