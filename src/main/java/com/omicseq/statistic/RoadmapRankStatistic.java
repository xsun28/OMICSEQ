package com.omicseq.statistic;

import java.util.Iterator;

import com.omicseq.bean.ReadRecord;
import com.omicseq.io.BedReadRecordIterator;

public class RoadmapRankStatistic extends AbstractRankStatistic  {

	// road map is bed 
	public RoadmapRankStatistic() {
		super();
	}
	
	@Override
	Iterator<ReadRecord> getFileIterator(String samplePath) {
		return new BedReadRecordIterator(samplePath);
	}
	
}
