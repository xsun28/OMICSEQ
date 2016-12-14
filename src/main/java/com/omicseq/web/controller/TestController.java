
package com.omicseq.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.omicseq.bean.GeneItem;
import com.omicseq.bean.SampleItem;
import com.omicseq.bean.SampleResult;
import com.omicseq.common.Charsets;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SortType;
import com.omicseq.common.SourceType;
import com.omicseq.core.GeneCache;
import com.omicseq.core.SampleCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.core.batch.GeneRankExport;
import com.omicseq.domain.Comment;
import com.omicseq.domain.Gene;
import com.omicseq.domain.SearchHistory;
import com.omicseq.domain.TxrRef;
import com.omicseq.domain.User;
import com.omicseq.pathway.PathWay;
import com.omicseq.store.criteria.GeneRankCriteria;
import com.omicseq.store.dao.ICellDescDao;
import com.omicseq.store.dao.ICommentDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.MongoDBManager;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateUtils;
import com.omicseq.utils.JFreeChartUtils;
import com.omicseq.web.dto.SettingDTO;
import com.omicseq.web.service.ICellTypeDescService;
import com.omicseq.web.service.IFactorDescService;
import com.omicseq.web.service.IPathWaySearchService;
import com.omicseq.web.service.ISampleSearchService;
import com.omicseq.web.service.ISearchHistoryService;
import com.omicseq.web.service.ITypeAheadService;
import com.omicseq.web.serviceimpl.MessageSourceHelper;
import com.omicseq.web.utils.RequestUtils;

/**
 * 类名称：HomeController 类描述： 首页控制层
 * 
 * 
 * 创建人：Liangxiaoyan 创建时间：2014-3-20 下午8:50:37
 * 
 * @version
 * 
 */
@Controller
@RequestMapping("/testpages/")
public class TestController extends BaseController {
    private static int pageSize = 20;

    @Autowired
    private MessageSourceHelper messageSourceHelper;
    @Autowired
    private ITypeAheadService typeAheadService;
    @Autowired
    private IPathWaySearchService pathWaySearchService;
    @Autowired
    private ISampleSearchService searchService;
    @Autowired
    private ISearchHistoryService searchHistoryService;
    
    @Autowired
    private IFactorDescService factorDescService;
    
    @Autowired
    private ICellTypeDescService cellTypeDescService;

    @RequestMapping("index.html")
    public String toIndex(ModelMap map) {
        return "toIndex";
    }
    
    @RequestMapping("confirm.json")
    @ResponseBody
    public Object confirm() {
        RequestUtils.getSession().setAttribute("isValid", true);
        return JsonSuccess(true);
    }
    
    @RequestMapping("welcome.htm")
    public String welcome(ModelMap map, HttpServletResponse response) throws IOException,
    ServletException {
    	User user = (User) RequestUtils.getAttribute("user");
        SettingDTO settingDTO = new SettingDTO();
        constructSettinsData(settingDTO, user, null);
        map.put("settingDTO", settingDTO);
        Cookie usernamecookie = RequestUtils.getCookie("userName");
        Cookie pwdcookie = RequestUtils.getCookie("userPassword");
        if (usernamecookie != null && pwdcookie != null) {
            map.put("cookiename", usernamecookie.getValue());
            map.put("cookiepassword", pwdcookie.getValue());
        }
        if (user != null && user.getPageSize() != null) {
            map.put("pageSize", user.getPageSize());
        }
        String [] column = {"Rank","DataSetID","DataType", "Cell","Factor","Order/Total","Percentile(%)","tssTesCount","tss5KCount","Study","Lab"} ;
        map.put("columns", column);
    	return "testpages/welcome";
    }
    
    @RequestMapping("index.htm")
    public String index(ModelMap map, HttpServletResponse response) throws IOException,
            ServletException {
        //判断是否已通过拦截页面
        boolean isValid = false;
        if (RequestUtils.getSession() != null) {
            Object temp = RequestUtils.getSession().getAttribute("isValid");
            isValid = temp == null ? false : (Boolean)temp;
        }
        if (!isValid) {
            return "redirect:/index.html"; 
        }
        User user = (User) RequestUtils.getAttribute("user");
        SettingDTO settingDTO = new SettingDTO();
        constructSettinsData(settingDTO, user, null);
        map.put("settingDTO", settingDTO);
        Map<String, Integer> sampleSumMap = SampleCache.getInstance().getSampleSumMap();
        map.put("sampleSumMap", sampleSumMap);
        Cookie isRemembercookie = RequestUtils.getCookie("isRemember");
        String isremember = isRemembercookie == null ? "false" : isRemembercookie.getValue();
        map.put("isRemember", isremember);
        Cookie usernamecookie = RequestUtils.getCookie("userName");
        Cookie pwdcookie = RequestUtils.getCookie("userPassword");
        if (usernamecookie != null && pwdcookie != null) {
            map.put("cookiename", usernamecookie.getValue());
            map.put("cookiepassword", pwdcookie.getValue());
        }
        RequestUtils.removeAttribute("geneSymbol");
        RequestUtils.removeAttribute("geneItemList");
        if (user != null && user.getPageSize() != null) {
            map.put("pageSize", user.getPageSize());
        }
        return "index";
    }

