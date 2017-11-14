package ru.naumen.sd40.log.parser;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Top output parser
 * @author dkolmogortsev
 *
 */
public class TopParser implements IDataParser, ITimeParser{
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH:mm");
	
    private String dataDate;
    private MultipartFile logs;

    private Map<Long, DataSet> existing;

    private Pattern timeRegex = Pattern.compile("^_+ (\\S+)");

    private Pattern cpuAndMemPattren = Pattern
            .compile("^ *\\d+ \\S+ +\\S+ +\\S+ +\\S+ +\\S+ +\\S+ +\\S+ \\S+ +(\\S+) +(\\S+) +\\S+ java");
	
	private DataSet currentSet;
	
    public TopParser(MultipartFile logs, Map<Long, DataSet> existingDataSet) throws IllegalArgumentException{
        //Supports these masks in file name: YYYYmmdd, YYY-mm-dd i.e. 20161101, 2016-11-01
        Matcher matcher = Pattern.compile("\\d{8}|\\d{4}-\\d{2}-\\d{2}").matcher(logs.getOriginalFilename());
        if (!matcher.find()){
            throw new IllegalArgumentException();
        }
        this.dataDate = matcher.group(0).replaceAll("-", "");
        this.logs = logs;
        this.existing = existingDataSet;
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public void configureTimeZone(String timeZone){
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
    }


    public void parseLine(String line) throws ParseException{
        //check time
        if (currentSet != null)
        {
            //get la
            Matcher la = Pattern.compile(".*load average:(.*)").matcher(line);
            if (la.find())
            {
                currentSet.cpuData().addLa(Double.parseDouble(la.group(1).split(",")[0].trim()));
                return;
            }

            //get cpu and mem
            Matcher cpuAndMemMatcher = cpuAndMemPattren.matcher(line);
            if (cpuAndMemMatcher.find())
            {
                currentSet.cpuData().addCpu(Double.valueOf(cpuAndMemMatcher.group(1)));
                currentSet.cpuData().addMem(Double.valueOf(cpuAndMemMatcher.group(2)));
                return;
            }
        }
    }

    private long prepareDate(long parsedDate){
        int min5 = 5 * 60 * 1000;
        long count = parsedDate / min5;
        return count * min5;
    }

	public long parseTime(String line) throws ParseException {
		long time;
		Matcher matcher = Pattern.compile("\\d{8}|\\d{4}-\\d{2}-\\d{2}").matcher(logs.getOriginalFilename());
        if (matcher.find()){
            time = prepareDate(sdf.parse(dataDate + matcher.group(1)).getTime());
            currentSet = existing.computeIfAbsent(time, k -> new DataSet());
            return time;
        }
        return 0L;
	}
}
