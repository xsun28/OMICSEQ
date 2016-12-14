package com.omicseq.statistic;

import java.util.Iterator;

import com.omicseq.bean.ReadRecord;
import com.omicseq.io.BedReadRecordIterator;

public class BedFileRankStatistic extends AbstractRankStatistic {

	
	public BedFileRankStatistic() {
		super();
	}

	@Override
	Iterator<ReadRecord> getFileIterator(String samplePath) {
		return new BedReadRecordIterator(samplePath);
	}
	
}