    @RequestMapping("result.htm")
    public String result(ModelMap map, HttpServletRequest request) throws Throwable {
        int page = ServletRequestUtils.getIntParameter(request, "page", 1);
//        String geneSymbol = (String) request.getParameter("geneSymbol");
        String geneSymbol = "KLK3";
        Boolean isLink = ServletRequestUtils.getBooleanParameter(request, "isLink", false);
        Boolean isHistory = ServletRequestUtils.getBooleanParameter(request, "isHistory", false);
        Boolean isHistoryResult = ServletRequestUtils.getBooleanParameter(request, "isHistoryResult", false);
        String genome = request.getParameter("genome");
        if(!isLink){
        	RequestUtils.setAttribute("geneSymbol", geneSymbol);
        }
        String sort = (String) RequestUtils.getRequest().getParameter("sort");
        User user = (User) RequestUtils.getAttribute("user");
        if (user != null && user.getPageSize() != null) {
            pageSize = user.getPageSize();
        }
        SettingDTO settingDTO = new SettingDTO();
        String history_id = "";
        if (isHistory || isHistoryResult) {
            String keyword = (String) RequestUtils.getAttribute("geneSymbol");
            SearchHistory history = searchHistoryService.findByKeyword(keyword, user);
            history_id = history.get_id();
            constructSettinsData(settingDTO, null, history);
            String formateDate = DateUtils.format(history.getCreateDate(), "yyyy/MM/dd");
            map.put("date", formateDate);
        }else{
            constructSettinsData(settingDTO, user, null);
        }
        map.put("settingDTO", settingDTO);
        Map<String, Integer> sampleSumMap = SampleCache.getInstance().getSampleSumMap();
        map.put("sampleSumMap", sampleSumMap);
        // find Sample by geneSymbol or refSeq
        SortType sortType = StringUtils.isEmpty(sort) ? SortType.ASC : SortType.parse(sort);
        List<String> experimentsList = new ArrayList<String>();
        List<String> sourceList = new ArrayList<String>();
        for (String exp : settingDTO.getExperimentsMap().keySet()) {
            if (settingDTO.getExperimentsMap().get(exp)) {
                experimentsList.add(exp);
            }
        }
        for (String source : settingDTO.getSourcesMap().keySet()) {
            if (settingDTO.getSourcesMap().get(source)) {
                sourceList.add(source);
            }
        }
        SampleResult result = new SampleResult();
        //判断是否是点击搜索结果
        Integer start = (page-1)*pageSize;
        if (isHistoryResult) {
            result = searchHistoryService.searchSample(history_id, sourceList, experimentsList, sortType, start, pageSize);
        }else if (StringUtils.isNotBlank(geneSymbol)) {
            long begin = System.nanoTime();
            result = searchService.searchSample(genome, geneSymbol, sourceList, experimentsList, sortType, start, pageSize);
            result.setUsedTime(begin,System.nanoTime());
        }
        int totalRecords = result.getTotal();
        int totalPage = totalRecords % pageSize == 0 ? totalRecords / pageSize : (totalRecords / pageSize) + 1;
        if (page > totalPage) {
            result = new SampleResult();
        }
        Map<String, String> fdMap = factorDescService.findAll();
        Map<String, String> cdMap = cellTypeDescService.findAll();
        if(result.getSampleItemList() != null)
        {
        	for(SampleItem sampleItem : result.getSampleItemList())
        	{
        		String factor = sampleItem.getFactor();
        		if(factor == null || "".equals(factor))
        		{
        			continue;
        		}
        		String factor_desc = fdMap.get(factor);
        		if(factor_desc == null || "".equals(factor_desc))
        		{
        			factor_desc =  factorDescService.findByFactorRegex(factor);
        			if(factor_desc == null || "".equals(factor_desc))
        			{
        				factor_desc = factor;
        			}
        		}
        		sampleItem.setFactor_desc(factor_desc);
        	}
        	
        	for(SampleItem sampleItem : result.getSampleItemList())
        	{
        		String cell = sampleItem.getCell();
        		if(cell == null || "".equals(cell))
        		{
        			continue;
        		}
        		cell = cell.replace("-tumor", "").replace("-normal", "").replace("-control", "");
        		String cell_desc = cdMap.get(cell);
        		
        		if(cell_desc == null || "".equals(cell_desc))
        		{
        			cell_desc = cellTypeDescService.findByCellRegex(cell);
        			if(cell_desc == null || "".equals(cell_desc))
        			{
        				cell_desc = cell;
        			}
        		}
        		
        		String cell_x = sampleItem.getCell();
        		if(cell_x.contains("tumor"))
    			{
    				cell_desc += " tumor";
    			}
    			if(cell_x.contains("normal"))
    			{
    				cell_desc += " normal";
    			}
    			if(cell_x.contains("control"))
    			{
    				cell_desc += " control";
    			}
        		sampleItem.setCell_desc(cell_desc);
        	}
        }
        map.put("result", result);
        if(!isLink){
        	RequestUtils.setAttribute("geneItemList", result.getGeneItemList());
        	logger.debug("isLink: {}, find geneItemList size is: {}", isLink, result.getGeneItemList().size());
        }
        List<GeneItem> geneItemList = (List<GeneItem>) RequestUtils.getAttribute("geneItemList");
        if (geneItemList == null) {
            map.put("geneSize", 0);
        }else{
            map.put("geneSize", geneItemList.size());
        }
        int beginPage = page > 6 ? page - 5 : 1;
        int endPage = totalPage;
        if (totalPage > 10 && totalPage > page+5) {
            endPage = page > 6 ? page + 5 : 10;
        }
        if (isHistoryResult) {
            beginPage = 1;
            endPage = totalPage;
        }else{
            if (result.getCurrent() != null) {
                GeneRankCriteria criteria = new GeneRankCriteria();
                criteria.setGeneId(result.getCurrent().getGeneId());
                String ucscUrl = criteria.generateKey(GeneRankCriteria.ImageFileTempalte);
                map.put("ucscUrl", ucscUrl);
            }
        }
        map.put("totalPage", totalPage);
        map.put("page", page);
        map.put("beginPage", beginPage);
        map.put("endPage", endPage);
        map.put("geneSymbol", geneSymbol);
        map.put("totalRecords", totalRecords);
        map.put("sortType", sortType.name());
        Cookie isRemembercookie = RequestUtils.getCookie("isRemember");
        String isremember = isRemembercookie == null ? "false" : isRemembercookie.getValue();
        map.put("isRemember", isremember);
        Cookie usernamecookie = RequestUtils.getCookie("userName");
        Cookie pwdcookie = RequestUtils.getCookie("userPassword");
        if (usernamecookie != null && pwdcookie != null) {
            map.put("cookiename", usernamecookie.getValue());
            map.put("cookiepassword", pwdcookie.getValue());
        }
        map.put("isLink", isLink);
        map.put("isHistory", isHistory);
        map.put("isHistoryResult", isHistoryResult);
        map.put("pageSize", pageSize);
        if (logger.isDebugEnabled() && geneItemList != null) {
            logger.debug("at last: isLink: {}, find geneItemList size is: {}", isLink, geneItemList.size());
        }
        return "testpages/result";
    }
    
