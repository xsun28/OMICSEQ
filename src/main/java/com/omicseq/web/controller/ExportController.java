package com.omicseq.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.omicseq.common.Charsets;
import com.omicseq.common.SortType;
import com.omicseq.core.GeneCache;
import com.omicseq.core.PropertiesHolder;
import com.omicseq.core.batch.GeneRankExport;
import com.omicseq.domain.Gene;
import com.omicseq.store.criteria.GeneRankCriteria;
import com.omicseq.utils.DateUtils;

@Controller
@RequestMapping("/export/")
public class ExportController extends BaseController {
    /**
     * @param geneId
     * @param req
     * @return
     */
    private String makeFileName(Integer geneId, HttpServletRequest req) {
        // file name = genename_date_select/all;
        String term = ServletRequestUtils.getStringParameter(req, "term", "gene");
        boolean isAll = null != geneId && geneId.intValue() > 0;
        if (isAll && StringUtils.isBlank(term)) {
            List<Gene> genes = GeneCache.getInstance().getGeneById(geneId);
            term = genes.get(0).getTxName();
        }
        String sdate = DateUtils.format(new Date(), "yyyyMMdd");
        return String.format("%s_%s_%s", term, sdate, isAll ? "all" : "select");
    }
    
    private String makeFileName_miRNA(Integer miRNAId, HttpServletRequest req) {
        // file name = genename_date_select/all;
        String term = ServletRequestUtils.getStringParameter(req, "term", "miRNA");
        boolean isAll = null != miRNAId && miRNAId.intValue() > 0;
        if (isAll && StringUtils.isBlank(term)) {
            
        }
        String sdate = DateUtils.format(new Date(), "yyyyMMdd");
        return String.format("%s_%s_%s", term, sdate, isAll ? "all" : "select");
    }
  private String makeFileName_pathway(String pathwayname, HttpServletRequest req) {
        String term = ServletRequestUtils.getStringParameter(req, "term", "pathway");
        boolean isAll = null != pathwayname;
        if (isAll && StringUtils.isBlank(term)) {
            
        }
        String sdate = DateUtils.format(new Date(), "yyyyMMdd");
        return String.format("%s_%s_%s", term, sdate, isAll ? "all" : "select");
    }

    @RequestMapping("csv.htm")
    public void cvs(Integer geneId, HttpServletRequest req, HttpServletResponse res) throws IOException {
        String fname = makeFileName(geneId, req);
        Collection<String> lines = new ArrayList<String>(12);
        if (null == geneId || geneId.intValue() <= 0) {
            // 导出选中行
            String headers = (String) req.getParameter("headers");
            String data = (String) req.getParameter("data");
            if (StringUtils.isNotEmpty(headers)) {
                lines.add(headers);// titles
            }
            String[] items = data.split(";");
            for (String item : items) {
                lines.add(item);
            }
        } else {
            // 导出所有
            if (logger.isDebugEnabled()) {
                logger.debug("export all data by " + geneId);
            }
            File root = new File(PropertiesHolder.get(PropertiesHolder.FILES, "csv.gene"));
            GeneRankCriteria criteria = new GeneRankCriteria();
            criteria.setGeneId(geneId);
            File f = new File(root, criteria.generateKey(GeneRankCriteria.CSVFileTempalte));
            if (!f.exists()) {
                GeneRankExport.getInstance().expCsv(criteria);
            }
            lines = FileUtils.readLines(f);
        }
        // 定义文件名
        res.setHeader("Content-Disposition", "attachment;filename=\"" + fname + ".csv\"");
        // 设置打开方式
        res.setContentType("application/octet-stream;charset=" + Charsets.UTF_8.displayName());
        // 设置编码
        res.setCharacterEncoding(Charsets.UTF_8.displayName());
        ServletOutputStream out = res.getOutputStream();
        // UTF-8去BOM,防止出现奇怪情况添加,可以不添加
        out.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
        // 写入数据
        IOUtils.writeLines(lines, IOUtils.LINE_SEPARATOR_UNIX, out, Charsets.UTF_8);
        out.flush();
        out.close();
    }

