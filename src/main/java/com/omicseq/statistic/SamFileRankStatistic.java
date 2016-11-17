package com.omicseq.statistic;

import java.io.File;
import java.util.Iterator;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileReader.ValidationStringency;

import com.omicseq.bean.ReadRecord;
import com.omicseq.common.ExperimentType;
import com.omicseq.io.SAMReadRecordIterator;

/**
 * 
 *以bam,sam格式的文件解析
 */
public class SamFileRankStatistic extends AbstractRankStatistic {

	private ExperimentType eType;
	
	public SamFileRankStatistic() {
		super();
	}

	@Override
	Iterator<ReadRecord> getFileIterator(String samplePath) {
		SAMFileReader.setDefaultValidationStringency(ValidationStringency.SILENT);
		SAMFileReader inputSam = new SAMFileReader(new File(samplePath));
		SAMReadRecordIterator samReadRecordIterator = new SAMReadRecordIterator(inputSam.iterator());
		return samReadRecordIterator;
	}
	
	
}
