package com.omicseq.store.imp;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.utils.DateTimeUtils;

public class NewTxrRefImp extends BaseImp {
    private static ITxrRefDAO dao = DAOFactory.getDAOByTableType(ITxrRefDAO.class, "new");

    private List<TxrRef> data = new ArrayList<TxrRef>(5);

    public static void main(String[] args) throws Exception {
        DateTime dt = DateTime.now();
        try {
            if (null != args && args.length != 0) {
                for (String file : args) {
                    NewTxrRefImp txrref = new NewTxrRefImp();
                    txrref.impl(file);
                }
            } else {
                String file = "./src/test/resources/txXref_hg19_new.csv";
                NewTxrRefImp txrref = new NewTxrRefImp();
                txrref.impl(file);
                txrref.after();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("used time:" + DateTimeUtils.diff(dt, DateTime.now()));
            System.exit(0);
        }
    }

    @Override
    void doProcess(String[] lines) {
        // tx_id,mRNA,spID,spDisplayID,geneSymbol,refseq,protAcc,description
        if ("tx_id".equalsIgnoreCase(lines[0])) {
            return;
        }
        TxrRef obj = new TxrRef();
        obj.setUcscName(lines[0]);
        obj.setmRNA(lines[1]);
        obj.setSpID(lines[2]);
        obj.setSpDisplayID(lines[3]);
        obj.setGeneSymbol(lines[4]);
        obj.setRefseq(lines[5]);
        obj.setProtAcc(lines[6]);
        obj.setDescription(lines[7]);
        data.add(obj);
    }

    private void after() {
        DateTime dt = DateTime.now();
        logger.debug("准备写入数据 data {} records", data.size());
        dao.create(data);
        logger.debug("写入数据用时{}", DateTimeUtils.diff(dt, DateTime.now()));
    }
}
