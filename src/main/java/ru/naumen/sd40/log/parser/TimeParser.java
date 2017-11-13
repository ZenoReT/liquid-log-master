package ru.naumen.sd40.log.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by doki on 22.10.16.
 */
public class TimeParser implements ITimeParser{
    private static final Pattern TIME_PATTERN = Pattern
            .compile("^\\d+ \\[.*?\\] \\((\\d{2} .{3} \\d{4} \\d{2}:\\d{2}:\\d{2},\\d{3})\\)");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy HH:mm:ss,SSS",
            new Locale("ru", "RU"));

    public TimeParser()
    {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public TimeParser(String zoneId)
    {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(zoneId));
    }
    
	public void parse(HashMap<Long, DataSet> data, String timeZone, MultipartFile logs)
													throws IOException, ParseException {
		TimeParser timeParser = new TimeParser(timeZone);
    	try (BufferedReader br = new BufferedReader(new InputStreamReader(logs.getInputStream())))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
				long time = timeParser.parseTime(line);

                if (time == 0)
                {
                    continue;
                }

                int min5 = 5 * 60 * 1000;
                long count = time / min5;
                long key = count * min5;
                
                data.computeIfAbsent(key, k -> new DataSet()).parseLine(line);                	
            }
        }
	}

	public long parseTime(String line) throws ParseException {
        Matcher matcher = TIME_PATTERN.matcher(line);

        if (matcher.find())
        {
            String timeString = matcher.group(1);
            Date recDate = DATE_FORMAT.parse(timeString);
            return recDate.getTime();
        }
        return 0L;
	}
}
