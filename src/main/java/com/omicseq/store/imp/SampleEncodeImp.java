package com.omicseq.store.imp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.omicseq.common.Constants;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

public class SampleEncodeImp extends BaseImp {
    private static ISampleDAO dao = DAOFactory.getDAO(ISampleDAO.class);
    private static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, Constants.STAT_SUFFIX);
    private static Map<Integer, Set<Integer>> inputIdMap = new HashMap<Integer, Set<Integer>>(5);
    private static Map<Integer, Integer> idMap = new HashMap<Integer, Integer>(5);

    public static void main(String[] args) throws Exception {
        List<String> errs = new ArrayList<String>(5);
        try {
            List<String> lines = FileUtils.readLines(new File("./src/test/resources/sample_encode_input.csv"));
            for (String line : lines) {
                String[] arr = line.split(",");
                String key = StringUtils.trimToEmpty(arr[0]);
                String val = StringUtils.trimToEmpty(arr[1]);
                if (!StringUtils.isNumeric(val)) {
                    errs.add(line);
                    continue;
                }
                Integer _key = Integer.valueOf(key);
                Set<Integer> set = inputIdMap.get(_key);
                if (set == null) {
                    set = new HashSet<Integer>(3);
                }
                set.add(Integer.valueOf(val));
                inputIdMap.put(_key, set);
            }
            if (null != args && args.length != 0) {
                for (String file : args) {
                    new SampleEncodeImp().impl(file);
                }
            } else {
                String file = "./src/test/resources/sample_encode_chip_hg19.csv";
                SampleEncodeImp impl = new SampleEncodeImp();
                impl.impl(file);
                impl.after(errs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtils.writeLines(new File("./logs/encode_err.txt "), errs);
            System.exit(0);
        }
    }

    protected Integer getEtype() {
        return ExperimentType.CHIP_SEQ_TF.getValue();
    }

    @Override
    void doProcess(String[] lines) {
        String url = lines[2];
        Sample exits = sampleDAO.getByUrl(url);
        if (null != exits) {
            logger.debug("Exits {} ", exits);
            return;
        }
        Sample obj = new Sample();
        Integer sampleId = toInteger(lines[0]);
        obj.setSampleId(dao.getSequenceId(SourceType.ENCODE));
        idMap.put(sampleId, obj.getSampleId());
        obj.setSegment(lines[1]);
        obj.setUrl(url);
        obj.setDescription(lines[3]);
        obj.setEtype(getEtype());
        obj.setSource(SourceType.ENCODE.value());
        Map<String, String> descMap = obj.descMap();
        obj.setCell(descMap.get("cell"));
        obj.setFactor(descMap.get("antibody"));
        obj.setLab(descMap.get("lab"));
        obj.setTimeStamp(descMap.get("datesubmitted"));
        obj.setSettype(descMap.get("settype"));
        sampleDAO.create(obj);
        logger.debug("Created:" + obj);
    }

    private void after(List<String> errs) {
        Set<Integer> ids = idMap.keySet();
        for (Integer id : ids) {
            Integer newId = idMap.get(id);
            Sample obj = sampleDAO.getBySampleId(newId);
            Set<Integer> set = inputIdMap.get(id);
            if (CollectionUtils.isNotEmpty(set)) {
                Set<Integer> coll = new HashSet<Integer>();
                for (Integer old : set) {
                    if (idMap.containsKey(old)) {
                        coll.add(idMap.get(old));
                    }
                }
                List<Integer> inputids = new ArrayList<Integer>(coll);
                Collections.sort(inputids);
                obj.setInputSampleIds(StringUtils.join(inputids, ","));
                sampleDAO.update(obj);
            } else {
                errs.add(String.format("newId:%s,sampleId:%s,inputSampleId:%s", newId, id, set));
            }
        }
    }
}
