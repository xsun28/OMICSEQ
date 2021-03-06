package com.omicseq.core.batch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;

import com.omicseq.bean.SampleItem;
import com.omicseq.common.Constants;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SortType;
import com.omicseq.common.SourceType;
import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.concurrent.WaitFutureTask;
import com.omicseq.core.GeneCache;
import com.omicseq.core.MiRNASampleCache;
import com.omicseq.core.PropertiesHolder;
import com.omicseq.core.SampleCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.domain.Gene;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.MiRNARank;
import com.omicseq.domain.MiRNASample;
import com.omicseq.domain.Sample;
import com.omicseq.domain.TxrRef;
import com.omicseq.pathway.PathWaySample;
import com.omicseq.store.criteria.GeneRankCriteria;
import com.omicseq.store.dao.ISampleDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.store.helper.MongodbHelper;
import com.omicseq.utils.DateTimeUtils;

public class GeneRankExport extends BaseGeneRankBatch {
    private static GeneRankExport single = new GeneRankExport();
    private static ISampleDAO sampleDAO = DAOFactory.getDAOByTableType(ISampleDAO.class, Constants.STAT_SUFFIX);

    private GeneRankExport() {
    }

    static class ExportCallable extends BaseCallable<Object, GeneRankExport> {
        public ExportCallable(GeneRankExport ref, List<GeneRankCriteria> criteries) {
            super(ref, criteries);
        }

