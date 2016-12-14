package com.omicseq.statistic.variation;

import java.io.File;
import java.util.Iterator;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileReader.ValidationStringency;

import com.omicseq.bean.ReadRecord;
import com.omicseq.io.SAMReadRecordIterator;


public class EncodeVariationRankStatistic extends AbstractVariationRankStatistic {
	// encode is bam file
	public EncodeVariationRankStatistic() {
		super();
	}

	@Override
	Iterator<ReadRecord> getFileIterator(String filePath) {
		SAMFileReader.setDefaultValidationStringency(ValidationStringency.SILENT);
		SAMFileReader inputSam = new SAMFileReader(new File(filePath));
		//inputSam.setValidationStringency(ValidationStringency.SILENT);
		SAMReadRecordIterator samReadRecordIterator = new SAMReadRecordIterator(inputSam.iterator());
		
//		inputSam.close();
		return samReadRecordIterator;
	}

}
