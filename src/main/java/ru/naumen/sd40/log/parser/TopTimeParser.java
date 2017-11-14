package ru.naumen.sd40.log.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.multipart.MultipartFile;

public class TopTimeParser implements ITimeParser{
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH:mm");
	private String dataDate;
	private MultipartFile logs;
//    private Map<Long, DataSet> existing;
//    private Pattern timeRegex = Pattern.compile("^_+ (\\S+)");
	
    public TopTimeParser(MultipartFile logs, Map<Long, DataSet> existingDataSet, String timeZone) throws IllegalArgumentException{
        //Supports these masks in file name: YYYYmmdd, YYY-mm-dd i.e. 20161101, 2016-11-01
        Matcher matcher = Pattern.compile("\\d{8}|\\d{4}-\\d{2}-\\d{2}").matcher(logs.getOriginalFilename());
        if (!matcher.find()){
            throw new IllegalArgumentException();
        }
        this.dataDate = matcher.group(0).replaceAll("-", "");
        this.logs = logs;
        this.existing = existingDataSet;
        configureTimeZone(timeZone);
    }
    public void configureTimeZone(String timeZone){
    	sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
    }
	
	public long parseTime(String line) throws ParseException {
		long time;
		Matcher matcher = Pattern.compile("\\d{8}|\\d{4}-\\d{2}-\\d{2}").matcher(logs.getOriginalFilename());
        if (matcher.find()){
            time = prepareDate(sdf.parse(dataDate + matcher.group(1)).getTime());
            return time;
        }
        return 0L;
	}
	
    private long prepareDate(long parsedDate){
        int min5 = 5 * 60 * 1000;
        long count = parsedDate / min5;
        return count * min5;
    }
}