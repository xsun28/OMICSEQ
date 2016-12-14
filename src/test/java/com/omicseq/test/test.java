package com.omicseq.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.mongodb.DBCollection;
import com.omicseq.core.SampleCache;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateUtils;

public class test {
	public static void main(String[] args) throws IOException {
		String[] rts = { /*"unc.edu_LGG.IlluminaHiSeq_RNASeqV2.1.13.0.sdrf",
				"unc.edu_LIHC.IlluminaHiSeq_RNASeqV2.1.13.0.sdrf",
				"unc.edu_LUAD.IlluminaHiSeq_RNASeqV2.1.13.0.sdrf",
				"unc.edu_LUSC.IlluminaHiSeq_RNASeqV2.1.10.0.sdrf",
				"unc.edu_OV.IlluminaHiSeq_RNASeqV2.1.1.0.sdrf",
				"unc.edu_PAAD.IlluminaHiSeq_RNASeqV2.1.7.0.sdrf",
				"unc.edu_PCPG.IlluminaHiSeq_RNASeqV2.1.2.0.sdrf",
				"unc.edu_PRAD.IlluminaHiSeq_RNASeqV2.1.13.0.sdrf",
				"unc.edu_READ.IlluminaGA_RNASeqV2.1.1.0.sdrf",
				"unc.edu_READ.IlluminaHiSeq_RNASeqV2.1.7.0.sdrf",
				"unc.edu_SARC.IlluminaHiSeq_RNASeqV2.1.5.0.sdrf",
				"unc.edu_SKCM.IlluminaHiSeq_RNASeqV2.1.12.0.sdrf",
				"unc.edu_THCA.IlluminaHiSeq_RNASeqV2.1.12.0.sdrf",
				"unc.edu_UCEC.IlluminaGA_RNASeqV2.1.0.0.sdrf",
				"unc.edu_UCEC.IlluminaHiSeq_RNASeqV2.1.9.0.sdrf",
				"unc.edu_UCS.IlluminaHiSeq_RNASeqV2.1.1.0.sdrf" */
				"unc.edu_DLBC.IlluminaHiSeq_RNASeqV2.1.1.0.sdrf"};
		for (String sf : rts) {
			String root = "F:/TCGA-RNAseq/" + sf + ".txt";
			ISampleDAO dao = DAOFactory.getDAOByTableType(ISampleDAO.class,
					"new");
			DBCollection collection = MongoDBManager.getInstance()
					.getCollection("manage", "manage", "samplenew");
			File file = new File(root);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String readString;
			boolean flag = true;
			String lastb = "";
			while ((readString = br.readLine()) != null) {
				if (flag) {
					flag = false;
					continue;
				}
				String[] cols = readString.split("	");
				String uuid = cols[0];
				String barcode = cols[1];
				if (!barcode.equals(lastb)) {
					SmartDBObject query1 = new SmartDBObject("$regex", uuid);
					SmartDBObject query = new SmartDBObject();
					query.put("url", query1);
					query.put("deleted", 0);
					query.put("source", 1);
					query.put("etype", 2);
					List<Sample> samples = dao.find(query);
					if (samples.size() > 1) {
						System.out.println(uuid);
					}
					if (samples.size() == 1) {
						Sample s = samples.get(0);
						String cell = s.getCell();
						String cancerType = cell.split("-")[1];
						SmartDBObject query2 = new SmartDBObject("sampleId",
								s.getSampleId());
						SmartDBObject q3 = new SmartDBObject();
						String cs = barcode.split("-")[3];
						if (cs.startsWith("0")) {
							cell = "TCGA-" + cancerType.toLowerCase()
									+ "-tumor";
						} else if (cs.startsWith("1")) {
							cell = "TCGA-" + cancerType.toLowerCase()
									+ "-normal";
						} else if (cs.startsWith("2")) {
							cell = "TCGA-" + cancerType.toLowerCase()
									+ "-control";
						}
						q3.put("cell", cell);
						q3.put("sampleCode", barcode);
						collection.update(query2, new SmartDBObject("$set", q3));
					}
					lastb = barcode;
				}
			}
		}
	}
}