        @Override
        public Object call() throws Exception {
            DateTime dt = DateTime.now();
            try {
                ref.start();
                
                for (final GeneRankCriteria criteria : criteries) {
                    List<Integer> geneIds = geneCache.getGeneIds();
//                	List<MiRNA> miRna = miRnadao.find(new SmartDBObject());
//                    List<Integer> geneIds = new ArrayList<Integer>();
//                	for(MiRNA m : miRna)
//                	{
//                		geneIds.add(m.getMiRNAId());
//                	}
                    Collections.sort(geneIds);
                    int thread = 4;
                    final BlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>(geneIds);
                    Semaphore semaphore = new Semaphore(thread);
                    List<WaitFutureTask<Object>> tasks = new ArrayList<WaitFutureTask<Object>>(thread);
                    Callable<Object> callable = new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            while (true) {
                                Integer geneId = queue.poll();
                                if(geneId < 5990)
                                {
                                	continue;
                                }
                                criteria.setGeneId(geneId);
                                DateTime dt = DateTime.now();
                                ref.expXlsx(criteria);
                                if (single.logger.isDebugEnabled()) {
                                    single.logger.debug("生成{} Excel文件,用时:{}", geneId, DateTimeUtils.used(dt));
                                }
                                if (queue.isEmpty()) {
                                    break;
                                }
                            }
                            return null;
                        }
                    };
                    for (int i = 0; i < thread; i++) {
                        tasks.add(new WaitFutureTask<Object>(callable, semaphore));
                    }
                    ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor().blockRun(tasks, 10l, TimeUnit.DAYS);
                }
            } catch (Exception e) {
                ref.logger.error("生成Excel文件出错:", e);
            } finally {
                ref.stop();
                single.logger.debug("生成Excel用时:{}", DateTimeUtils.used(dt));
            }
            return Boolean.TRUE;
        }
    }

    public XSSFWorkbook buildWorkbook(GeneRankCriteria criteria) {
        DateTime dt = DateTime.now();
        XSSFWorkbook wb = new XSSFWorkbook();
        
        XSSFSheet st = wb.createSheet("sheet1");
        XSSFCellStyle styleDouble = wb.createCellStyle();
        XSSFDataFormat formatDouble = wb.createDataFormat();  
		styleDouble.setDataFormat(formatDouble.getFormat("#,##0.00000")); 
		
		XSSFRow header = st.createRow(0);
        String[] arr = "Rank,DataSetID,DataType, Cell,Factor,Detail,Order/Total,Percentile(%),tssTesCount,tss5KCount,Study,Lab".split(",");
        for (int i = 0; i < arr.length; i++) {
            CellUtil.createCell(header, i, arr[i]);
        }
        List<GeneRank> ranks = geneRankDAO.findByCriteria(criteria);
        for (int i = 0; i < ranks.size(); i++) {
            GeneRank geneRank = ranks.get(i);
            Sample sample = SampleCache.getInstance().getSampleById(geneRank.getSampleId());
            if (null == sample) {
                if (logger.isWarnEnabled()) {
                    logger.warn("{} not in sample cache!", geneRank.getSampleId());
                }
                continue;
            }
            int idx = i + 1;
            XSSFRow row = st.createRow(idx);
            String[] _arr = toArray(idx, geneRank, sample);
            for (int j = 0; j < _arr.length; j++) {
            	if(j==7 || j==8 || j==9) {
            		XSSFCell cell = row.createCell(j);
            		cell.setCellStyle(styleDouble);
            		cell.setCellValue(Double.parseDouble((_arr[j] == null || "".equals(_arr[j]))?"1":_arr[j]));
            	} else {
            		CellUtil.createCell(row, j, _arr[j]);
            	}
            }
        }
        
        XSSFSheet st2 = wb.createSheet("sheet2");
        XSSFRow header2 = st2.createRow(0);
        CellUtil.createCell(header2, 0, "tssTesCount Remark");
        CellUtil.createCell(header2, 1, "TSS5K_COUN Remark");
        
        XSSFRow row2 = st2.createRow(1);
        CellUtil.createCell(row2, 0, "statistic Read of this dataset in GeneBody,get the sample_readcount,because every GeneBody's length is not same,TSSTES_COUNT=sample_readcount/len(Genebody)。");
        CellUtil.createCell(row2, 1, "statistic Read of this dataset in Promoter，get the sample_readcount，it will be normalized and then TSS5K_COUNT=sample_readcount/10000");
        
        XSSFRow row3 = st2.createRow(2);
        CellUtil.createCell(row3, 0, "If this dataset contains INPUT, deal TSSTES_COUNT\n first to get sample and input TSSTES_COUNT，then exclude TSSTES_COUNT of INPUT as :sample_readcount-(input_readcount*inputtotalread/sampletotalread)");
        
        XSSFRow row4 = st2.createRow(3);
        CellUtil.createCell(row4, 0, "TCGA Assemble Of CNV ExperimentType\n COUNT_CNV=  log_2⁡(sample_readcount/2)");
        
        XSSFRow row5 = st2.createRow(4);
        CellUtil.createCell(row5, 0, "TCGA Assemble Of RNA-seq ExperimentType tssTesCount uesed experimental_protocol Of RPKM\nICGC Assemble of RNA-seq ExperimentType tssTesCount uesed experimental_protocol Of RSEM");
        
        for (int i = 0; i < arr.length; i++) {
            st.autoSizeColumn(i);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("生成Excel文件,用时:{}", DateTimeUtils.used(dt));
        }
        return wb;
    }

    public void expXlsx(GeneRankCriteria criteria) {
        String fname = criteria.generateKey(GeneRankCriteria.XLSXFileTempalte);
//    	String fname = criteria.generateKey("miRNA_" + GeneRankCriteria.XLSXFileTempalte);
        try {
            File root = new File(PropertiesHolder.get(PropertiesHolder.FILES, "xlxs.gene", "/data/tmp/files/omicseq/"));
            File file = new File(root, fname);
            if (file.exists()) {
                file.delete();
            }
            FileUtils.forceMkdir(root);
            if (logger.isDebugEnabled()) {
                logger.debug("生成excel文件 :{}", file.getAbsolutePath());
            }
            DateTime dt = DateTime.now();
            XSSFWorkbook wb = buildWorkbook(criteria);
//            XSSFWorkbook wb = buildWorkbook_miRNA(null, criteria.getGeneId());
            OutputStream out = new FileOutputStream(file);
            wb.write(out);
            IOUtils.closeQuietly(out);
            if (logger.isDebugEnabled()) {
                logger.debug("生成{}文件,用时:{}", fname, DateTimeUtils.used(dt));
            }

        } catch (Exception e) {
            logger.error("生成[" + fname + "]文件出错!", e);
        }

    }

    /**
     * @param geneId
     * @return
     */
    public void expCsv(GeneRankCriteria criteria) {
        String fname = criteria.generateKey(GeneRankCriteria.CSVFileTempalte);
        try {
            File root = new File(PropertiesHolder.get(PropertiesHolder.FILES, "csv.gene", "/data/tmp/files/omicseq/"));
            File file = new File(root, fname);
            if (file.exists()) {
                file.delete();
            }
            FileUtils.forceMkdir(root);
            if (logger.isDebugEnabled()) {
                logger.debug("生成CSV文件 :{}", file.getAbsolutePath());
            }
            DateTime dt = DateTime.now();
            Collection<String> lines = new ArrayList<String>();
            lines.add("Rank,DataSetID,DataType, Cell,Factor,Detail,Order/Total,Percentile(%),tssTesCount,tss5KCount,Study,Lab");// titles
            List<GeneRank> ranks = geneRankDAO.findByCriteria(criteria);
            for (int i = 0; i < ranks.size(); i++) {
                GeneRank geneRank = ranks.get(i);
                Sample sample = SampleCache.getInstance().getSampleById(geneRank.getSampleId());
                lines.add(toCsv(i + 1, geneRank, sample));
            }
            FileUtils.writeLines(file, "utf-8", lines, IOUtils.LINE_SEPARATOR_UNIX);
            if (logger.isDebugEnabled()) {
                logger.debug("生成{}文件,用时:{}", fname, DateTimeUtils.used(dt));
            }
        } catch (Exception e) {
            logger.error("生成[" + fname + "]文件出错!", e);
        }
    }

    private String toCsv(int idx, GeneRank geneRank, Sample sample) {
        String[] arr = toArray(idx, geneRank, sample);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            sb.append(column(arr[i])).append(i != arr.length - 1 ? "," : "");
        }
        return sb.toString();
    }

    private String[] toArray(int idx, GeneRank geneRank, Sample sample) {
        SampleItem item = new SampleItem(sample, geneRank);
        // "Rank,DataSetID,DataType, Cell,Factor,Order/Total,Percentile(%),Study,Lab"
        String[] arr = new String[12];
        // Rank
        arr[0] = String.valueOf(idx);
        // DataSetID
        arr[1] = String.valueOf(item.getSampleId());
        // DataType
        arr[2] = StringUtils.trimToEmpty(item.getDataType());
        // Cell
        arr[3] = StringUtils.trimToEmpty(item.getCell());
        // Factor
        arr[4] = StringUtils.trimToEmpty(item.getFactor());
        //Detail
        arr[5] = StringUtils.trimToEmpty(item.getDetail());
        // Order/Total
        arr[6] = String.format("%s/%s", item.getRank(), item.getTotal());
        // Percentile(%)
        arr[7] = StringUtils.trimToEmpty(item.getPercentileFormat());
        //tssTesCount
        arr[8] = StringUtils.trimToEmpty(String.valueOf(item.getTssTesCount()));
        //tss5KCount
        arr[9] = StringUtils.trimToEmpty(String.valueOf(item.getTssCount()));
        // Study
        arr[10] = StringUtils.trimToEmpty(item.getStudy());
        // Lab
        arr[11] = StringUtils.trimToEmpty(item.getLab());
        return arr;
    }
    
   
    private String column(String str) {
        String val = StringUtils.trimToEmpty(str);
        return String.format("\"%s\"", val.replaceAll("\"", "'"));
    }

    @Override
    protected Callable<Object> getCallable(List<GeneRankCriteria> criteries) {
        return new ExportCallable(new GeneRankExport(), criteries);
    }

    public static void main(String[] args) {
//    	SampleCache.getInstance().init();
//    	MiRNASampleCache.getInstance().init();
    	geneCache.init();
//      single.refresh();
    	
    	TxrRefCache.getInstance().init();
    	SmartDBObject query = new SmartDBObject("source", 2);
    	query.put("etype", 1);
    	query.put("deleted", 0);
    	SmartDBObject factorQuery = new SmartDBObject("$regex", "^(?!H)");
    	factorQuery.append("$ne", "Input");
    	query.put("factor", factorQuery);
//    	query.put("sampleId", 202213);
    	List<Sample> sampleList = sampleDAO.find(query, 0, 300);
    	GeneRankExport  ex = new GeneRankExport();
    	ex.exportExcelOfRankList(sampleList);
//    	GeneRankCriteria criteria = new GeneRankCriteria();
//    	List<Integer> etypeList = new ArrayList<Integer>();
//    	etypeList.add(11);
//    	criteria.setEtypeList(etypeList);
//    	List<Integer> geneIds = new ArrayList<Integer>();
//    	geneIds.add(2303);
//    	geneIds.add(13659);
//    	geneIds.add(2178);
//    	geneIds.add(10887);
//    	geneIds.add(32197);
//    	geneIds.add(26962);
//    	geneIds.add(23457);
//    	geneIds.add(30677);
//    	geneIds.add(508);
//    	geneIds.add(9200);
//    	for(int i=0; i<geneIds.size(); i++)
//    	{
//    		criteria.setGeneId(geneIds.get(i));
//        	GeneRankExport  ex = new GeneRankExport();
//        	ex.expXlsx(criteria);
//    	}
    }

    private void exportExcelOfRankList(List<Sample> sampleList) {
    	File file = new File("/home/tomcat/sample_gene_rank.xlsx");
//    	File file = new File("F:\\sample_gene_rank.xlsx");
    	SXSSFWorkbook wb = new SXSSFWorkbook(-1);
        Sheet st = wb.createSheet("sheet1");
    	try {
	        Row header = st.createRow(0);
	        CellUtil.createCell(header, 2, "sampleId");
	        for (int i = 0; i < sampleList.size(); i++) {
	        	String sampleId = sampleList.get(i).getSampleId().toString();
	        	CellUtil.createCell(header, i+3, sampleId);
	        }
	        
	        Row cellRow = st.createRow(1);
	        CellUtil.createCell(cellRow, 2, "cell");
	        for (int i = 0; i < sampleList.size(); i++) {
	        	
	        	String cell = sampleList.get(i).getCell();
	        	CellUtil.createCell(cellRow, i+3, cell);
	        }
	        
	        Row detailRow = st.createRow(2);
	        CellUtil.createCell(detailRow, 2, "detail");
	        for (int i = 0; i < sampleList.size(); i++) {
	        	
	        	String detail = sampleList.get(i).getDetail();
	        	CellUtil.createCell(detailRow, i+3, detail);
	        }
	     
	        Row titleRow = st.createRow(3);
	        CellUtil.createCell(titleRow, 0, "geneId");
	        CellUtil.createCell(titleRow, 1, "refseq");
	        CellUtil.createCell(titleRow, 2, "geneSymbol");
	        
	        List<Integer> geneIds = geneCache.getGeneIds();
	        
	        CellStyle styleDouble = wb.createCellStyle();
	        DataFormat formatDouble = wb.createDataFormat();  
			styleDouble.setDataFormat(formatDouble.getFormat("#,##0.00000"));
			boolean isFind;
	        for (int i = 0; i < sampleList.size(); i++) {
	        	SmartDBObject query = new SmartDBObject("sampleId", sampleList.get(i).getSampleId());
	        	query.addSort("geneId", SortType.ASC);
	        	List<GeneRank> geneRankList = geneRankDAO.find(query);
	        	for(int g=0; g < geneIds.size(); g++) {
	        		Row row = st.getRow(g+4);
	        		if(row == null){
	        			row = st.createRow(g+4);
	        			String refseq = geneCache.getGeneById(geneIds.get(g)).get(0).getTxName();
			        	String geneSymbol = TxrRefCache.getInstance().getGeneSymbolByRefSeq(refseq);
			        	CellUtil.createCell(row, 0, geneIds.get(g).toString());
			    		CellUtil.createCell(row, 1, refseq);
			    		CellUtil.createCell(row, 2, geneSymbol);
	        		}
	        		
	        		Integer geneId = geneIds.get(g);
	        		isFind = false;
	        		for(GeneRank geneRank : geneRankList)
	        		{
	        			if(geneId.equals(geneRank.getGeneId())) {
	        				Cell cell0 = row.createCell(i+3);
	        				cell0.setCellStyle(styleDouble);
	        				cell0.setCellValue(geneRank.getMixturePerc());
	  	            		isFind = true;
	  	            		break;
	        			}
	        		}
	        		if(!isFind)
	        		{
	        			row.createCell(i+3).setCellValue(1.00000);
	        		}
	        	}
	        }
	        
			OutputStream out = new FileOutputStream(file);
			wb.write(out);
			IOUtils.closeQuietly(out);
		}  catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static GeneRankExport getInstance() {
        return single;
    }
    
    public XSSFWorkbook buildWorkbookByDownload(String geneSymbols,
			String sources, String etypes, String cell, String factor,String [] titles){
    	XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet st = wb.createSheet("sheet1");
        XSSFCellStyle styleDouble = wb.createCellStyle();
        XSSFDataFormat formatDouble = wb.createDataFormat();  
		styleDouble.setDataFormat(formatDouble.getFormat("#,##0.00000")); 
		XSSFRow header = st.createRow(0);
		String [] symbols = geneSymbols.split(",");
		String [] cells = null;
		int count = 0; 
		if(StringUtils.isNotEmpty(StringUtils.trimToEmpty(cell))){
			cells = cell.split(",");
		}
				
		String [] factors = null;
		if(StringUtils.isNotEmpty(StringUtils.trimToEmpty(factor))){
			factors = factor.split(",");
		}
		int n = symbols.length;
		String [] newTitles = new String [titles.length+n-1];
		int h = 0;
		for( int i=0;i<titles.length;i++){
			if(titles[i].equalsIgnoreCase("Percentile(%)")){
				for( h= 0;h<n;h++){
					newTitles[i+h] =  titles[i]+"/"+symbols[h];
				}
				h--;
				continue;
			}else{
				newTitles[i+h] = titles[i];
			}
		}
		for (int i = 0; i < newTitles.length; i++) {
            CellUtil.createCell(header, i, newTitles[i]);
        }
		//BasicDBList values = new BasicDBList();
		List<Integer> values = new ArrayList<Integer>();
		Map<Integer,String> map = new HashMap<Integer,String>();
		for(String s :symbols){
			List<TxrRef> txrRefList = TxrRefCache.getInstance().getTxrRefBySymbol(s.toLowerCase());
			if(txrRefList !=null && txrRefList.size() != 0){
				for(TxrRef t : txrRefList){
					String refseq = t.getRefseq();
					if(refseq !=null && !"".equals(refseq)){
						//根据refseq对应gene表txName字段 找geneId
						//Gene gene = geneDAO.getByName(refseq); 
						Gene gene = GeneCache.getInstance().getGeneByName(refseq);
						if(gene != null){
							values.add(gene.getGeneId());
							map.put(gene.getGeneId(),s);
							break;
						}
					}
				}
			}
		}
		List<Integer> sourceList = new ArrayList<Integer>();
		if(StringUtils.isNotBlank(sources)){
			for(String s : sources.split(",")){
				sourceList.add(Integer.parseInt(s));
			}
		}else{
			for (SourceType type : SourceType.getUiMap().values()){
				sourceList.add(type.getValue());
			}
			
		}
		List<Integer> etypeList = new ArrayList<Integer>();
		if(StringUtils.isNotBlank(etypes)){
			for(String s : etypes.split(",")){
				etypeList.add(Integer.parseInt(s));
			}
		}else{
			for (ExperimentType type : ExperimentType.getUiMap().values()){
				etypeList.add(type.getValue());
			}
			
		}
		for(Integer geneId : values){
			SmartDBObject query = new SmartDBObject();
			query.put("geneId", geneId);
			if (CollectionUtils.isNotEmpty(sourceList)) {
	            query = MongodbHelper.and(query, MongodbHelper.in("source", sourceList.toArray()));
	        }
	        if (CollectionUtils.isNotEmpty(etypeList)) {
	            query = MongodbHelper.and(query, MongodbHelper.in("etype", etypeList.toArray()));
	        }
	        query.addSort("mixturePerc", SortType.ASC);
	        query.addSort("sampleId", SortType.ASC);
	        List<GeneRank> geneRank = geneRankDAO.find(query,0,1000);
		        
			
			double d = Math.sqrt(12*n);
	        
			HashMap<Integer, Double> mapA = new HashMap<Integer, Double>();
			HashMap<Integer, Integer> mapB = new HashMap<Integer, Integer>();
			HashMap<Integer, Double> mapC = new HashMap<Integer, Double>();
			HashMap<Integer, Integer> mapSource = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> mapEtype = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> mapCount = new HashMap<Integer, Integer>();
			HashMap<Integer, Double> mapTss5kCount = new HashMap<Integer, Double>();
			HashMap<Integer, Double> mapTss5kPerc = new HashMap<Integer, Double>();
			HashMap<Integer, Double> mapTssTesCount = new HashMap<Integer, Double>();
	
			for(GeneRank g : geneRank){
				mapCount.put(g.getSampleId(),g.getTotalCount());
				
				if (mapA.containsKey(g.getSampleId())) {
					if(g.getMixturePerc() != null)
					{
						mapA.put(g.getSampleId(), mapA.get(g.getSampleId())+g.getMixturePerc());
						mapB.put(g.getSampleId(), mapB.get(g.getSampleId()) +1);
						mapC.put(g.getSampleId(), mapC.get(g.getSampleId())+g.getMixturePerc());
					}
					
				}else {
					if(g.getMixturePerc() != null) {
						mapA.put(g.getSampleId(), g.getMixturePerc());
						mapB.put(g.getSampleId(), 1);
						mapC.put(g.getSampleId(), g.getMixturePerc());
					}
				}
				if(!mapSource.containsKey(g.getSampleId()))
				{
					mapSource.put(g.getSampleId(), g.getSource());
				}
				if(!mapEtype.containsKey(g.getSampleId()))
				{
					mapEtype.put(g.getSampleId(), g.getEtype());
				}
			}
			
			Iterator<Integer> itC = mapC.keySet().iterator();
			while(itC.hasNext()) {
				int sampleId = itC.next();
				if(mapB.get(sampleId) < n/2)
				{
					if(mapA.get(sampleId) != null)
					{
						mapA.remove(sampleId);
					}
				}
			}
			
			Iterator<Integer> it = mapA.keySet().iterator();
			List<PathWaySample> psList = new ArrayList<PathWaySample>();
			while(it.hasNext()) {
				Integer sampleId = (Integer)it.next();
				double avgR = mapA.get(sampleId)/mapB.get(sampleId);
				double b = d*(avgR - 0.5);
				
				double test = NORMSDIST(b);
				
				java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.0000000"); 
				try {
					PathWaySample ps = new PathWaySample();
					ps.setSampleId(sampleId);
//					ps.setCount(mapCount.get(sampleId));
					ps.setAvgA(avgR);
					ps.setB(b);
					ps.setRank(Double.valueOf(df.format(test)));
					ps.setSource(mapSource.get(sampleId));
					ps.setEtype(mapEtype.get(sampleId));
//					ps.setTss5kCount(mapTss5kCount.get(sampleId));
//					ps.setTss5kPerc(mapTss5kPerc.get(sampleId));
//					ps.setTssTesCount(mapTssTesCount.get(sampleId));
					
					psList.add(ps);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
			//根据rank值排序
			Collections.sort(psList, new Comparator<PathWaySample>() {
				@Override
				public int compare(PathWaySample o1, PathWaySample o2) {
					if(null != o1.getRank() && null != o2.getRank())
					{
						return o1.getRank().compareTo(o2.getRank());
					}
					return 0;
				}
			});
//			if(psList!=null){
//		        for(PathWaySample ps : psList){
//		        	Sample sample = SampleCache.getInstance().getSampleById(ps.getSampleId());
//		            if (sample == null) {
//		                logger.warn(" can't find sample for id : " + ps.getSampleId());
//		                continue;
//		            }
//		            
////		            SampleItem sampleItem = new SampleItem(sample, null,ps.getCount(), ps.getAvgA(),ps.getTss5kPerc(), null, false, null, ps.getTss5kCount(), ps.getTssTesCount());
//		            //去除cell中#号 ，避免导出异常
//		            if(sampleItem.getCell()!=null && sampleItem.getCell().contains("#")){
//		            	String cell1 = sampleItem.getCell().split("#")[0];
//		            	sampleItem.setCell(cell1);
//		            }
//		            sampleItem.setMixturePerc(MathUtils.floor(ps.getAvgA()*100)); //实验数据对应该基因组的平均percentile
//		            sampleItem.setPathwayOfRank(ps.getRank());
//		            
//		            
//		            String[] _arr = new String[newTitles.length];
//		            int [] index = {-1,-1,-1};
//		            DecimalFormat df = new DecimalFormat("0.000");
//		            for(int j=0;j<newTitles.length;j++){
//		            	//"Rank","DataSetID","DataType", "Cell","Factor","Order/Total","Percentile(%)",
//		            	//"tssTesCount","tss5KCount","Study","Lab"
//		            	if("Rank".equalsIgnoreCase(newTitles[j])){
//		            		_arr[j] = String.valueOf(count+1);
//		            		continue;
//		            	}
//		            	if("DataSetID".equalsIgnoreCase(newTitles[j])){
//		            		_arr[j] = StringUtils.trimToEmpty(sampleItem.getSampleId().toString());
//		            		continue;
//		            	}
//		            	if("DataType".equalsIgnoreCase(newTitles[j])){
//		            		_arr[j] = StringUtils.trimToEmpty(sampleItem.getDataType());
//		            		continue;
//		            	}
//		            	if("Cell".equalsIgnoreCase(newTitles[j])){
//		            		_arr[j] = StringUtils.trimToEmpty(sampleItem.getCell());
//		            		continue;
//		            	}
//		            	if("Factor".equalsIgnoreCase(newTitles[j])){
//		            		_arr[j] = StringUtils.trimToEmpty(sampleItem.getFactor());
//		            		continue;
//		            	}
//		            	if("Order/Total".equalsIgnoreCase(newTitles[j])){
//		            		BigDecimal bigDecimal = new BigDecimal(sampleItem.getMixturePerc() * sampleItem.getTotal()).setScale(0, BigDecimal.ROUND_HALF_UP);
//		                    int rank = bigDecimal.intValue();
//		            		_arr[j] = String.format("%s/%s", rank, sampleItem.getTotal());
//		            		continue;
//		            	}
//		            	if(("Percentile(%)/"+ map.get(geneId)).equalsIgnoreCase(newTitles[j])){
//		            		_arr[j] =  StringUtils.trimToEmpty(String.valueOf(sampleItem.getMixturePerc()));
//		            		index[0] = j;
//		            		continue;
//		            	}
//		            	if("tssTesCount".equalsIgnoreCase(newTitles[j])){
//		            		_arr[j] = StringUtils.trimToEmpty(String.valueOf(df.format(sampleItem.getTssTesCount()*100)));
//		            		index[1] = j;
//		            		continue;
//		            	}
//		            	if("tss5KCount".equalsIgnoreCase(newTitles[j])){
//		            		_arr[j] = StringUtils.trimToEmpty(String.valueOf(sampleItem.getTssCount()));
//		            		index[2] = j;
//		            		continue;
//		            	}
//		            	if("Study".equalsIgnoreCase(newTitles[j])){
//		            		_arr[j] = StringUtils.trimToEmpty(sampleItem.getStudy());
//		            		continue;
//		            	}
//		            	if("Lab".equalsIgnoreCase(newTitles[j])){
//		            		_arr[j] = StringUtils.trimToEmpty(sampleItem.getLab());
//		            		continue;
//		            	}
//		            	
//		            }
//		            String sampleCell = sampleItem.getCell()==null?"":sampleItem.getCell().toLowerCase();
//    				String sampleFactor = sampleItem.getFactor()==null?"":sampleItem.getFactor().toLowerCase();
//		            if(cells == null && factors ==null ){
//		            	int idx = ++count;
//		   	            XSSFRow row = st.createRow(idx);
//		           		for (int j = 0; j < _arr.length; j++) {
//		  	            	if(j==index[0] || j==index[1] || j==index[2]) {
//		  	            		XSSFCell cell0 = row.createCell(j);
//		  	            		cell0.setCellStyle(styleDouble);
//		  	            		cell0.setCellValue(Double.parseDouble((_arr[j] == null || "".equals(_arr[j]))?"1":_arr[j]));
//		  	            	} else {
//		  	            		CellUtil.createCell(row, j, _arr[j]);
//			  	            }
//		            	}
//		            }else if(cells!=null && factors ==null){
//		            	for(String c : cells){
//		            		if(c.equalsIgnoreCase(StringUtils.trimToEmpty(sampleItem.getCell()))){
//		            			int idx = ++count;
//				   	            XSSFRow row = st.createRow(idx);
//				           		for (int j = 0; j < _arr.length; j++) {
//				  	            	if(j==index[0] || j==index[1] || j==index[2]) {
//				  	            		XSSFCell cell0 = row.createCell(j);
//				  	            		cell0.setCellStyle(styleDouble);
//				  	            		cell0.setCellValue(Double.parseDouble((_arr[j] == null || "".equals(_arr[j]))?"1":_arr[j]));
//				  	            	} else {
//				  	            		CellUtil.createCell(row, j, _arr[j]);
//					  	            }
//				            	}
//		            		}else if(c.startsWith("*")){
//		            			String tmp = c.replace("*", "").toLowerCase();
//		            			if(sampleCell.endsWith(tmp)){
//		            				int idx = ++count;
//					   	            XSSFRow row = st.createRow(idx);
//					           		for (int j = 0; j < _arr.length; j++) {
//					  	            	if(j==index[0] || j==index[1] || j==index[2]) {
//					  	            		XSSFCell cell0 = row.createCell(j);
//					  	            		cell0.setCellStyle(styleDouble);
//					  	            		cell0.setCellValue(Double.parseDouble((_arr[j] == null || "".equals(_arr[j]))?"1":_arr[j]));
//					  	            	} else {
//					  	            		CellUtil.createCell(row, j, _arr[j]);
//						  	            }
//					            	}
//		            			}
//		            		}else if(c.endsWith("*")){
//		            			String tmp = c.replace("*", "").toLowerCase();
//		            			if(sampleCell.startsWith(tmp)){
//		            				int idx = ++count;
//					   	            XSSFRow row = st.createRow(idx);
//					           		for (int j = 0; j < _arr.length; j++) {
//					  	            	if(j==index[0] || j==index[1] || j==index[2]) {
//					  	            		XSSFCell cell0 = row.createCell(j);
//					  	            		cell0.setCellStyle(styleDouble);
//					  	            		cell0.setCellValue(Double.parseDouble((_arr[j] == null || "".equals(_arr[j]))?"1":_arr[j]));
//					  	            	} else {
//					  	            		CellUtil.createCell(row, j, _arr[j]);
//						  	            }
//					            	}
//		            			}
//		            		}
//		           		}
//		           	}else if(factors!=null && cells==null){
//		           		for(String c : factors){
//		           			if(c.equalsIgnoreCase(StringUtils.trimToEmpty(sampleItem.getFactor()))){
//		           				int idx = ++count;
//			    	            XSSFRow row = st.createRow(idx);
//			            		for (int j = 0; j < _arr.length; j++) {
//				  	            	if(j==index[0] || j==index[1] || j==index[2]) {
//					  	            	XSSFCell cell0 = row.createCell(j);
//					  	            	cell0.setCellStyle(styleDouble);
//					  	           		cell0.setCellValue(Double.parseDouble((_arr[j] == null || "".equals(_arr[j]))?"1":_arr[j]));
//					  	           	} else {
//					  		           	CellUtil.createCell(row, j, _arr[j]);
//					  	           	}
//				            	}
//		            		}else if(c.startsWith("*")){
//		            			String tmp = c.replace("*", "").toLowerCase();
//		            			if(sampleFactor.endsWith(tmp)){
//		            				int idx = ++count;
//					   	            XSSFRow row = st.createRow(idx);
//					           		for (int j = 0; j < _arr.length; j++) {
//					  	            	if(j==index[0] || j==index[1] || j==index[2]) {
//					  	            		XSSFCell cell0 = row.createCell(j);
//					  	            		cell0.setCellStyle(styleDouble);
//					  	            		cell0.setCellValue(Double.parseDouble((_arr[j] == null || "".equals(_arr[j]))?"1":_arr[j]));
//					  	            	} else {
//					  	            		CellUtil.createCell(row, j, _arr[j]);
//						  	            }
//					            	}
//		            			}
//		            		}else if(c.endsWith("*")){
//		            			String tmp = c.replace("*", "").toLowerCase();
//		            			if(sampleFactor.startsWith(tmp)){
//		            				int idx = ++count;
//					   	            XSSFRow row = st.createRow(idx);
//					           		for (int j = 0; j < _arr.length; j++) {
//					  	            	if(j==index[0] || j==index[1] || j==index[2]) {
//					  	            		XSSFCell cell0 = row.createCell(j);
//					  	            		cell0.setCellStyle(styleDouble);
//					  	            		cell0.setCellValue(Double.parseDouble((_arr[j] == null || "".equals(_arr[j]))?"1":_arr[j]));
//					  	            	} else {
//					  	            		CellUtil.createCell(row, j, _arr[j]);
//						  	            }
//					            	}
//		            			}
//		            		}
//		            	}
//		            }else if(factors!=null && cells!=null){
//		           		for(String c : cells){
//		           			for(String f : factors){
//			           			if(c.equalsIgnoreCase(StringUtils.trimToEmpty(sampleItem.getCell())) && f.equalsIgnoreCase(StringUtils.trimToEmpty(sampleItem.getFactor()))){
//			           				int idx = ++count;
//				    	            XSSFRow row = st.createRow(idx);
//				            		for (int j = 0; j < _arr.length; j++) {
//					  	            	if(j==index[0] || j==index[1] || j==index[2]) {
//					  	            		XSSFCell cell0 = row.createCell(j);						  	            		cell0.setCellStyle(styleDouble);
//						  	            	cell0.setCellValue(Double.parseDouble((_arr[j] == null || "".equals(_arr[j]))?"1":_arr[j]));
//						  	            } else {
//						  		           	CellUtil.createCell(row, j, _arr[j]);
//						  	           	}
//					            	}
//			            		}else if(c.contains("*") && !f.contains("*")){
//			            			String tmp = c.replace("*", "").toLowerCase();
//			            			if(c.startsWith("*") ){
//				            			if(sampleCell.endsWith(tmp)){
//				            				int idx = ++count;
//							   	            XSSFRow row = st.createRow(idx);
//							           		for (int j = 0; j < _arr.length; j++) {
//							  	            	if(j==index[0] || j==index[1] || j==index[2]) {
//							  	            		XSSFCell cell0 = row.createCell(j);
//							  	            		cell0.setCellStyle(styleDouble);
//							  	            		cell0.setCellValue(Double.parseDouble((_arr[j] == null || "".equals(_arr[j]))?"1":_arr[j]));
//							  	            	} else {
//							  	            		CellUtil.createCell(row, j, _arr[j]);
//								  	            }
//							            	}
//				            			}
//			            			}else if(c.endsWith("*")){
//				            			if(sampleCell.startsWith(tmp)){
//				            				int idx = ++count;
//							   	            XSSFRow row = st.createRow(idx);
//							           		for (int j = 0; j < _arr.length; j++) {
//							  	            	if(j==index[0] || j==index[1] || j==index[2]) {
//							  	            		XSSFCell cell0 = row.createCell(j);
//							  	            		cell0.setCellStyle(styleDouble);
//							  	            		cell0.setCellValue(Double.parseDouble((_arr[j] == null || "".equals(_arr[j]))?"1":_arr[j]));
//							  	            	} else {
//							  	            		CellUtil.createCell(row, j, _arr[j]);
//								  	            }
//							            	}
//				            			}
//			            			}
//			            		}else if(f.contains("*") && !c.contains("*")){
//		            				String tmp = f.replace("*", "").toLowerCase();
//			            			if(f.startsWith("*") ){
//				            			if(sampleFactor.endsWith(tmp)){
//				            				int idx = ++count;
//							   	            XSSFRow row = st.createRow(idx);
//							           		for (int j = 0; j < _arr.length; j++) {
//							  	            	if(j==index[0] || j==index[1] || j==index[2]) {
//							  	            		XSSFCell cell0 = row.createCell(j);
//							  	            		cell0.setCellStyle(styleDouble);
//							  	            		cell0.setCellValue(Double.parseDouble((_arr[j] == null || "".equals(_arr[j]))?"1":_arr[j]));
//							  	            	} else {
//							  	            		CellUtil.createCell(row, j, _arr[j]);
//								  	            }
//							            	}
//				            			}
//			            			}else if(f.endsWith("*")){
//				            			if(sampleFactor.startsWith(tmp)){
//				            				int idx = ++count;
//							   	            XSSFRow row = st.createRow(idx);
//							           		for (int j = 0; j < _arr.length; j++) {
//							  	            	if(j==index[0] || j==index[1] || j==index[2]) {
//							  	            		XSSFCell cell0 = row.createCell(j);
//							  	            		cell0.setCellStyle(styleDouble);
//							  	            		cell0.setCellValue(Double.parseDouble((_arr[j] == null || "".equals(_arr[j]))?"1":_arr[j]));
//							  	            	} else {
//							  	            		CellUtil.createCell(row, j, _arr[j]);
//								  	            }
//							            	}
//				            			}
//			            			}
//			            		}else if(f.contains("*") && c.contains("*")){
//			            			String ctmp = c.replace("*", "").toLowerCase();
//		            				String ftmp = f.replace("*", "").toLowerCase();
//		            				if(f.startsWith("*") && c.startsWith("*")){
//			            				if(sampleCell.endsWith(ctmp) && sampleFactor.endsWith(ftmp)){
//			            					int idx = ++count;
//							   	            XSSFRow row = st.createRow(idx);
//							           		for (int j = 0; j < _arr.length; j++) {
//							  	            	if(j==index[0] || j==index[1] || j==index[2]) {
//							  	            		XSSFCell cell0 = row.createCell(j);
//							  	            		cell0.setCellStyle(styleDouble);
//							  	            		cell0.setCellValue(Double.parseDouble((_arr[j] == null || "".equals(_arr[j]))?"1":_arr[j]));
//							  	            	} else {
//							  	            		CellUtil.createCell(row, j, _arr[j]);
//								  	            }
//							            	}
//			            				}
//			            			}else if(f.startsWith("*") && c.endsWith("*")){
//			            				if(sampleCell.startsWith(ctmp) && sampleFactor.endsWith(ftmp)){
//			            					int idx = ++count;
//							   	            XSSFRow row = st.createRow(idx);
//							           		for (int j = 0; j < _arr.length; j++) {
//							  	            	if(j==index[0] || j==index[1] || j==index[2]) {
//							  	            		XSSFCell cell0 = row.createCell(j);
//							  	            		cell0.setCellStyle(styleDouble);
//							  	            		cell0.setCellValue(Double.parseDouble((_arr[j] == null || "".equals(_arr[j]))?"1":_arr[j]));
//							  	            	} else {
//							  	            		CellUtil.createCell(row, j, _arr[j]);
//								  	            }
//							            	}
//			            				}
//			            			}else if(f.endsWith("*") && c.endsWith("*")){
//			            				if(sampleCell.startsWith(ctmp) && sampleFactor.startsWith(ftmp)){
//			            					int idx = ++count;
//							   	            XSSFRow row = st.createRow(idx);
//							           		for (int j = 0; j < _arr.length; j++) {
//							  	            	if(j==index[0] || j==index[1] || j==index[2]) {
//							  	            		XSSFCell cell0 = row.createCell(j);
//							  	            		cell0.setCellStyle(styleDouble);
//							  	            		cell0.setCellValue(Double.parseDouble((_arr[j] == null || "".equals(_arr[j]))?"1":_arr[j]));
//							  	            	} else {
//							  	            		CellUtil.createCell(row, j, _arr[j]);
//								  	            }
//							            	}
//			            				}
//			            			}else if(f.endsWith("*") && c.startsWith("*")){
//			            				if(sampleCell.endsWith(ctmp) && sampleFactor.startsWith(ftmp)){
//			            					int idx = ++count;
//							   	            XSSFRow row = st.createRow(idx);
//							           		for (int j = 0; j < _arr.length; j++) {
//							  	            	if(j==index[0] || j==index[1] || j==index[2]) {
//							  	            		XSSFCell cell0 = row.createCell(j);
//							  	            		cell0.setCellStyle(styleDouble);
//							  	            		cell0.setCellValue(Double.parseDouble((_arr[j] == null || "".equals(_arr[j]))?"1":_arr[j]));
//							  	            	} else {
//							  	            		CellUtil.createCell(row, j, _arr[j]);
//								  	            }
//							            	}
//			            				}
//			            			}
//			            		}
//		            		}
//		            	}
//		           	}
//		        }
//			}
		}
		XSSFSheet st2 = wb.createSheet("sheet2");
	    XSSFRow header2 = st2.createRow(0);
	    CellUtil.createCell(header2, 0, "tssTesCount Remark");
	    CellUtil.createCell(header2, 1, "TSS5K_COUN Remark");
	        
	    
	    XSSFRow row2 = st2.createRow(1);
	    CellUtil.createCell(row2, 0, "statistic Read of this dataset in GeneBody,get the sample_readcount,because every GeneBody's length is not same,TSSTES_COUNT=sample_readcount/len(Genebody)。");
	    CellUtil.createCell(row2, 1, "statistic Read of this dataset in Promoter，get the sample_readcount，it will be normalized and then TSS5K_COUNT=sample_readcount/10000");
	    XSSFRow row3 = st2.createRow(2);
	    CellUtil.createCell(row3, 0, "If this dataset contains INPUT, deal TSSTES_COUNT\n first to get sample and input TSSTES_COUNT，then exclude TSSTES_COUNT of INPUT as :sample_readcount-(input_readcount*inputtotalread/sampletotalread)");
	    XSSFRow row4 = st2.createRow(3);
	    CellUtil.createCell(row4, 0, "TCGA Assemble Of CNV ExperimentType\n COUNT_CNV=  log_2⁡(sample_readcount/2)");
	    XSSFRow row5 = st2.createRow(4);
	    CellUtil.createCell(row5, 0, "TCGA Assemble Of RNA-seq ExperimentType tssTesCount uesed experimental_protocol Of RPKM\nICGC Assemble of RNA-seq ExperimentType tssTesCount uesed experimental_protocol Of RSEM");
	    for (int i = 0; i < titles.length; i++) {
	        st.autoSizeColumn(i);
	    }  
		
    	return wb;
    }
    public static double NORMSDIST(double b)
    {
        double p = 0.2316419;
        double b1 = 0.31938153;
        double b2 = -0.356563782;
        double b3 = 1.781477937;
        double b4 = -1.821255978;
        double b5 = 1.330274429;
         
        double x = Math.abs(b);
        double t = 1/(1+p*x);
         
        double val = 1 - (1/(Math.sqrt(2*Math.PI))  * Math.exp(-1*Math.pow(b, 2)/2)) 
						* (b1*t + b2 * Math.pow(t,2) + b3*Math.pow(t,3) + b4 * Math.pow(t,4) + b5 * Math.pow(t,5));
        if(b < 0)
        {
        	val = 1 - val;
        }
        return val;
    }
    
    public XSSFWorkbook buildWorkbook_miRNA(String miRNAName,int miRNAId) {
    		
    	DateTime dt = DateTime.now();
        XSSFWorkbook wb = new XSSFWorkbook();
        
        XSSFSheet st = wb.createSheet("sheet1");
        XSSFCellStyle styleDouble = wb.createCellStyle();
        XSSFDataFormat formatDouble = wb.createDataFormat();  
		styleDouble.setDataFormat(formatDouble.getFormat("#,##0.00000")); 
		
		XSSFRow header = st.createRow(0);
        String[] arr = "Rank,DataSetID,DataType, Cell,Factor,Order/Total,Percentile(%),Study,Lab".split(",");
        for (int i = 0; i < arr.length; i++) {
            CellUtil.createCell(header, i, arr[i]);
        }
        SmartDBObject query = new SmartDBObject("miRNAId",miRNAId);
//        query.put("mixtureperc", new SmartDBObject("$lte",0.01));
        query.addSort("mixtureperc", SortType.ASC);
        List<MiRNARank> ranks = miRankDao.find(query);
        DecimalFormat df = new DecimalFormat("0.000");
        for (int i = 0; i < ranks.size(); i++) {
        	MiRNARank rank = ranks.get(i);
            MiRNASample sample = MiRNASampleCache.getInstance().getMiRNASampleById(ranks.get(i).getMiRNASampleId());
            if (null == sample) {
                if (logger.isWarnEnabled()) {
                    logger.warn("{} not in sample cache!", rank.getMiRNASampleId());
                }
                continue;
            }
            int idx = i + 1;
            XSSFRow row = st.createRow(idx);
            String[] arr1 = new String[9];
            // Rank
            arr1[0] = String.valueOf(idx);
            // DataSetID
            arr1[1] = String.valueOf(sample.getMiRNASampleId());
            // DataType
            arr1[2] = StringUtils.trimToEmpty(sample.getEtype().toString());
            // Cell
            arr1[3] = StringUtils.trimToEmpty(sample.getCell());
            // Factor
            arr1[4] = StringUtils.trimToEmpty(sample.getFactor());
            // Order/Total
            BigDecimal bigDecimal = new BigDecimal(rank.getMixtureperc()*rank.getTotalCount()).setScale(0, BigDecimal.ROUND_HALF_UP);
            int order = bigDecimal.intValue();
            arr1[5] = String.format("%s/%s",order,rank.getTotalCount() );
            // Percentile(%)
            arr1[6] = StringUtils.trimToEmpty( df.format(rank.getMixtureperc() * 100));
            // Study
            arr1[7] = StringUtils.trimToEmpty(sample.getCell().split("-")[0]);
            // Lab
            arr1[8] = StringUtils.trimToEmpty(sample.getLab());
            for (int j = 0; j < arr1.length; j++) {
            	if(j==6) {
            		XSSFCell cell = row.createCell(j);
            		cell.setCellStyle(styleDouble);
            		cell.setCellValue(Double.parseDouble((arr1[j] == null || "".equals(arr1[j]))?"1":arr1[j]));
            	} else {
            		CellUtil.createCell(row, j, arr1[j]);
            	}
            }
        }
        
        for (int i = 0; i < arr.length; i++) {
            st.autoSizeColumn(i);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("生成Excel文件,用时:{}", DateTimeUtils.used(dt));
        }
        return wb;
    }

    
}