    @RequestMapping("xlsx.htm")
    public void xlsx(Integer geneId, HttpServletRequest req, HttpServletResponse res) throws Exception {
        String fname = makeFileName(geneId, req);
        // 定义文件名
        res.setHeader("Content-Disposition", "attachment;filename=\"" + fname + ".csv\"");
        // 设置打开方式
        res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        // 设置编码
        res.setCharacterEncoding(Charsets.UTF_8.displayName());
        ServletOutputStream out = res.getOutputStream();
        if (null == geneId || geneId.intValue() <= 0) {
            // 导出选中行
            String headers = (String) req.getParameter("headers");
            String data = (String) req.getParameter("data").replaceAll("\\$", "#");
            Workbook wb = new XSSFWorkbook();
            Sheet st = wb.createSheet();
            int rownum = 0;
            String[] titles = null;
            if (StringUtils.isNotEmpty(headers)) {
                Row row = st.createRow(rownum);
                titles = headers.split(",");
                for (int i = 0; i < titles.length; i++) {
                    CellUtil.createCell(row, i, titles[i]);
                }
                rownum++;
            }
            String[] items = data.split(";");
		Pattern pattern = Pattern.compile("\\D+");
            for (String item : items) {
                String[] arr = item.split("@");
                Row row = st.createRow(rownum);
                for (int i = 0; i < arr.length; i++) {
                   
                	Matcher matcher = pattern.matcher(arr[i]);
                	boolean nondigit = matcher.find();
			if((i==6 || i==7 || i==8)&&!nondigit) {
                		CellStyle styleDouble = wb.createCellStyle();
                		DataFormat formatDouble = wb.createDataFormat();  
                		styleDouble.setDataFormat(formatDouble.getFormat("#,##0.0000000"));  
                		Cell cell = row.createCell(i);
                		cell.setCellStyle(styleDouble);
                		cell.setCellValue(Double.parseDouble((arr[i] == null || "".equals(arr[i]))?"1":arr[i]));
                	} else {
                		CellUtil.createCell(row, i, arr[i]);
                	}
                }
                rownum++;
            }
            if (null != titles) {
                for (int i = 0; i < titles.length; i++) {
                    st.autoSizeColumn(i);
                }
            }
            wb.write(out);
        } else {
            String fpath = "http://34.193.180.92/export/so_e/" + geneId + ".xlsx";
            try {
                URL url = new URL(fpath);
                InputStream is = url.openStream();
                byte[] b = new byte[1024];
                int len = 0;
                while ((len = is.read(b)) != -1) {
                    out.write(b, 0, len);
                }
            } catch (Exception e1) {
            	fpath = "http://34.193.180.92/export/so_e/" + geneId + ".csv";
            	try {
                    URL url = new URL(fpath);
                    InputStream is = url.openStream();
                    byte[] b = new byte[1024];
                    int len = 0;
                    while ((len = is.read(b)) != -1) {
                        out.write(b, 0, len);
                    }
                } catch (Exception e) {
                	if (logger.isDebugEnabled()) {
                        logger.debug("open {} failed!", fpath, e);
                    }
                    GeneRankCriteria criteria = new GeneRankCriteria();
                    criteria.setGeneId(geneId);
                    criteria.setSortType(SortType.ASC);
		    XSSFWorkbook wb = GeneRankExport.getInstance().buildWorkbook(criteria);
                    wb.write(out);
                }
            }
        }
        out.flush();
        out.close();
    }

  @RequestMapping("pathway_xlsx.htm")
    public void pathway_xlsx(String pathwayName, HttpServletRequest req, HttpServletResponse res) throws Exception {
    	
    	String fname = makeFileName_pathway(pathwayName, req);
      
        res.setHeader("Content-Disposition", "attachment;filename=\"" + fname + ".csv\"");
        
        res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        
        res.setCharacterEncoding(Charsets.UTF_8.displayName());
        ServletOutputStream out = res.getOutputStream();
        if (null == pathwayName) {
           
            String headers = (String) req.getParameter("headers");
            String data = (String) req.getParameter("data").replaceAll("\\$", "#");
            Workbook wb = new XSSFWorkbook();
            Sheet st = wb.createSheet();
            int rownum = 0;
            String[] titles = null;
            if (StringUtils.isNotEmpty(headers)) {
                Row row = st.createRow(rownum);
                titles = headers.split(",");
                for (int i = 0; i < titles.length; i++) {
                    CellUtil.createCell(row, i, titles[i]);
                }
                rownum++;
            }
            String[] items = data.split(";");
            for (String item : items) {
                String[] arr = item.split("@");
                Row row = st.createRow(rownum);
                for (int i = 0; i < arr.length; i++) {
                	Pattern pattern = Pattern.compile("\\D+");
                	Matcher matcher = pattern.matcher(arr[i]);
                	boolean nondigit = matcher.find();
                    if((i==6 || i==7 || i==8) &&!nondigit) {
                		CellStyle styleDouble = wb.createCellStyle();
                		DataFormat formatDouble = wb.createDataFormat();  
                		System.err.println("array "+i+" is "+arr[i]);
                		styleDouble.setDataFormat(formatDouble.getFormat("#,##0.0000000"));  
                		Cell cell = row.createCell(i);
                		cell.setCellStyle(styleDouble);
                		cell.setCellValue(Double.parseDouble((arr[i] == null || "".equals(arr[i]))?"1":arr[i]));
                	} else {
                		CellUtil.createCell(row, i, arr[i]);
                	}
                }
                rownum++;
            }
            if (null != titles) {
                for (int i = 0; i < titles.length; i++) {
                    st.autoSizeColumn(i);
                }
            }
            wb.write(out);
        } else {
        	List<String> experimentsList = (ArrayList<String>)req.getSession().getAttribute("experimentsList");
        	
        	List<String> sourceList = (ArrayList<String>)req.getSession().getAttribute("sourcesList");
        
                    XSSFWorkbook wb = GeneRankExport.getInstance().buildWorkbook_pathway(pathwayName,experimentsList,sourceList);
                    wb.write(out);

        }
        out.flush();
        out.close();
    }
   