    @RequestMapping("result_pathway.htm")
    public String result_pathway(ModelMap map, HttpServletRequest request) throws Throwable 
    {
        int page = ServletRequestUtils.getIntParameter(request, "page", 1);
        String pathwayName = (String) request.getParameter("pathWayName");
        Integer pathwayId = null; //后期传递PathWayId
        Boolean isLink = ServletRequestUtils.getBooleanParameter(request, "isLink", false);
        Boolean isHistory = ServletRequestUtils.getBooleanParameter(request, "isHistory", false);
        
        if (!isLink) {
            RequestUtils.setAttribute("pathwayName", pathwayName);
        }
        String sort = (String) RequestUtils.getRequest().getParameter("sort");
        SortType sortType = StringUtils.isEmpty(sort) ? SortType.ASC : SortType.parse(sort);
        User user = (User) RequestUtils.getAttribute("user");
        if (user != null && user.getPageSize() != null) {
            pageSize = user.getPageSize();
        }
        SettingDTO settingDTO = new SettingDTO();
        String history_id = "";
        if (isHistory) {
//            String keyword = (String) RequestUtils.getAttribute("pathwayName");
//            SearchHistory history = searchHistoryService.findByKeyword(keyword);
//            history_id = history.get_id();
//            constructSettinsData(settingDTO, null, history);
//            String formateDate = DateUtils.format(history.getCreateDate(), "yyyy/MM/dd");
//            map.put("date", formateDate);
        }else{
            constructSettinsData(settingDTO, user, null);
        }
        map.put("settingDTO", settingDTO);
        Map<String, Integer> sampleSumMap = SampleCache.getInstance().getSampleSumMap();
        map.put("sampleSumMap", sampleSumMap);
        // find Sample by geneSymbol or refSeq
        //TODO find Sample by pathway_sample
        List<String> experimentsList = new ArrayList<String>();
        List<String> sourceList = new ArrayList<String>();
        for (String exp : settingDTO.getExperimentsMap().keySet()) {
            if (settingDTO.getExperimentsMap().get(exp)) {
                experimentsList.add(exp);
            }
        }
        for (String source : settingDTO.getSourcesMap().keySet()) {
            if (settingDTO.getSourcesMap().get(source)) {
                sourceList.add(source);
            }
        }
        SampleResult result = new SampleResult();
        //判断是否是点击搜索结果
        Integer start = (page-1)*pageSize;
        if (isHistory) {
//            result = searchHistoryService.searchSample(history_id, sourceList, experimentsList, sortType, start, pageSize);
        }else if (StringUtils.isNotBlank(pathwayName)) {
            long begin = System.nanoTime();
            result = searchService.searchSampleByPathway(pathwayName, pathwayId, sourceList, experimentsList, sortType, start, pageSize);
            result.setUsedTime(begin,System.nanoTime());
        }
        int totalRecords = result.getTotal();
        int totalPage = totalRecords % pageSize == 0 ? totalRecords / pageSize : (totalRecords / pageSize) + 1;
        if (page > totalPage) {
            result = new SampleResult();
        }
        Map<String, String> fdMap = factorDescService.findAll();
        Map<String, String> cdMap = cellTypeDescService.findAll();
        if(result.getSampleItemList() != null)
        {
        	for(SampleItem sampleItem : result.getSampleItemList())
        	{
        		String factor = sampleItem.getFactor();
        		if(factor == null || "".equals(factor))
        		{
        			continue;
        		}
        		String factor_desc = fdMap.get(factor);
        		if(factor_desc == null || "".equals(factor_desc))
        		{
        			factor_desc =  factorDescService.findByFactorRegex(factor);
        			if(factor_desc == null || "".equals(factor_desc))
        			{
        				factor_desc = factor;
        			}
        		}
        		sampleItem.setFactor_desc(factor_desc);
        	}
        	
        	for(SampleItem sampleItem : result.getSampleItemList())
        	{
        		String cell = sampleItem.getCell();
        		if(cell == null || "".equals(cell))
        		{
        			continue;
        		}
        		cell = cell.replace("-tumor", "").replace("-normal", "").replace("-control", "");
        		String cell_desc = cdMap.get(cell);
        		if(cell_desc == null || "".equals(cell_desc))
        		{
        			cell_desc = cellTypeDescService.findByCellRegex(cell);
        			if(cell_desc == null || "".equals(cell_desc))
        			{
        				cell_desc = cell;
        			}
        		}
        		String cell_x = sampleItem.getCell();
        		if(cell_x.contains("tumor"))
    			{
    				cell_desc += " tumor";
    			}
    			if(cell_x.contains("normal"))
    			{
    				cell_desc += " normal";
    			}
    			if(cell_x.contains("control"))
    			{
    				cell_desc += " control";
    			}
        		sampleItem.setCell_desc(cell_desc);
        	}
        }
        map.put("result", result);
        if (!isLink) {
            RequestUtils.setAttribute("geneItemList", result.getGeneItemList());
            logger.debug("isLink: {}, find geneItemList size is: {}", isLink, result.getGeneItemList().size());
        }
        List<GeneItem> geneItemList = (List<GeneItem>) RequestUtils.getAttribute("geneItemList");
        if (geneItemList == null) {
            map.put("geneSize", 0);
        }else{
            map.put("geneSize", geneItemList.size());
        }
        int beginPage = page > 6 ? page - 5 : 1;
        int endPage = totalPage;
        if (totalPage > 10) {
            endPage = page > 6 ? page + 5 : 10;
        }
        if (isHistory) {
            beginPage = 1;
            endPage = totalPage;
        }else{
            if (result.getCurrent() != null) {
                GeneRankCriteria criteria = new GeneRankCriteria();
                criteria.setGeneId(result.getCurrent().getGeneId());
                String ucscUrl = criteria.generateKey(GeneRankCriteria.ImageFileTempalte);
                map.put("ucscUrl", ucscUrl);
            }
        }
        map.put("pathwayUrl", result.getUrl());
        map.put("totalPage", totalPage);
        map.put("page", page);
        map.put("beginPage", beginPage);
        map.put("endPage", endPage);
        map.put("pathwayName", pathwayName);
        map.put("totalRecords", totalRecords);
        map.put("sortType", sortType.name());
        Cookie isRemembercookie = RequestUtils.getCookie("isRemember");
        String isremember = isRemembercookie == null ? "false" : isRemembercookie.getValue();
        map.put("isRemember", isremember);
        Cookie usernamecookie = RequestUtils.getCookie("userName");
        Cookie pwdcookie = RequestUtils.getCookie("userPassword");
        if (usernamecookie != null && pwdcookie != null) {
            map.put("cookiename", usernamecookie.getValue());
            map.put("cookiepassword", pwdcookie.getValue());
        }
        map.put("isLink", isLink);
        map.put("isHistory", isHistory);
//        map.put("isHistoryResult", isHistoryResult);
        map.put("pageSize", pageSize);
        if (logger.isDebugEnabled() && geneItemList != null) {
            logger.debug("at last: isLink: {}, find geneItemList size is: {}", isLink, geneItemList.size());
        }
    	return "result_pathway";
    }

