package com.omicseq.test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

public class Test3 {

	public static void main(String[] args) throws UnsupportedEncodingException {
//		String value = CancerType.valueOf("ACC").getValue();
//		System.out.println(value);
		String str = "abcd,\"efg,hij\",\"lm,n\",opq";
		Pattern p = Pattern.compile("\"(.*?)\"");
		Matcher m = p.matcher(str);
		ArrayList<String> strs = new ArrayList<String>();
//		ArrayList<String> strs2 = new ArrayList<String>();
//		strs.add("2");
//		strs.add("3");
//		strs2.add("2");
//		strs.retainAll(strs2);
//		System.out.println(strs.size());
		while(m.find()) {
			strs.add(m.group(1).replaceAll(",", " "));
			str = str.replace(m.group(1), m.group(1).replaceAll(",", " "));
		}
//		for (String s : strs){
//            System.out.println(s);
//        } 
		str = str.replaceAll("\"", "");
		System.out.println(str);
//		"96e79218965eb72c92a549dd5a330112"
		String s = DigestUtils.md5Hex(StringUtils.trimToEmpty("123456"));
		System.out.println(s);
		
		String t = "http://www.ncbi.nlm.nih.gov/geo/download/?acc=GSE53463&format=file&file=GSE53463%5FRead%5Fcounts%5Fper%5F100%5Fkb%2Etxt%2Egz";
		String t1 = URLDecoder.decode(t, "UTF-8");
		System.out.println(t1);
	}

}
