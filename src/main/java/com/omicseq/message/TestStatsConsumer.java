package com.omicseq.message;

import com.omicseq.domain.Chunk;



public class TestStatsConsumer implements IMessageConsumer<Chunk> {

	@Override
	public void consume(Chunk t) {
		System.out.println(" start : " + t.getBegin() + "; end : " + t.getEnd());
	}

}
