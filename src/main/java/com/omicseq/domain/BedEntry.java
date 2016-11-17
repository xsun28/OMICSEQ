package com.omicseq.domain;

import java.util.Arrays;


import com.omicseq.exception.OmicSeqException;

public class BedEntry implements java.io.Serializable {

	private static final long serialVersionUID = 8657984166944604756L;

	private String chr;
	private int start;
	private int stop;
	private String id;
	protected Number value;
	private String itemRgb;
	private int thickStart;
	private int thickEnd;
	private int blockCount;
	private int[] blockSizes;
	private int[] blockStarts;

	/**
	 * @param chr
	 * @param start
	 * @param stop
	 */
	public BedEntry(String chr, int start, int stop) {
		this.chr = chr;
		this.start = start;
		this.stop = stop;
	}

	/**
	 * Parse a BedEntry from a line in a Bed file
	 * or return null, if the line passed is a comment (#)
	 * @param line a record in a Bed file
	 * @return a BedEntry object parsed from line
	 */
	public static BedEntry parse(String line) {
		if (line == null) {
			return null;
		}
		if (line.startsWith("#") || line.startsWith("track")) {
			return null;
		}

		String[] entry = line.split("\t");
		if (entry.length < 3) {
			throw new OmicSeqException("Invalid Bed entry has < 3 columns");
		}

		String chr = entry[0];
		int start = Integer.parseInt(entry[1]) + 1; // Bed is 0-indexed
		int stop = Integer.parseInt(entry[2]); // and half-open
		if (start > stop) {
			throw new OmicSeqException("Invalid Bed entry has start > stop. Use strand column 6 (+/-) for Crick intervals");
		}
		BedEntry bed = new BedEntry(chr, start, stop);

		if (entry.length >= 4) {
			bed.setId(entry[3]);
		}
		
		if (entry.length >= 5 && !entry[4].equalsIgnoreCase(".") && !entry[4].equalsIgnoreCase("-")) {
			try {
				bed.setValue(Integer.valueOf(entry[4]));
			} catch (NumberFormatException e) {
				
			}
		}

		// Reverse start/stop if on the - strand
		if (entry.length >= 6 && entry[5].equalsIgnoreCase("-")) {
			bed.setStart(stop);
			bed.setStop(start);
		}

		if (entry.length >= 8) {
			bed.setThickStart(Integer.parseInt(entry[6]) + 1);
			bed.setThickEnd(Integer.parseInt(entry[7]));
		}

		if (entry.length >= 9) {
			bed.setItemRgb(entry[8]);
		}

		if (entry.length >= 12) {
			bed.setBlockCount(Integer.parseInt(entry[9]));
			bed.setBlockSizes(mapToInt(entry[10].split(",")));
			bed.setBlockStarts(mapToInt(entry[11].split(",")));
		}

		return bed;
	}

	public String toOutput() {
		return toBed();
	}

	public String toBed() {
		String idStr = (getId() == null) ? "." : getId();
		String valueStr = (getValue() == null) ? "." : String.valueOf(getValue().intValue());
		return getChr() + "\t" + (low() - 1) + "\t" + high() + "\t" + idStr + "\t" + valueStr
				+ "\t" + strand();
	}

	/**
	 * The strand of this Interval, either "+" or "-"
	 * @return "+" if this Interval is Watson, "-" if this Inteval is Crick
	 */
	public final String strand() {
		return isWatson() ? "+" : "-";
	}

	public final boolean isWatson() {
		return stop >= start;
	}

	/**
	 * The lowest genomic coordinate, i.e min { start, stop }
	 * @return start or stop, whichever is lower
	 */
	public final int low() {
		return Math.min(start, stop);
	}

	/**
	 * The highest genomic coordinate, i.e max { start, stop }
	 * @return start or stop, whichever is higher
	 */
	public final int high() {
		return Math.max(start, stop);
	}

	/**
	 * @return the itemRgb
	 */
	public String getItemRgb() {
		return itemRgb;
	}

	/**
	 * @param itemRgb the itemRgb to set
	 */
	public void setItemRgb(String itemRgb) {
		this.itemRgb = itemRgb;
	}

	/**
	 * @return the thickStart
	 */
	public int getThickStart() {
		return thickStart;
	}

	/**
	 * @param thickStart the thickStart to set
	 */
	public void setThickStart(int thickStart) {
		this.thickStart = thickStart;
	}

	/**
	 * @return the thickEnd
	 */
	public int getThickEnd() {
		return thickEnd;
	}

	/**
	 * @param thickEnd the thickEnd to set
	 */
	public void setThickEnd(int thickEnd) {
		this.thickEnd = thickEnd;
	}

	/**
	 * @return the blockCount
	 */
	public int getBlockCount() {
		return blockCount;
	}

	/**
	 * @param blockCount the blockCount to set
	 */
	public void setBlockCount(int blockCount) {
		this.blockCount = blockCount;
	}

	/**
	 * @return the blockSizes
	 */
	public int[] getBlockSizes() {
		return blockSizes;
	}

	/**
	 * @param blockSizes the blockSizes to set
	 */
	public void setBlockSizes(int[] blockSizes) {
		this.blockSizes = blockSizes;
	}

	/**
	 * @return the blockStarts
	 */
	public int[] getBlockStarts() {
		return blockStarts;
	}

	/**
	 * @param blockStarts the blockStarts to set
	 */
	public void setBlockStarts(int[] blockStarts) {
		this.blockStarts = blockStarts;
	}

	public String getChr() {
		return chr;
	}

	public void setChr(String chr) {
		this.chr = chr;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getStop() {
		return stop;
	}

	public void setStop(int stop) {
		this.stop = stop;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Number getValue() {
		return value;
	}

	public void setValue(Number value) {
		this.value = value;
	}

	public static int[] mapToInt(String[] list) {
		int[] ret = new int[list.length];
		for (int i = 0; i < list.length; i++) {
			ret[i] = Integer.parseInt(list[i]);
		}
		return ret;
	}

	public float[] mapToFloat(String[] list) {
		float[] ret = new float[list.length];
		for (int i = 0; i < list.length; i++) {
			ret[i] = Float.parseFloat(list[i]);
		}
		return ret;
	}

	@Override
	public String toString() {
		return "BedEntry [chr=" + chr + ", start=" + start + ", stop=" + stop + ", id=" + id
				+ ", value=" + value + ", itemRgb=" + itemRgb + ", thickStart=" + thickStart
				+ ", thickEnd=" + thickEnd + ", blockCount=" + blockCount + ", blockSizes="
				+ Arrays.toString(blockSizes) + ", blockStarts=" + Arrays.toString(blockStarts)
				+ "]";
	}

}