    @RequestMapping("result_multigene.htm")
    public String result_multigene(ModelMap map, HttpServletRequest request) throws Throwable {
    	  int page = ServletRequestUtils.getIntParameter(request, "page", 1);
          String multigene = (String) request.getParameter("multigene");
          if(multigene!=null){
        	  multigene = multigene.substring(0,multigene.length()-1);
        	  String multigen ="";
              String [] genes = multigene.split(",");
              for(String s :genes){
            	  multigen += s+"\n";
              }
              map.put("multigene", multigen);
          }
          Boolean isLink = ServletRequestUtils.getBooleanParameter(request, "isLink", false);
          Boolean isHistory = ServletRequestUtils.getBooleanParameter(request, "isHistory", false);
          Boolean isHistoryResult = ServletRequestUtils.getBooleanParameter(request, "isHistoryResult", false);
          if (!isLink) {
              RequestUtils.setAttribute("multigene", multigene);
          }
          String sort = (String) RequestUtils.getRequest().getParameter("sort");
          User user = (User) RequestUtils.getAttribute("user");
          if (user != null && user.getPageSize() != null) {
              pageSize = user.getPageSize();
          }
          SettingDTO settingDTO = new SettingDTO();
          String history_id = "";
          if (isHistory || isHistoryResult) {
             // String keyword = (String) RequestUtils.getAttribute("geneSymbol");
              //SearchHistory history = searchHistoryService.findByKeyword(keyword);
              //history_id = history.get_id();
              //constructSettinsData(settingDTO, null, history);
             // String formateDate = DateUtils.format(history.getCreateDate(), "yyyy/MM/dd");
              //map.put("date", formateDate);
          }else{
              constructSettinsData(settingDTO, user, null);
          }
          map.put("settingDTO", settingDTO);
          Map<String, Integer> sampleSumMap = SampleCache.getInstance().getSampleSumMap();
          map.put("sampleSumMap", sampleSumMap);
          // find Sample by geneSymbol or refSeq
          SortType sortType = StringUtils.isEmpty(sort) ? SortType.ASC : SortType.parse(sort);
          List<String> experimentsList = new ArrayList<String>();
          List<String> sourceList = new ArrayList<String>();
          for (String exp : settingDTO.getExperimentsMap().keySet()) {
              if (settingDTO.getExperimentsMap().get(exp)) {
                  experimentsList.add(exp);
              }
          }
          for (String source : settingDTO.getSourcesMap().keySet()) {
              if (settingDTO.getSourcesMap().get(source)) {
                  sourceList.add(source);
              }
          }
          SampleResult result = new SampleResult();
          //判断是否是点击搜索结果
          Integer start = (page-1)*pageSize;
          if (isHistoryResult) {
              //result = searchHistoryService.searchSample(history_id, sourceList, experimentsList, sortType, start, pageSize);
          }else if (StringUtils.isNotBlank(multigene)) {
              long begin = System.nanoTime();
              //result = searchService.searchSample(geneSymbol, sourceList, experimentsList, sortType, start, pageSize);
              result = searchService.searchSampleByMultigene(multigene, sourceList, experimentsList, sortType, start, pageSize);
              result.setUsedTime(begin,System.nanoTime());
          }
          int totalRecords = result.getTotal();
          int totalPage = totalRecords % pageSize == 0 ? totalRecords / pageSize : (totalRecords / pageSize) + 1;
          if (page > totalPage) {
              result = new SampleResult();
          }
          Map<String, String> fdMap = factorDescService.findAll();
          Map<String, String> cdMap = cellTypeDescService.findAll();
          if(result.getSampleItemList() != null)
          {
          	for(SampleItem sampleItem : result.getSampleItemList())
          	{
          		String factor = sampleItem.getFactor();
          		if(factor == null || "".equals(factor))
          		{
          			continue;
          		}
          		String factor_desc = fdMap.get(factor);
          		if(factor_desc == null || "".equals(factor_desc))
          		{
          			factor_desc =  factorDescService.findByFactorRegex(factor);
          			if(factor_desc == null || "".equals(factor_desc))
          			{
          				factor_desc = factor;
          			}
          		}
          		sampleItem.setFactor_desc(factor_desc);
          	}
          	
          	for(SampleItem sampleItem : result.getSampleItemList())
          	{
          		String cell = sampleItem.getCell();
          		if(cell == null || "".equals(cell))
          		{
          			continue;
          		}
          		cell = cell.replace("-tumor", "").replace("-normal", "").replace("-control", "");
          		String cell_desc = cdMap.get(cell);
          		if(cell_desc == null || "".equals(cell_desc))
          		{
          			cell_desc = cellTypeDescService.findByCellRegex(cell);
          			if(cell_desc == null || "".equals(cell_desc))
          			{
          				cell_desc = cell;
          			}
          		}
          		String cell_x = sampleItem.getCell();
        		if(cell_x.contains("tumor"))
    			{
    				cell_desc += " tumor";
    			}
    			if(cell_x.contains("normal"))
    			{
    				cell_desc += " normal";
    			}
    			if(cell_x.contains("control"))
    			{
    				cell_desc += " control";
    			}
          		sampleItem.setCell_desc(cell_desc);
          	}
          }
          map.put("result", result);
          if (!isLink) {
              RequestUtils.setAttribute("geneItemList", result.getGeneItemList());
              logger.debug("isLink: {}, find geneItemList size is: {}", isLink, result.getGeneItemList().size());
          }
          List<GeneItem> geneItemList = (List<GeneItem>) RequestUtils.getAttribute("geneItemList");
          if (geneItemList == null) {
              map.put("geneSize", 0);
          }else{
              map.put("geneSize", geneItemList.size());
          }
          int beginPage = page > 6 ? page - 5 : 1;
          int endPage = totalPage;
          if (totalPage > 10) {
              endPage = page > 6 ? page + 5 : 10;
          }
          if (isHistoryResult) {
              beginPage = 1;
              endPage = totalPage;
          }else{
              if (result.getCurrent() != null) {
                  GeneRankCriteria criteria = new GeneRankCriteria();
                  criteria.setGeneId(result.getCurrent().getGeneId());
                  String ucscUrl = criteria.generateKey(GeneRankCriteria.ImageFileTempalte);
                  map.put("ucscUrl", ucscUrl);
              }
          }
          map.put("totalPage", totalPage);
          map.put("page", page);
          map.put("beginPage", beginPage);
          map.put("endPage", endPage);
         
          map.put("totalRecords", totalRecords);
          map.put("sortType", sortType.name());
          Cookie isRemembercookie = RequestUtils.getCookie("isRemember");
          String isremember = isRemembercookie == null ? "false" : isRemembercookie.getValue();
          map.put("isRemember", isremember);
          Cookie usernamecookie = RequestUtils.getCookie("userName");
          Cookie pwdcookie = RequestUtils.getCookie("userPassword");
          if (usernamecookie != null && pwdcookie != null) {
              map.put("cookiename", usernamecookie.getValue());
              map.put("cookiepassword", pwdcookie.getValue());
          }
          map.put("isLink", isLink);
          map.put("isHistory", isHistory);
          map.put("isHistoryResult", isHistoryResult);
          map.put("pageSize", pageSize);
          if (logger.isDebugEnabled() && geneItemList != null) {
              logger.debug("at last: isLink: {}, find geneItemList size is: {}", isLink, geneItemList.size());
          }
    	return "result_multigene";
    }
    
