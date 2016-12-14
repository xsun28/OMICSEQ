package com.omicseq.statistic;

import java.util.Iterator;

import com.omicseq.bean.ReadRecord;
import com.omicseq.io.WigReadRecordIterator;

public class WigFileRankStatistic extends AbstractRankStatistic  {

	public WigFileRankStatistic() {
		super();
	}

	
	@Override
	Iterator<ReadRecord> getFileIterator(String samplePath) {
		return new WigReadRecordIterator(samplePath);
	}
	
	
}
