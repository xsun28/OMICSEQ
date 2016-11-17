package com.omicseq.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.bean.ReadRecord;
import com.omicseq.domain.BedEntry;
import com.omicseq.exception.OmicSeqException;


public class BedReadRecordIterator implements Iterator<ReadRecord> {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	BufferedReader bufferReader = null;
	private String nextLine = "";
	public BedReadRecordIterator(String fileName) {
		super();
		try {
			if (fileName.endsWith(".gz")) {
				this.bufferReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName))));
			} else {
				this.bufferReader = new BufferedReader(new FileReader(fileName));
			}
		} catch (FileNotFoundException e) {
			throw new OmicSeqException(" file " + fileName + " don't been found ", e);
		} catch (IOException e) {
			throw new OmicSeqException(" file " + fileName + " don't been found ", e);
		}
	}
	
	@Override
	public boolean hasNext() {
		return (nextLine != null);
	}

	@Override
	public ReadRecord next() {
		advance();
		String line = nextLine;
		BedEntry bedEntry = BedEntry.parse(line);
		if (bedEntry == null) {
			return null;
		} else {
			ReadRecord readRecord = new ReadRecord();
			readRecord.setStart(bedEntry.getStart());
			readRecord.setEnd(bedEntry.getStop());
			readRecord.setSeqName(bedEntry.getChr());
			return readRecord;
		}
	}

	@Override
	public void remove() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Cannot remove records from a BufferedReaderIterator");
	}
	
	private void advance() {
		try {
			nextLine = bufferReader.readLine();
		} catch (IOException e) {
			log.error("Error getting next line from BufferedReader");
			e.printStackTrace();
			nextLine = null;
			throw new RuntimeException("Error getting next line from BufferedReader");
		}
	}
	
}
