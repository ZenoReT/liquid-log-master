package ru.naumen.sd40.log.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.io.InputStreamReader;

import org.influxdb.dto.BatchPoints;

import org.springframework.web.multipart.MultipartFile;
import ru.naumen.perfhouse.influx.InfluxDAO;

/**
 * Created by doki on 22.10.16.
 */
public class App
{
    /**
     * 
     *
     * @param logs - logs file
     * @param nameInfluxDB - name of influxDB
     * @param timeZone
     * @param parseMode - top/sdng/gc
     * @param traceCheck - if true, then output in console trace of log
     * @param influxDAO - the variable for works with data base
     * @throws IOException
     * @throws ParseException
     */
    public static void parse(MultipartFile logs, String nameInfluxDB, String timeZone, String parseMode,
    						boolean traceCheck, InfluxDAO influxDAO) throws IOException, ParseException
    {
        String influxDb = nameInfluxDB.replaceAll("-", "_");
        BatchPoints points = influxDAO.startBatchPoints(influxDb);
        HashMap<Long, DataSet> data = new HashMap<>();

        switch (parseMode){
        case "sdng":
            //Parse sdng
        	TimeParser timeParser = new TimeParser();
        	parse(data, timeZone, logs, (ITimeParser)timeParser);
            break;
        case "gc":
            //Parse gc log
        	GCParser gcParser = new GCParser();
        	parse(data, timeZone, logs, (ITimeParser)gcParser);
            break;
        case "top":
        	//Parse top
            TopParser topParser = new TopParser(logs, data);
            topParser.configureTimeZone(timeZone);
            topParser.parse();
            break;
        default:
            throw new IllegalArgumentException(
                    "Unknown parse mode! Availiable modes: sdng, gc, top. Requested mode: " + parseMode);
        }

        if (traceCheck){
            System.out.print("Timestamp;Actions;Min;Mean;Stddev;50%%;95%%;99%%;99.9%%;Max;Errors\n");
        }
        data.forEach((k, set) ->{
            ActionDoneParser dones = set.getActionsDone();
            dones.calculate();
            ErrorParser erros = set.getErrors();
            if (traceCheck){
                System.out.print(String.format("%d;%d;%f;%f;%f;%f;%f;%f;%f;%f;%d\n", k, dones.getCount(),
                        dones.getMin(), dones.getMean(), dones.getStddev(), dones.getPercent50(), dones.getPercent95(),
                        dones.getPercent99(), dones.getPercent999(), dones.getMax(), erros.getErrorCount()));
            }
            if (!dones.isNan()){
                influxDAO.storeActionsFromLog(points, influxDb, k, dones, erros);
            }

            GCParser gc = set.getGc();
            if (!gc.isNan()){
                influxDAO.storeGc(points, influxDb, k, gc);
            }

            TopData cpuData = set.cpuData();
            if (!cpuData.isNan()){
                influxDAO.storeTop(points, influxDb, k, cpuData);
            }
        });
        influxDAO.writeBatch(points);
    }
    
	private static void parse(HashMap<Long, DataSet> data, String timeZone,
					   MultipartFile logs, ITimeParser parser) 
			   throws ParseException, IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(logs.getInputStream()))){
		String line;
		while ((line = br.readLine()) != null){
			long time = parser.parseTime(line);

			if (time == 0){
			continue;
			}
			
			int min5 = 5 * 60 * 1000;
			long count = time / min5;
			long key = count * min5;
			
			data.computeIfAbsent(key, k -> new DataSet()).parseLine(line);
			}
		}
	}
}
