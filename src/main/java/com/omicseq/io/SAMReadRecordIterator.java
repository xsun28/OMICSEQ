package com.omicseq.io;

import java.util.Iterator;

import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

import com.omicseq.bean.ReadRecord;

public class SAMReadRecordIterator implements Iterator<ReadRecord> {

	private SAMRecordIterator samRecordIterator = null;
	
	
	public SAMReadRecordIterator(SAMRecordIterator samRecordIterator) {
		super();
		this.samRecordIterator = samRecordIterator;
	}


	@Override
	public boolean hasNext() {
		return samRecordIterator.hasNext();
	}


	@Override
	public ReadRecord next() {
		SAMRecord  samRecord  = samRecordIterator.next();
		if (samRecord != null) {
			ReadRecord readRecord = new ReadRecord();
			readRecord.setStart(samRecord.getAlignmentStart());
			readRecord.setEnd(samRecord.getAlignmentStart() + samRecord.getReadLength() - 1);
			readRecord.setSeqName(samRecord.getReferenceName());
			return readRecord;
		} else {
			return null;
		}
	}


	@Override
	public void remove() {
		// TODO Auto-generated method stub
		samRecordIterator.remove();
	}


	


	

	
	
}
