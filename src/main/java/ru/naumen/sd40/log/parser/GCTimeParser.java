package ru.naumen.sd40.log.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GCTimeParser implements ITimeParser{
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            new Locale("ru", "RU"));

    private static final Pattern PATTERN = Pattern
            .compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4}).*");

    public GCTimeParser(){
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public GCTimeParser(String timeZone){
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(timeZone));
    }
    
    public long parseTime(String line) throws ParseException{
        Matcher matcher = PATTERN.matcher(line);
        if (matcher.find()){
            Date parse = DATE_FORMAT.parse(matcher.group(1));
            return parse.getTime();
        }
        return 0L;
    }
}