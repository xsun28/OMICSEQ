package com.omicseq.utils;

import java.math.BigDecimal;

/**
 * @author Min.Wang
 *
 */
public class MathUtils {

	/**
	 * floor the double value
	 * @param value
	 * @return
	 */
	public static Double floor(Double value) {
		BigDecimal bigDecimal = new BigDecimal(value).setScale(5, BigDecimal.ROUND_FLOOR);
		return bigDecimal.doubleValue();
	}
	
}
