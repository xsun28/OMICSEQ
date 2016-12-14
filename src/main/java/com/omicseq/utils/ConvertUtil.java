package com.omicseq.utils;

import org.apache.commons.lang3.StringUtils;


/**
 * @author Min.Wang
 *
 */
public class ConvertUtil {

	/**
	 * parse text to long value
	 * @param value
	 * @param defaultValue
	 * @return
	 */
	public static Long toLong(String value, Long defaultValue) {
		if (StringUtils.isBlank(value)) {
			return defaultValue;
		}

		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * parse text to integer value
	 * @param value
	 * @param defaultValue
	 * @return
	 */
	public static Integer toInteger(String value, Integer defaultValue) {
		if (StringUtils.isBlank(value)) {
			return defaultValue;
		}

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * parse text to double value
	 * @param value
	 * @param defaultValue
	 * @return
	 */
	public static Double toDouble(String value, double defaultValue) {
		if (StringUtils.isBlank(value)) {
			return defaultValue;
		}

		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

}