    @RequestMapping("autocomplete_multigene.json")
    @ResponseBody
    public Object autocomplete_multigene() {
    	String key = (String) RequestUtils.getRequest().getParameter("term");
        List<String> list = typeAheadService.search(key, "");
        return list.toArray();
    }
    
    @RequestMapping("autocomplete.json")
    @ResponseBody
    public Object autocomplete() {
        String key = (String) RequestUtils.getRequest().getParameter("term");
        List<String> list = typeAheadService.search(key, "");
        List<Map> result = new ArrayList<Map>();
        for(String key1 : list){
        	if(key1.startsWith("NM_")||key1.startsWith("NR_")){
        		String geneSymbol = TxrRefCache.getInstance().getGeneSymbolByRefSeq(key1);
        		Gene gene = GeneCache.getInstance().getGeneByName(key1);
				if(gene!=null){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("show", gene.getTxName());
					map.put("geneSymbol", geneSymbol);
					map.put("refseq", gene.getTxName());
					map.put("start", gene.getStart());
					map.put("end", gene.getEnd());
					map.put("strand", gene.getStrand());
					map.put("seqName", gene.getSeqName());
					result.add(map);
				}
        	}else{
	        	List<TxrRef> txrref = TxrRefCache.getInstance().getTxrRefBySymbol(key1.toLowerCase());
	        	for(TxrRef t :txrref){
	        		if(t.getRefseq() !=null && !"".equals(t.getRefseq())){
						Gene gene = GeneCache.getInstance().getGeneByName(t.getRefseq());
						if(gene!=null){
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("show", key1);
							map.put("geneSymbol", key1);
							map.put("refseq", gene.getTxName());
							map.put("start", gene.getStart());
							map.put("end", gene.getEnd());
							map.put("strand", gene.getStrand());
							map.put("seqName", gene.getSeqName());
							result.add(map);
							break;
						}
	        		}
	        	}
        	}
        }
        return result.toArray();
    }
    
