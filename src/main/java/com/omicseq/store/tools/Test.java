package com.omicseq.store.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class Test {

	public static void main(String[] args) {
//		String url = "https://genome.ucsc.edu/cgi-bin/hgEncodeVocab?ra=encode/cv.ra&type=Antibody&bgcolor=FFFEE8";
//		EncodeFactor ef = new EncodeFactor();
//		ef.readFactorDescription(url);
//		HashMap<String, String> map = new HashMap<String, String>();
//		map.put("header", "Content-Type: text/plain");
//		map.put("data", "6ddf4ce5-5956-4394-af6e-f840573ac67e, 9482fe83-a4b1-424a-baf1-8b0f8e40c9b7");
//		String s = doPost("https://tcga-data.nci.nih.gov/uuid/uuidws/mapping/json/uuid/batch", map);
//		System.out.println(s);
		
		List<Integer> geneIdList = new ArrayList<Integer>();
		geneIdList.add(1);
		geneIdList.add(2);
		Integer[] geneIds = new Integer[geneIdList.size()];
		int i =0;
		for(Integer geneId : geneIdList)
		{
			geneIds[i] = geneId;
			i++;
		}
		System.out.println(geneIds.length);
	}
	
	public static String doPost(String url, Map<String, String> params) { 
        String response = null; 
        HttpClient client = new HttpClient();
        HttpMethod method = new PostMethod(url); 
        //设置Http Post数据 
        if (params != null) { 
                HttpMethodParams p = new HttpMethodParams(); 
                for (Map.Entry<String, String> entry : params.entrySet()) { 
                        p.setParameter(entry.getKey(), entry.getValue()); 
                } 
                method.setParams(p); 
        } 
        try { 
                client.executeMethod(method); 
                response = method.getResponseBodyAsString(); 
        } catch (IOException e) { 
//                log.error("执行HTTP Post请求" + url + "时，发生异常！", e); 
        } finally { 
                method.releaseConnection(); 
        } 

        return response; 
	} 

}
