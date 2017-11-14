package ru.naumen.sd40.log.parser;


/**
 * Created by doki on 22.10.16.
 */
public class SDNGParser implements IDataParser{
	
	private DataSet currentSet;
	
    public void parseLine(String line){
		ErrorParser errors = currentSet.getErrors();
		ActionDoneParser actionsDone = currentSet.getActionsDone();
        errors.parseLine(line);
        actionsDone.parseLine(line);
    }
}
