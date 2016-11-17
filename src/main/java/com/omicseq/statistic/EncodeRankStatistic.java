package com.omicseq.statistic;

import java.io.File;
import java.util.Iterator;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileReader.ValidationStringency;

import com.omicseq.bean.ReadRecord;
import com.omicseq.io.SAMReadRecordIterator;

public class EncodeRankStatistic extends AbstractRankStatistic {

	// encode is sam file
	public EncodeRankStatistic() {
		super();
	}
	
	@Override
	Iterator<ReadRecord> getFileIterator(String samplePath) {
		SAMFileReader.setDefaultValidationStringency(ValidationStringency.SILENT);
		SAMFileReader inputSam = new SAMFileReader(new File(samplePath));
		//inputSam.setValidationStringency(ValidationStringency.SILENT);
		SAMReadRecordIterator samReadRecordIterator = new SAMReadRecordIterator(inputSam.iterator());
		return samReadRecordIterator;
	}
	
}