    @RequestMapping("autocomplete_pathWay.json")
    @ResponseBody
    public Object autocompletePathWay() {
        String key = (String) RequestUtils.getRequest().getParameter("term");
        List<PathWay> list = pathWaySearchService.search(key);
        List<String> slist = new ArrayList<String>();
        for(PathWay pw : list)
        {
        	slist.add(pw.getPathwayName());
        }
        return slist.toArray();
    }
    
    @RequestMapping("chart.json")
    @ResponseBody
    public Object chart() {
    	String geneId = (String) RequestUtils.getRequest().getParameter("geneId");
    	JFreeChartUtils j = new JFreeChartUtils();
		try {
			String fileName = j.chart(Integer.parseInt(geneId));
			List<String> slist = new ArrayList<String>();
			slist.add(fileName);
			return slist.toArray();
		}  catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }

    
    private void constructSettinsData(SettingDTO settingDTO, User user, SearchHistory history) {
        for (ExperimentType type : ExperimentType.getUiMap().values()) {
            settingDTO.getExperimentsMap().put(type.getDesc(), false);
        }
        for (SourceType type : SourceType.getUiMap().values()) {
            settingDTO.getSourcesMap().put(type.desc(), false);
        }
        String experiments = "", source = "", cellType = "";
        
        String experimentsAll = "", sourceAll = "";;
        				
        Set exSet = settingDTO.getExperimentsMap().keySet();
    	for( Iterator   it = exSet.iterator();  it.hasNext(); ){
    		experimentsAll += ExperimentType.getType(it.next().toString()).value()+",";
    	}
    	experimentsAll = experimentsAll.substring(0, experimentsAll.length()-1);
    	
    	Set sourceSet = settingDTO.getSourcesMap().keySet();
    	for( Iterator   it = sourceSet.iterator();  it.hasNext(); ){
    		sourceAll += SourceType.getType(it.next().toString()).value()+",";
    	}
    	sourceAll = sourceAll.substring(0, sourceAll.length()-1);
    	
        if (user != null) {
            logger.debug("set SettingDTO by user.");
            experiments = user.getExperiments();
            if("".equals(experiments) || experiments==null){
            	experiments = experimentsAll;
            }
            source = user.getSource();
            if("".equals(source) || source == null){
            	source = sourceAll;
            }
            cellType = user.getCellType();
        }else if(history != null) {
            logger.debug("set SettingDTO by history.");
            experiments = history.getExperiments();
            if("".equals(experiments) || experiments==null){
            	experiments = experimentsAll;
            }
            source = history.getSource();
            if("".equals(source) || source == null){
            	source = sourceAll;
            }
            cellType = history.getCellType();
        }else{
            logger.debug("set SettingDTO by cookie.");
            Cookie experimentsCookie = RequestUtils.getCookie("experiments");
            if (experimentsCookie != null) {
                experiments = experimentsCookie.getValue();
                if("".equals(experiments) || experiments==null){
                	experiments = experimentsAll;
                }  
            }else{
            	experiments = experimentsAll;
            }
            Cookie sourceCookie = RequestUtils.getCookie("source");
            if (sourceCookie != null) {
                source = sourceCookie.getValue();
                if("".equals(source) || source == null){
                	source = sourceAll;
                }
            }else{
            	source = sourceAll;
            }
            Cookie cellTypeCookie = RequestUtils.getCookie("cellType");
            if (cellTypeCookie != null) {
                cellType = cellTypeCookie.getValue();
            }
        }
        if (StringUtils.isNotBlank(experiments)) {
            String[] itemsArray = experiments.split(",");
            for (String value : itemsArray) {
                ExperimentType type = ExperimentType.parse(Integer.valueOf(value));
                settingDTO.getExperimentsMap().put(type.getDesc(), true);
            }
        }
        if (StringUtils.isNotBlank(source)) {
            String[] sourcesArray = source.split(",");
            for (String value : sourcesArray) {
                SourceType type = SourceType.parse(Integer.valueOf(value));
                settingDTO.getSourcesMap().put(type.desc(), true);
            }
        }
        settingDTO.setCellType(cellType);
    }
    