    @RequestMapping("miRNA_xlsx.htm")
    public void miRNA_xlsx(Integer miRNAId, HttpServletRequest req, HttpServletResponse res) throws Exception {
        String fname = makeFileName(miRNAId, req);
        // 定义文件名
        res.setHeader("Content-Disposition", "attachment;filename=\"" + fname + ".csv\"");
        // 设置打开方式
        res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        // 设置编码
        res.setCharacterEncoding(Charsets.UTF_8.displayName());
        ServletOutputStream out = res.getOutputStream();
        if (null == miRNAId || miRNAId.intValue() <= 0) {
            // 导出选中行 
            String headers = (String) req.getParameter("headers");
            String data = (String) req.getParameter("data").replaceAll("\\$", "#");
            Workbook wb = new XSSFWorkbook();
            Sheet st = wb.createSheet();
            int rownum = 0;
            String[] titles = null;
            if (StringUtils.isNotEmpty(headers)) {
                Row row = st.createRow(rownum);
                titles = headers.split(",");
                for (int i = 0; i < titles.length; i++) {
                    CellUtil.createCell(row, i, titles[i]);
                }
                rownum++;
            }
            String[] items = data.split(";");
            for (String item : items) {
                String[] arr = item.split("@");
                Row row = st.createRow(rownum);
                for (int i = 0; i < arr.length; i++) {
                    if(i==6 ) {
                		CellStyle styleDouble = wb.createCellStyle();
                		DataFormat formatDouble = wb.createDataFormat();  
                		styleDouble.setDataFormat(formatDouble.getFormat("#,##0.0000000"));  
                		Cell cell = row.createCell(i);
                		cell.setCellStyle(styleDouble);
                		cell.setCellValue(Double.parseDouble((arr[i] == null || "".equals(arr[i]))?"1":arr[i]));
                	} else {
                		CellUtil.createCell(row, i, arr[i]);
                	}
                }
                rownum++;
            }
            if (null != titles) {
                for (int i = 0; i < titles.length; i++) {
                    st.autoSizeColumn(i);
                }
            }
            wb.write(out);
        } else {
        	 String fpath = "http://112.25.20.156/export/miRNA_so_e/" + miRNAId + ".xlsx";
             try {
                 URL url = new URL(fpath);
                 InputStream is = url.openStream();
                 byte[] b = new byte[1024];
                 int len = 0;
                 while ((len = is.read(b)) != -1) {
                     out.write(b, 0, len);
                 }
             } catch (Exception e1) {
             	fpath = "http://112.25.20.156/export/miRNA_so_e/" + miRNAId + ".csv";
             	try {
                     URL url = new URL(fpath);
                     InputStream is = url.openStream();
                     byte[] b = new byte[1024];
                     int len = 0;
                     while ((len = is.read(b)) != -1) {
                         out.write(b, 0, len);
                     }
                 } catch (Exception e) {
                 	if (logger.isDebugEnabled()) {
                         logger.debug("open {} failed!", fpath, e);
                     }
                 	String term = ServletRequestUtils.getStringParameter(req, "term", "miRNAName");
                 	XSSFWorkbook wb = GeneRankExport.getInstance().buildWorkbook_miRNA(term, miRNAId);
        			wb.write(out);
                 }
             }
        }
        out.flush();
        out.close();
    }
}
