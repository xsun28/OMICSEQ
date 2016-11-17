package com.omicseq.store.imp;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.concurrent.WaitFutureTask;
import com.omicseq.utils.HttpClients;
import com.omicseq.utils.ThreadUtils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class Eutils {
    private static Logger logger = LoggerFactory.getLogger(Eutils.class);

    public static String getXml(String url, int max) {
        int cnt = 0;
        while (true) {
            ++cnt;
            try {
                return HttpClients.get(url);
            } catch (Exception e) {
                logger.error("get xml failed {}", url);
            }
            ThreadUtils.sleep(1000);
            if (cnt >= max) {
                return null;
            }
        }
    }

    public static class ESearchResult {
        public String id;

        @Override
        public String toString() {
            return "id:" + id;
        }
    }

    public static class ESummaryResult {
        public Map<String, String> summaries = new HashMap<String, String>(5);

        @Override
        public String toString() {
            return "summaries:" + summaries;
        }
    }

    public static abstract class BaseConverter<T> implements Converter {
        protected Class<T> clz;

        @SuppressWarnings("unchecked")
        public BaseConverter() {
            clz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean canConvert(Class type) {
            return clz.equals(type);
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            return null;
        }

        @Override
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {

        }

    }

    public static List<WaitFutureTask<Object>> buildTasks(List<String> names, final ESearchCallable call) {
        Semaphore semaphore = new Semaphore(5);
        if (CollectionUtils.isEmpty(names)) {
            return new ArrayList<WaitFutureTask<Object>>(0);
        }
        String baseUrl = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gene&term=";
        List<WaitFutureTask<Object>> taskList = new ArrayList<WaitFutureTask<Object>>(names.size());
        for (final String param : names) {
            final String url = baseUrl + HttpClients.encode(param);
            Callable<Object> callable = new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    if (!call.validation(param)) {
                        return null;
                    }
                    String xml = Eutils.getXml(url, 20);
                    if (StringUtils.isBlank(xml)) {
                        logger.info("not data return {}", param);
                        return null;
                    }
                    XStream xs = new XStream();
                    xs.registerConverter(new BaseConverter<ESearchResult>() {
                        @Override
                        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("conver geneSymbol {} ", param);
                            }
                            call.unmarshal(reader, param);
                            return null;
                        }
                    });
                    xs.alias("eSearchResult", ESearchResult.class);
                    xs.fromXML(xml);
                    return null;
                }
            };
            WaitFutureTask<Object> e = new WaitFutureTask<Object>(callable, semaphore);
            taskList.add(e);
        }
        return taskList;
    }

    public static abstract class ESearchCallable implements Callable<Object> {

        @Override
        public Object call() throws Exception {

            return null;
        }

        public abstract Object unmarshal(HierarchicalStreamReader reader, String param);

        public boolean validation(String param) {
            return true;
        }

    }

}
