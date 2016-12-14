package com.omicseq.store.dao;

import com.omicseq.domain.SummaryTrackData;

public interface ISummaryTrackDataDao extends IGenericDAO<SummaryTrackData> {

	void removeByCellAndEtype(String cancerType, int etype);

}
