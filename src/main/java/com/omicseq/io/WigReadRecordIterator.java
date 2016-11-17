package com.omicseq.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.bean.ReadRecord;
import com.omicseq.exception.OmicSeqException;


/**
 * @author Min.Wang
 *
 */
public class WigReadRecordIterator implements Iterator<ReadRecord>  {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	BufferedReader bufferReader = null;
	private String nextLine;
	private String lastHeader;
	
	public WigReadRecordIterator(String fileName) {
		super();
		try {
			this.bufferReader = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			throw new OmicSeqException(" file " + fileName + " don't been found ", e);
		}
	}
	
	@Override
	public boolean hasNext() {
		return (nextLine != null);
	}
	
	@Override
	public ReadRecord next() {
		try {
			String line = "";
			Integer count = 0;
			String currentHeader = lastHeader;
			Integer readCount = 0;
			while ((line = bufferReader.readLine()) != null) {
				readCount = readCount + 1;
				line = line.toLowerCase();
				if (line.startsWith("track")) {
			        continue;
			      } else if (line.startsWith("fixedstep") || line.startsWith("variablestep")) {
			    	  if (lastHeader != null) {
			    		  lastHeader = line;
			    		  break;
			    	  } else {
			    		  currentHeader = line;
			    		  lastHeader = line;
			    	  }
			      } else {
			    	  count = count + 1;
			      }
			}
			
			if (readCount == 0) {
				return null;
			}
			
			currentHeader = currentHeader.toLowerCase();
			if (currentHeader.startsWith("fixedstep")) {
				ReadRecord readRecord  = parseFixedStepHeader(currentHeader);
				readRecord.setEnd(readRecord.getStart() + count -1);
				readRecord.setValue(currentHeader);
				return readRecord;
			} else if (currentHeader.startsWith("variablestep")) {
				ReadRecord readRecord  = this.parseVariableStepHeader(currentHeader);
				return readRecord;
			} else {
				ReadRecord readRecord = new ReadRecord();
				readRecord.setStart(0);
				readRecord.setEnd(0);
				readRecord.setSeqName("x");
				return readRecord;
			}
			
		} catch (IOException e) {
			log.error(" read file failed ", e);
			return null;
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Cannot remove records from a BufferedReaderIterator");
	}
	
	
	private ReadRecord parseVariableStepHeader(String headerLine)  {
		String[] tokens = headerLine.split(" ");
		String chr = "";
		int start = 1;
		int span = 1;
		for (int i = 1; i < tokens.length; i++ ) {
			String s = tokens[i];
			String[] pair = s.split("=");
			
			
			String key = pair[0];
			String value = pair[1];
			if (key.equalsIgnoreCase("chrom")) {
				chr = value;
			} else if (key.equalsIgnoreCase("span")) {
				span = Integer.parseInt(value);
			}
			
		}
		
		ReadRecord readRecord = new ReadRecord();
		if (!chr.contains("chr")) {
			readRecord.setSeqName("chr" + chr);
		}
		readRecord.setType("variablestep");
		readRecord.setStart(1);
		return readRecord;
	}
	

	public  ReadRecord parseFixedStepHeader(String headerLine)  {
		String[] tokens = headerLine.split(" ");
		
		String chr = "";
		int start = 1;
		int span = 1;
		int step = 1;
		for (int i = 1; i < tokens.length; i++ ) {
			String s = tokens[i];
			String[] pair = s.split("=");
			
			
			String key = pair[0];
			String value = pair[1];
			if ("chrom".equalsIgnoreCase(key)) {
				chr = value;
			} else if ("start".equalsIgnoreCase(key)) {
				start = Integer.parseInt(value);
			} else if ("span".equalsIgnoreCase(key)) {
				span = Integer.parseInt(value);
			} else if ("step".equalsIgnoreCase(key)) {
				step = Integer.parseInt(value);
			}
		}
		
		ReadRecord readRecord = new ReadRecord();
		readRecord.setStart(start);
		readRecord.setType("fixedstep");
		if (!chr.contains("chr")) {
			readRecord.setSeqName("chr" + chr);
		}
		readRecord.setEnd(-1);
		return readRecord;
	}

	
	public static void main(String[] args) {
		WigReadRecordIterator recordIterator = new WigReadRecordIterator("E:\\机器学习课程\\神经网络\\broad.mit.edu_STAD.IlluminaGA_DNASeq.Level_2.0.0.0\\TCGA-B7-5816_f844b521-1ff0-4e58-9d70-9f7dc0ed0ee7_a2c1f792-39dd-4c35-8868-4be46623e3f0.coverage.wig.txt\\STAD-TCGA-B7-5816-TP-NB-SM-1V6U3-SM-1V6VA.coverage.wig.txt");
		ReadRecord readRecord = null;
		ReadRecord lastReadRecord = null;
		Integer count = 0;
		while((readRecord = recordIterator.next()) != null) {
			lastReadRecord = readRecord;
			count = count + 1;
		}
		
		System.out.println(" count is : " + count);
		System.out.println(" last record : " + lastReadRecord.toString());
	}
}
