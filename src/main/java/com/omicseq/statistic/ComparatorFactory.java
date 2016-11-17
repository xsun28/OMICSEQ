package com.omicseq.statistic;

import java.util.Comparator;

import com.omicseq.common.SortType;
import com.omicseq.domain.CacheGeneRank;
import com.omicseq.domain.GeneRank;

public class ComparatorFactory {

	public static Comparator<GeneRank> getTssTesComparator() {
		return new Comparator<GeneRank>() {
			@Override
			public int compare(GeneRank o1, GeneRank o2) {
				Double tssTesCount1 = o1.getTssTesCount();
				Double tssTesCount2 = o2.getTssTesCount();
				return tssTesCount2.compareTo(tssTesCount1);
			}
		};
	}

	public static Comparator<GeneRank> getTss5kComparator() {
		return new Comparator<GeneRank>() {
			@Override
			public int compare(GeneRank o1, GeneRank o2) {
				Double tss5kCount1 = o1.getTss5kCount();
				Double tss5kCount2 = o2.getTss5kCount();
				return tss5kCount2.compareTo(tss5kCount1);
			}
		};
	}

	public static Comparator<GeneRank> getTssTes5kComparator() {
		return new Comparator<GeneRank>() {
			@Override
			public int compare(GeneRank o1, GeneRank o2) {
				Double tssT5Count1 = o1.getTssT5Count();
				Double tssT5Count2 = o2.getTssT5Count();
				return tssT5Count2.compareTo(tssT5Count1);
			}
		};
	}

	public static Comparator<GeneRank> getGeneIdComparator() {
		return new Comparator<GeneRank>() {
			@Override
			public int compare(GeneRank o1, GeneRank o2) {
				if (null != o1.getGeneId() && null != o2.getGeneId()) {
					return o1.getGeneId().compareTo(o2.getGeneId());
				} else {
					return 0;
				}
			}
		};
	}
	
	public static Comparator<CacheGeneRank> getMixtureComparator(SortType sortType) {
		if (sortType == null || SortType.ASC.name().equalsIgnoreCase(sortType.name())) {
			return new Comparator<CacheGeneRank>() {
				@Override
				public int compare(CacheGeneRank o1, CacheGeneRank o2) {
					if (null != o1.getMixturePerc() && null != o2.getMixturePerc()) {
						return o1.getMixturePerc().compareTo(o2.getMixturePerc());
					} else {
						return 0;
					}
				}
			};
		} else {
			return new Comparator<CacheGeneRank>() {
				@Override
				public int compare(CacheGeneRank o1, CacheGeneRank o2) {
					if (null != o2.getMixturePerc() && null != o1.getMixturePerc()) {
						return o2.getMixturePerc().compareTo(o1.getMixturePerc());
					} else {
						return 0;
					}
				}
			};
		}
		
	}
	
	public static Comparator<CacheGeneRank> getCacheSampleIdComparator() {
		return new Comparator<CacheGeneRank>() {
			@Override
			public int compare(CacheGeneRank o1, CacheGeneRank o2) {
				if (null != o1.getSampleId() && null != o2.getSampleId()) {
					return o1.getSampleId().compareTo(o2.getSampleId());
				} else {
					return 0;
				}
			}
		};
	}
	
	public static Comparator<CacheGeneRank> getTssTesCountComparator() {
		return new Comparator<CacheGeneRank>() {
			@Override
			public int compare(CacheGeneRank o1, CacheGeneRank o2) {
				if(o1.getEtype() == 1 && o2.getEtype() == 1)
				{
					if (null != o1.getTss5kCount() && null != o2.getTss5kCount()) {
						return o1.getTss5kCount().compareTo(o2.getTss5kCount()) *(-1);
					} else {
						return 0;
					}
				} else {
					if (null != o1.getTssTesCount() && null != o2.getTssTesCount()) {
						return o1.getTssTesCount().compareTo(o2.getTssTesCount()) *(-1);
					} else {
						return 0;
					}
				}
				
			}
		};
	}
	
	public static Comparator<CacheGeneRank> getExperimentTypeComparator() {
		return new Comparator<CacheGeneRank>() {
			@Override
			public int compare(CacheGeneRank o1, CacheGeneRank o2) {
				if (null != o1.getEtype() && null != o2.getEtype()) {
					return o1.getEtype().compareTo(o2.getEtype());
				} else {
					return 0;
				}
			}
		};
	}
	
	public static Comparator<GeneRank> getTssTesCountComparator2() {
		return new Comparator<GeneRank>() {
			@Override
			public int compare(GeneRank o1, GeneRank o2) {
				if(o1.getEtype() == 1 && o2.getEtype() == 1)
				{
					if (null != o1.getTss5kCount() && null != o2.getTss5kCount()) {
						return o1.getTss5kCount().compareTo(o2.getTss5kCount()) *(-1);
					} else {
						return 0;
					}
				} else {
					if (null != o1.getTssTesCount() && null != o2.getTssTesCount()) {
						return o1.getTssTesCount().compareTo(o2.getTssTesCount()) *(-1);
					} else {
						return 0;
					}
				}
				
			}
		};
	}
	
	public static Comparator<GeneRank> getMixtureComparator2(SortType sortType) {
		if (sortType == null || SortType.ASC.name().equalsIgnoreCase(sortType.name())) {
			return new Comparator<GeneRank>() {
				@Override
				public int compare(GeneRank o1, GeneRank o2) {
					if (null != o1.getMixturePerc() && null != o2.getMixturePerc()) {
						return o1.getMixturePerc().compareTo(o2.getMixturePerc());
					} else {
						return 0;
					}
				}
			};
		} else {
			return new Comparator<GeneRank>() {
				@Override
				public int compare(GeneRank o1, GeneRank o2) {
					if (null != o2.getMixturePerc() && null != o1.getMixturePerc()) {
						return o2.getMixturePerc().compareTo(o1.getMixturePerc());
					} else {
						return 0;
					}
				}
			};
		}
		
	}
	
}
