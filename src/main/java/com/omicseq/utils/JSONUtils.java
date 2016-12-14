package com.omicseq.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omicseq.bean.Entry;
import com.omicseq.bean.HistoryResultValue;

public class JSONUtils {
    private static ObjectMapper mapper = new ObjectMapper();
    private static CloseableHttpClient client = HttpClients.createDefault();
    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(Include.NON_EMPTY);
    }

    /**
     * @param url
     * @param type
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static <T> T from(String content, TypeReference<T> type) throws JsonParseException, JsonMappingException,
            IOException {
        return mapper.readValue(content, type);
    }

    /**
     * @param url
     * @param parameters
     * @param type
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static <T> T from(String url, List<NameValuePair> parameters, TypeReference<T> type)
            throws JsonParseException, JsonMappingException, IOException {
        if (CollectionUtils.isNotEmpty(parameters)) {
            HttpPost post = new HttpPost(url);
            post.setEntity(new UrlEncodedFormEntity(parameters));
            InputStream is = client.execute(post).getEntity().getContent();
            return mapper.readValue(is, type);
        } else {
            return mapper.readValue(new URL(url), type);
        }
    }

    /**
     * object to json
     * 
     * @param obj
     * @return
     * @throws JsonProcessingException
     */
    public static String to(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }

    public static void main(String[] args) throws IOException {
        
        String str = JSONUtils.to(new Entry<String, String>("key","val"));
        System.out.println(str);

        TypeReference<Entry<String, String>> type = new TypeReference<Entry<String, String>>() {
        };
        Entry<String, String> obj = JSONUtils.from(str, type);
        System.out.println(obj);
        
        
        List<HistoryResultValue> rs = new ArrayList<HistoryResultValue>(100);
        Integer sampleId = 869;
        // Sample sample = sampleCache.get(sampleId);
        HistoryResultValue value = new HistoryResultValue();
        value.setSampleId(sampleId);
        value.setRank(500);
        value.setTotal(2000);
        // 保留三位小数
        double percentile = (value.getRank().doubleValue() / value.getTotal().doubleValue()) * 100;
       // value.setPercentile(percentile);
        DecimalFormat df = new DecimalFormat("0.000");
        value.setPercentileFormat(df.format(percentile));
        rs.add(value);
        String json = JSONUtils.to(rs);
        System.out.println(json);
    }
}
