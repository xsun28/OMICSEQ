package com.omicseq.store.imp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SortType;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class SampleEncodeDnaseImp extends SampleEncodeImp {
    public static void main1(String[] args) throws IOException {
        ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
        SmartDBObject query = new SmartDBObject();
        query.addSort("sampleId", SortType.ASC);
        List<Sample> coll = sampleDAO.find(query);
        for (int i = 0; i < 10; i++) {
            int fromIndex = i * 100;
            if (fromIndex >= coll.size()) {
                return;
            }
            int toIndex = fromIndex + 100;
            if (toIndex > coll.size()) {
                toIndex = coll.size();
            }
            List<Sample> list = coll.subList(fromIndex, toIndex);
            List<String> lines = new ArrayList<String>();
            for (Sample obj : list) {
                lines.add(obj.getUrl());
            }
            FileUtils.writeLines(new File(String.format("./%s.txt", i+1)), lines);
        }
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        try {
            if (null != args && args.length != 0) {
                for (String file : args) {
                    new SampleEncodeImp().impl(file);
                }
            } else {
                String file = "./src/test/resources/sample_encode_dnase_mate.csv";
                SampleEncodeDnaseImp impl = new SampleEncodeDnaseImp();
                impl.impl(file);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    @Override
    protected Integer getEtype() {
        return ExperimentType.DNASE_SEQ.value();
    }

}
