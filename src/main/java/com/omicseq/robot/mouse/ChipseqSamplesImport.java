package com.omicseq.robot.mouse;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.CellTypeDesc;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.utils.DateUtils;

public class ChipseqSamplesImport {
	
//	private static String rootPath = "F:"+File.separator +"mutation";
	private static String filePath = "E:\\mouse\\ENCODEmm9\\mouse__chipseqsamples.xlsx";
	private static ISampleDAO sampleNewDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, "new");
	private static ISampleDAO sampleDAO = DAOFactory.getDAO(ISampleDAO.class);
	
	
	public static void main(String[] args) throws Exception {
		ChipseqSamplesImport rnaSampleImport = new ChipseqSamplesImport();
		rnaSampleImport.importSamples(filePath);
	}


	private void importSamples(String filePath) throws Exception {
		File file = new File(filePath);
		Map<String, String> tissues = this.getDetailByCellFromWeb("http://www.genome.ucsc.edu/cgi-bin/hgEncodeVocab?ra=encode/cv.ra&type=Cell+Line&organism=Mouse&bgcolor=FFFEE8");
		
		InputStream is = new FileInputStream(file);
		XSSFWorkbook wb = new XSSFWorkbook(is);
 		int sheetNum = wb.getNumberOfSheets();
		for(int s=0; s<sheetNum; s ++){
			List<Sample> sampleList = new ArrayList<Sample>();
			XSSFSheet sheet = wb.getSheetAt(s);
			String sheetName = sheet.getSheetName();
			int rowNum = sheet.getLastRowNum();
			for(int i=0; i<=rowNum; i++)
			{
				XSSFRow row = sheet.getRow(i);
				
//				int cellNum = row.getLastCellNum();
//				for(int j=0; j<cellNum; j++)
//				{
//					XSSFCell cell = row.getCell(j);
//					System.out.println(cell.toString());
//				}
				
				String text = row.getCell(0).toString();
				String[] elements = text.split("; ");
				String url ="http://hgdownload-test.cse.ucsc.edu/goldenPath/mm9/encodeDCC/" + sheetName+ "/" + elements[0].split(" ")[0];
				String segment = "";
				if(elements[0].contains("     ")) {
					segment = elements[0].split("     ")[1].replace(";", "");
				}
				else if(elements[0].contains("    ")){
					segment = elements[0].split("    ")[1].replace(";", "");
				}
				else if(elements[0].contains("  ")){
					segment = elements[0].split("  ")[1].replace(";", "");
				}else {
					segment = elements[0].split(" ")[1].replace(";", "");
				}
				
				String grant = elements[1].replace(";", "");
				String lab = elements[2].replace(";", "");
				String composite = elements[3].replace(";", "");
				String dataType = elements[4].replace(";", "");
				String view = elements[5].replace(";", "");
				String cell = elements[6].replace(";", "");
				String strain = elements[7].replace(";", "");
				String sex = elements[8].replace(";", "");
				String age = elements[9].replace(";", "");
				String rnaExtract = elements[11].replace(";", "");
				String readType = elements[12].replace(";", "");
				String treatment = elements[13].replace(";", "");
				String source = elements[15].replace(";", "");
				String dccAccession = elements[16].replace(";", "");
				String dataSubmit = elements[17].replace(";", "");
//				String subId = elements[19].replace(";", "");
//				String geoSampleAccession = "";
//				String seqPlatform = "";
//				String softwareVersion = "";
//				String tissueSourceType = "";
				String barcode = elements[0].split(" ")[0];
//				String size = "";
//				if(elements[20].contains("geoSampleAccession"))
//				{
//					geoSampleAccession = elements[20].replace(";", "");
//					seqPlatform = elements[22].replace(";", "");
//					barcode = elements[23].replace(";", "");
//					if(barcode.contains("tableName")){
//						size = elements[26].replace(";", "");
//					} else {
//						size = elements[25].replace(";", "");
//					}
//					
//				} else {
//					seqPlatform = elements[21].replace(";", "");
//					barcode = elements[22].replace(";", "");
//					if(barcode.contains("tableName")){
//						size = elements[25].replace(";", "");
//					} else {
//						size = elements[24].replace(";", "");
//					}
//				}
				
//				String url ="http://hgdownload-test.cse.ucsc.edu/goldenPath/mm9/encodeDCC/wgEncodeCaltechRnaSeq/" + row.getCell(0).toString();
//				String segment = row.getCell(1).toString().replace(";", "");
//				String grant = row.getCell(2).toString().replace(";", "");
//				String lab = row.getCell(3).toString().replace(";", "");
//				String composite = row.getCell(4).toString().replace(";", "");
//				String dataType = row.getCell(5).toString().replace(";", "");
//				String view = row.getCell(6).toString().replace(";", "");
//				String cell = row.getCell(7).toString().replace(";", "");
//				String strain = row.getCell(8).toString().replace(";", "");
//				String sex = row.getCell(9).toString().replace(";", "");
//				String age = row.getCell(10).toString().replace(";", "");
//				String rnaExtract = row.getCell(11).toString().replace(";", "");
//				String readType = row.getCell(12).toString().replace(";", "");
//				String treatment = row.getCell(14).toString().replace(";", "");
//				String source = row.getCell(16).toString().replace(";", "");
//				String dccAccession = row.getCell(19).toString().replace(";", "");
//				String dataSubmit = row.getCell(20).toString().replace(";", "");
//				String subId = row.getCell(23).toString().replace(";", "");
//				String geoSampleAccession = row.getCell(24).toString().replace(";", "");
//				String mapAlgorithm = row.getCell(26).toString().replace(";", "");
//				String size = row.getCell(29).toString().replace(";", "");
				
				Sample sample = new Sample();
				
				Map<String, String> _mateData = new HashMap<String, String>();
				_mateData.put(segment.split("=")[0], segment.split("=")[1]);
				_mateData.put(grant.split("=")[0], grant.split("=")[1]);
				_mateData.put(lab.split("=")[0], lab.split("=")[1]);
				_mateData.put(composite.split("=")[0], composite.split("=")[1]);
				_mateData.put(dataType.split("=")[0], dataType.split("=")[1]);
				_mateData.put(view.split("=")[0], view.split("=")[1]);
				_mateData.put(cell.split("=")[0], cell.split("=")[1]);
				_mateData.put(strain.split("=")[0], strain.split("=")[1]);
				_mateData.put(sex.split("=")[0], sex.split("=")[1]);
				_mateData.put(age.split("=")[0], age.split("=")[1]);
				_mateData.put(rnaExtract.split("=")[0], rnaExtract.split("=")[1]);
				_mateData.put(readType.split("=")[0], readType.split("=")[1]);
				
//				_mateData.put(seqPlatform.split("=")[0], readType.split("=")[1]);
//				_mateData.put(softwareVersion.split("=")[0], readType.split("=")[1]);
				_mateData.put(source.split("=")[0], source.split("=")[1]);
				_mateData.put(dccAccession.split("=")[0], dccAccession.split("=")[1]);
				_mateData.put(dataSubmit.split("=")[0], dataSubmit.split("=")[1]);
//				_mateData.put(tissueSourceType.split("=")[0], subId.split("=")[1]);
				_mateData.put(treatment.split("=")[0], treatment.split("=")[1]);
//				_mateData.put(mapAlgorithm.split("=")[0], mapAlgorithm.split("=")[1]);
//				_mateData.put(size.split("=")[0], size.split("=")[1]);
				
//				if(StringUtils.isNotEmpty(geoSampleAccession))
//				{
//					_mateData.put(geoSampleAccession.split("=")[0], geoSampleAccession.split("=")[1]);
//				}
				
				String detail = tissues.get(cell.split("=")[1]);
				if(detail != null)
				{
					
					detail += " normal ";
				}else {
					detail = "none normal ";
				}
				
//				if(cell.split("=")[1].equals("10T1/2"))
//				{
//					detail = "embryonic normal";
//				} else if(cell.split("=")[1].equals("C2C12")) {
//					detail = "skeletal muscle normal";
//				}
				
				String antibody = rnaExtract.split("=")[1];
				
				if(antibody != null)
				{
					detail += antibody;
				}
				
				sample.setAntibody(antibody);
				sample.setCell(cell.split("=")[1]);
				sample.setCreateTiemStamp(DateUtils.getNowDate());
				sample.setDeleted(0);
				sample.setDetail(detail);
				sample.descMap(_mateData);
				if(sheetName.contains("Hist")) {
					sample.setEtype(ExperimentType.CHIP_SEQ_HISTONE.getValue());
				} else {
					sample.setEtype(ExperimentType.CHIP_SEQ_TF.getValue());
				}
				
				sample.setFactor(antibody);
				sample.setFromType("mouse");
				sample.setInstrument(antibody);
				sample.setLab(lab.split("=")[1]);
				sample.setPubmedUrl("http://www.ncbi.nlm.nih.gov/pubmed/25409824");
				sample.setReadCount(0);
				sample.setSegment(segment.split("=")[1]);
				sample.setSampleCode(barcode);
				sample.setSource(SourceType.ENCODE.getValue());
				sample.setSourceUrl("http://hgdownload-test.cse.ucsc.edu/goldenPath/mm9/encodeDCC/wgEncodeCaltechRnaSeq");
				sample.setTimeStamp(dataSubmit.split("=")[1]);
				sample.setUrl(url);
				sampleList.add(sample);
			}
			
			for(Sample sample : sampleList)
			{
				Integer sampleId = sampleDAO.getSequenceId(SourceType.ENCODE);
				sample.setSampleId(sampleId);
			}
			sampleNewDAO.create(sampleList);
		}
	}
	
	public Map<String, String> getDetailByCellFromWeb(String url) {
		Map<String, String> result = new HashMap<String, String>();
		try {
			Document doc = Jsoup.connect(url).timeout(300000).get();
			Elements els_tbody = doc.select("tbody");
			Elements els_tr = els_tbody.select("tr");
			for (int i=0;i <els_tr.size(); i++) {
//				CellTypeDesc fd = new CellTypeDesc();
				
				Elements els_td = els_tr.get(i).select("td");
				String cell = els_td.get(0).text();
				String tissue = els_td.get(3).text();
//				System.out.println("cell: "+ cell + " tissue: "+tissue);
				result.put(cell, tissue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

}
