package com.omicseq.store.imp;

import com.omicseq.domain.TxrRef;
import com.omicseq.store.dao.ITxrRefDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

public class TxrRefImp extends BaseImp {
    private static ITxrRefDAO dao = DAOFactory.getDAO(ITxrRefDAO.class);

    public static void main(String[] args) throws Exception {
        try {
            if (null != args && args.length != 0) {
                for (String file : args) {
                    new TxrRefImp().impl(file);
                }
            } else {
                String file = "./src/test/resources/txXref_hg19.csv";
                new TxrRefImp().impl(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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
        dao.create(obj);
        logger.debug("Created:" + obj);
    }
}
