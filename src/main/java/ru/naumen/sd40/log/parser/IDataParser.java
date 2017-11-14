package ru.naumen.sd40.log.parser;

import java.text.ParseException;

public interface IDataParser {
	public void parseLine(String line) throws ParseException;
}