    @RequestMapping("about.htm")
    public String about(ModelMap map) {
        return "testpages/about";
    }
    
    @RequestMapping("factorMapping.json")
    @ResponseBody
    public Object factorMapping() {
        List<String> list = searchService.findFactorMapingNotExist();
        return list.toArray();
    }
    
    @RequestMapping("waitForUcsc.htm")
    public String waitForUcsc(ModelMap map,HttpServletRequest request) {
    	String ucscUrl = request.getParameter("url");
    	ucscUrl = ucscUrl.replaceAll("\\$", "&");
    	map.put("ucscUrl",ucscUrl);
        return "waitForUcsc";
    }
    
    @RequestMapping("result_genomicregion")
    public String result_genomicregion(ModelMap map,HttpServletRequest request) {
    	String chr = request.getParameter("chr");
    	int start = Integer.parseInt(request.getParameter("start"));
    	int end = Integer.parseInt(request.getParameter("end"));
        List<Gene> geneList = new ArrayList<Gene>();
		DBCollection collection = MongoDBManager.getInstance().getCollection("manage", "mange", "gene");
		SmartDBObject query = new SmartDBObject();
		query.put("seqName", chr);
		DBCursor cursor = collection.find(query);
		System.out.println(cursor.getSizes());
    	//Gene genes = GeneCache.getInstance().get("seqName", "chr19");
    	/*for(Gene gene :genes){
    		if(gene.getStart()<= start &&gene.getEnd()>=end){
    			geneList.add(gene);
    			break;
    		}else if(gene.getStart()>=start && gene.getStart()<=end && gene.getEnd()>=end){
    			geneList.add(gene);
    			break;
    		}else if(gene.getStart()<=start && gene.getEnd()>=start &&gene.getEnd()<=end){
    			geneList.add(gene);
    			break;
    		}else if(gene.getStart()>=start && gene.getEnd()<end){
    			geneList.add(gene);
    			break;
    		}
    	}
    	System.out.println(geneList.size());*/
    	return "result_genomicregion";
    }
    @RequestMapping("comment.htm")
    public String showComment(ModelMap map, HttpServletRequest request){
    	String sampleId = request.getParameter("sampleId");
    	ICommentDAO commentDAO = DAOFactory.getDAO(ICommentDAO.class);
    	List<Comment> comments = commentDAO.findBySampleId(Integer.parseInt(sampleId));
    	map.put("sampleId", sampleId);
    	map.put("comments", comments);
    	User user = (User) RequestUtils.getAttribute("user");
    	map.put("user", user);
    	return "comment";
    }
    @RequestMapping("submitComment.htm")
    public String submitComment(ModelMap map, HttpServletRequest request){
    	String sampleId = request.getParameter("sampleId") ;
    	ICommentDAO commentDAO = DAOFactory.getDAO(ICommentDAO.class);
    	User user = (User) RequestUtils.getAttribute("user");
    	int userId = user==null?0:user.getUserId();
    	int sid = Integer.parseInt(sampleId);
    	Comment comment = new Comment();
    	comment.setCommentId(commentDAO.getSequenceId("CCLEComment"));
    	comment.setContent(request.getParameter("content"));
    	comment.setUserId(userId);
    	comment.setUserName(user.getName());
    	comment.setSampleId(sid);
    	commentDAO.create(comment);
    	return "comment";
    }
    @RequestMapping("dataDownload.htm")
    public String dataDownload(ModelMap map, HttpServletRequest request ) {
        String [] column = {"Rank","DataSetID","DataType", "Cell","Factor","Order/Total","Percentile(%)","tssTesCount","tss5KCount","Study","Lab"} ;
        SettingDTO settingDTO = new SettingDTO();
        constructSettinsData(settingDTO, null, null);
        map.put("settingDTO", settingDTO);
        map.put("columns", column);
    	return "dataDownload";
    }
    @RequestMapping("download.htm")
    public void download(ModelMap map, HttpServletRequest request ,HttpServletResponse res) throws IOException{
    	String experiment = request.getParameter("experiments");
    	StringBuilder sb = new StringBuilder();
        if (StringUtils.isNoneEmpty(experiment)) {
            String[] expArray = experiment.split(",");
            for (String exp : expArray) {
                sb.append(ExperimentType.getType(exp).getValue()).append(",");
            }
            sb.delete(sb.length() - 1, sb.length());
        }
        String experiments = sb.toString();
        String source = request.getParameter("sources");
        StringBuilder sb1 = new StringBuilder();
        if (StringUtils.isNoneEmpty(source)) {
            String[] sourceArray = source.split(",");
            for (String sour : sourceArray) {
                sb1.append(SourceType.getType(sour).getValue()).append(",");
            }
            sb1.delete(sb1.length() - 1, sb1.length());
        } 
        String sources = sb1.toString();
        String cell = request.getParameter("cell") ==null?"":request.getParameter("cell");
        String factor = request.getParameter("factor")==null?"":request.getParameter("factor");
        String geneSymbols = request.getParameter("geneSymbols");
        if(geneSymbols!=null && geneSymbols.endsWith(",")){
			geneSymbols = geneSymbols.substring(0, geneSymbols.length()-1);
		}
        if(cell!=null && cell.endsWith(",")){
        	cell = cell.substring(0, cell.length()-1);
		}
        if(factor!=null && factor.endsWith(",")){
        	factor = factor.substring(0, factor.length()-1);
		}
        String columns = request.getParameter("columns");
        String [] titles = {"Rank","DataSetID","DataType", "Cell","Factor","Order/Total","Percentile(%)","tssTesCount","tss5KCount","Study","Lab"} ;

        if(columns != null){
        	titles = columns.split(",");
        }
        XSSFWorkbook wb = null ;
        if(StringUtils.isNotEmpty(geneSymbols) && !geneSymbols.equals(",")){
        	 wb = GeneRankExport.getInstance().buildWorkbookByDownload(geneSymbols, sources, experiments, cell, factor, titles);
        
        // 定义文件名
        res.setHeader("Content-Disposition", "attachment;filename=\"" + geneSymbols + ".csv\"");
        // 设置打开方式
        res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        // 设置编码
        res.setCharacterEncoding(Charsets.UTF_8.displayName());
        ServletOutputStream out = res.getOutputStream();
        wb.write(out);
        out.flush();
        out.close();
        }
    }
}
