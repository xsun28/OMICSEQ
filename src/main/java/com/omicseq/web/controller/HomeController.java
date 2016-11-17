package com.omicseq.web.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.omicseq.bean.GeneItem;
import com.omicseq.bean.SampleItem;
import com.omicseq.bean.SampleResult;
import com.omicseq.common.Charsets;
import com.omicseq.common.ExperimentType;
import com.omicseq.common.SortType;
import com.omicseq.common.SourceType;
import com.omicseq.core.GeneCache;
import com.omicseq.core.MiRNASampleCache;
import com.omicseq.core.MouseGeneCache;
import com.omicseq.core.MouseTxrRefCache;
import com.omicseq.core.SampleCache;
import com.omicseq.core.TxrRefCache;
import com.omicseq.core.batch.GeneRankExport;
import com.omicseq.domain.Comment;
import com.omicseq.domain.Gene;
import com.omicseq.domain.MiRNA;
import com.omicseq.domain.MiRNASample;
import com.omicseq.domain.Sample;
import com.omicseq.domain.SearchHistory;
import com.omicseq.domain.TxrRef;
import com.omicseq.domain.User;
import com.omicseq.pathway.PathWay;
import com.omicseq.store.criteria.GeneRankCriteria;
import com.omicseq.store.dao.ICommentDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateUtils;
import com.omicseq.utils.JFreeChartUtils;
import com.omicseq.web.dto.SettingDTO;
import com.omicseq.web.service.ICellTypeDescService;
import com.omicseq.web.service.IFactorDescService;
import com.omicseq.web.service.IGeneRankSearchService;
import com.omicseq.web.service.IMouseSampleSearchService;
import com.omicseq.web.service.IPathWaySearchService;
import com.omicseq.web.service.ISampleSearchService;
import com.omicseq.web.service.ISearchHistoryService;
import com.omicseq.web.service.ISearchMiRNAService;
import com.omicseq.web.service.ITypeAheadService;
import com.omicseq.web.service.IUserService;
import com.omicseq.web.service.IVariationGeneService;
import com.omicseq.web.serviceimpl.MessageSourceHelper;
import com.omicseq.web.serviceimpl.SampleSearchServiceHelper;
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
@RequestMapping("/")
public class HomeController extends BaseController {
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
	private ISearchMiRNAService miRNAService;
	@Autowired
	private IFactorDescService factorDescService;

	@Autowired
	private ICellTypeDescService cellTypeDescService;
	@Autowired
	private ISearchMiRNAService searchMiRNAService;
	@Autowired
	private IMouseSampleSearchService mouseSearchService;
	@Autowired
    private SampleSearchServiceHelper sampleSearchServiceHelper;
	
	@Autowired
	private IGeneRankSearchService geneRankSearchService;
	@Autowired
	private IVariationGeneService variationSearchService;

	@Autowired
	private IUserService userService;
	
	@RequestMapping("index.html")
	public String toIndex(ModelMap map) {
		//return "toIndex";
		return "redirect:/welcome.htm";
	}

	@RequestMapping("confirm.json")
	@ResponseBody
	public Object confirm() {
		RequestUtils.getSession().setAttribute("isValid", true);
		return JsonSuccess(true);
	}

	@RequestMapping("welcome.htm")
	public String welcome(ModelMap map, HttpServletResponse response)
			throws IOException, ServletException {
		User adminuser = userService.login("emorybiostat", "emoryemory");
//		adminuser.set_id("8");
//		adminuser.setName("emorybiostat");
//		adminuser.setPassword("emoryemory");
//		adminuser.setDeleted(false);
//		adminuser.setExperiments("0,1,17,18,2,4,6,8,9,10,11,12,13,15,16,19");
//		adminuser.setSource("11,10,12,16,5,15,6,1,3,7,4,8,17,9,14,13,2");
//		adminuser.setPageSize(50);
//		adminuser.setSource_mouse("14,2");
//		adminuser.setExperiments_mouse("1,17,2,19");
//		adminuser.setUserType(0);
		RequestUtils.setAttribute("user", adminuser);
	User user = (User) RequestUtils.getAttribute("user");
		
		if(user == null){
			return "redirect:/index.html";
		}
		if(user.getUserType() != null) {
			if(user.getUserType() == 2){
				return "redirect:/result_single.htm";
			}
		}
		
		SettingDTO settingDTO = new SettingDTO();
		constructSettinsData(settingDTO, user, null);
		map.put("settingDTO", settingDTO);
		RequestUtils.setCookie("userName", user.getName(), response);
		RequestUtils.setCookie("userPassword", user.getPassword(), response);
		Cookie usernamecookie = RequestUtils.getCookie("userName");
		Cookie pwdcookie = RequestUtils.getCookie("userPassword");

		RequestUtils.setCookie("jforumSSOCookieNameUser", "emorybiostat,emoryemory,0", response);
		if (usernamecookie != null && pwdcookie != null) {
			map.put("cookiename", usernamecookie.getValue());
			map.put("cookiepassword", pwdcookie.getValue());
		}
		if (user != null && user.getPageSize() != null) {
			map.put("pageSize", user.getPageSize());
		}
		String[] column = { "Rank", "DataSetID", "DataType", "Cell", "Factor",
				"Order/Total", "Percentile(%)", "tssTesCount", "tss5KCount",
				"Study", "Lab" };
		map.put("columns", column);
		
		return "welcome";
	}

	@RequestMapping("index.htm")
	public String index(ModelMap map, HttpServletResponse response)
			throws IOException, ServletException {
		// 判断是否已通过拦截页面
		boolean isValid = false;
		if (RequestUtils.getSession() != null) {
			Object temp = RequestUtils.getSession().getAttribute("isValid");
			isValid = temp == null ? false : (Boolean) temp;
		}
		if (!isValid) {
			return "redirect:/index.html";
		}
		User user = (User) RequestUtils.getAttribute("user");
		logger.debug("user: {} ",user);
		SettingDTO settingDTO = new SettingDTO();
		constructSettinsData(settingDTO, user, null);
		map.put("settingDTO", settingDTO);
		Map<String, Integer> sampleSumMap = SampleCache.getInstance()
				.getSampleSumMap();
		map.put("sampleSumMap", sampleSumMap);
		Cookie isRemembercookie = RequestUtils.getCookie("isRemember");
		String isremember = isRemembercookie == null ? "false"
				: isRemembercookie.getValue();
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
	public String result(ModelMap map, HttpServletRequest request)
			throws Throwable {
		int page = ServletRequestUtils.getIntParameter(request, "page", 1);
		String geneSymbol = (String) request.getParameter("geneSymbol");
		Boolean isLink = ServletRequestUtils.getBooleanParameter(request,
				"isLink", false);
		Boolean isHistory = ServletRequestUtils.getBooleanParameter(request,
				"isHistory", false);
		Boolean isHistoryResult = ServletRequestUtils.getBooleanParameter(
				request, "isHistoryResult", false);
		String genome = (String) request.getParameter("genome") == null ? "Human" : (String) request.getParameter("genome"); 
		map.put("genome", genome);
		if (!isLink) {
			RequestUtils.setAttribute("geneSymbol", geneSymbol);
		}
		String sort = (String) RequestUtils.getRequest().getParameter("sort");
		User user = (User) RequestUtils.getAttribute("user");
		if(user == null){
			return "redirect:/index.html";
		}
		if (user.getPageSize() != null) {
			pageSize = user.getPageSize();
		}
		
		if(user.getUserType() != null)
		{
			if(user.getUserType() > 1){
				pageSize = 20;
				page = 1;
			}
		}
		
		SettingDTO settingDTO = new SettingDTO();
		String history_id = "";
		if (isHistory || isHistoryResult) {
			String keyword = (String) RequestUtils.getAttribute("geneSymbol");
			SearchHistory history = searchHistoryService.findByKeyword(keyword,
					user);
			history_id = history.get_id();
			constructSettinsData(settingDTO, null, history);
			String formateDate = DateUtils.format(history.getCreateDate(),
					"yyyy/MM/dd");
			map.put("date", formateDate);
		} else {
			constructSettinsData(settingDTO, user, null);
		}
		map.put("settingDTO", settingDTO);
		Map<String, Integer> sampleSumMap = SampleCache.getInstance()
				.getSampleSumMap();
		map.put("sampleSumMap", sampleSumMap);
		Map<String, Integer> mouseSampleSumMap = SampleCache.getInstance().getMouseSampleSumMap();
		map.put("mouseSampleSumMap", mouseSampleSumMap);
		// find Sample by geneSymbol or refSeq
		SortType sortType = StringUtils.isEmpty(sort) ? SortType.ASC : SortType
				.parse(sort);
		List<String> experimentsList = new ArrayList<String>();
		List<String> sourceList = new ArrayList<String>();
		if(genome.equals("Human")){
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
		}
		else if(genome.equals("Mouse")){
			for (String exp : settingDTO.getExperimentsMap_mouse().keySet()) {
				if (settingDTO.getExperimentsMap_mouse().get(exp)) {
					experimentsList.add(exp);
				}
			}
			for (String source : settingDTO.getSourcesMap_mouse().keySet()) {
				if (settingDTO.getSourcesMap_mouse().get(source)) {
					sourceList.add(source);
				}
			}
		}
		SampleResult result = new SampleResult();
		// 判断是否是点击搜索结果
		Integer start = (page - 1) * pageSize;
		if (isHistoryResult) {
			result = searchHistoryService.searchSample(history_id, sourceList,
					experimentsList, sortType, start, pageSize);
		} else if (StringUtils.isNotBlank(geneSymbol)) {
			long begin = System.nanoTime();
			if(genome.equals("Human")){
				result = searchService.searchSample(geneSymbol, sourceList,
						experimentsList, sortType, start, pageSize);
			}
			else if(genome.equals("Mouse")){
				result = mouseSearchService.searchSample(geneSymbol, sourceList,
						experimentsList, sortType, start, pageSize);
			}
			result.setUsedTime(begin, System.nanoTime());
		}
		int totalRecords = result.getTotal();
		int totalPage = totalRecords % pageSize == 0 ? totalRecords / pageSize
				: (totalRecords / pageSize) + 1;
		if (page > totalPage) {
			int total_all = result.getTotal_all();
//			result = new SampleResult();
			result.setTotal_all(total_all);
		}
		Map<String, String> fdMap = factorDescService.findAll();
		Map<String, String> cdMap = cellTypeDescService.findAll();
		if (result.getSampleItemList() != null) {
			for (SampleItem sampleItem : result.getSampleItemList()) {
				String factor = sampleItem.getFactor();
				String factor_desc = fdMap.get(factor);
		
				if ((factor_desc == null || "".equals(factor_desc)) && factor != null) {
					factor_desc = factorDescService.findByFactorRegex(factor);
				}
				
				if (factor_desc == null || "".equals(factor_desc)) {
					factor_desc = sampleItem.getDetail();
				}
				
				sampleItem.setFactor_desc(factor_desc);
			}

			for (SampleItem sampleItem : result.getSampleItemList()) {
				String cell = sampleItem.getCell();
				if (cell == null || "".equals(cell) || "Summary Track".equals(sampleItem.getDataType()) || "Somatic Mutations".equals(sampleItem.getDataType())) {
					sampleItem.setCell_desc(cell);
					continue;
				}
				cell = cell.replace("-tumor", "").replace("-normal", "")
						.replace("-control", "");
				String cell_desc = cdMap.get(cell);

				if (cell_desc == null || "".equals(cell_desc)) {
					cell_desc = cellTypeDescService.findByCellRegex(cell);
					if (cell_desc == null || "".equals(cell_desc)) {
						cell_desc = cell;
					}
				}

				String cell_x = sampleItem.getCell();
				if (cell_x.contains("tumor")) {
					cell_desc += " tumor";
				}
				if (cell_x.contains("normal")) {
					cell_desc += " normal";
				}
				if (cell_x.contains("control")) {
					cell_desc += " control";
				}
				sampleItem.setCell_desc(cell_desc);
			}
		}
		map.put("result", result);
		if (!isLink) {
			RequestUtils.setAttribute("geneItemList", result.getGeneItemList());
			logger.debug("isLink: {}, find geneItemList size is: {}", isLink,
					result.getGeneItemList().size());
		}
		List<String> relKeyList = new ArrayList<String>();
		if (result.getCurrent() != null) {
			String relKey = result.getCurrent().getRelKey();
			if(StringUtils.isNotBlank(relKey))
			{
				String[] keys = relKey.split(",");
				for(int i=0; i<keys.length; i++)
				{
					relKeyList.add(keys[i]);
				}
			}
		}
		map.put("relKeyList", relKeyList);
		@SuppressWarnings("unchecked")
		List<GeneItem> geneItemList = (List<GeneItem>) RequestUtils.getAttribute("geneItemList");
		if (geneItemList == null) {
			map.put("geneSize", 0);
		} else {
			map.put("geneSize", geneItemList.size());
		}
		int beginPage = page > 6 ? page - 5 : 1;
		int endPage = totalPage;
		if (totalPage > 10 && totalPage > page + 5) {
			endPage = page > 6 ? page + 5 : 10;
		}
		if (isHistoryResult) {
			beginPage = 1;
			endPage = totalPage;
		} else {
			if (result.getCurrent() != null) {
				GeneRankCriteria criteria = new GeneRankCriteria();
				criteria.setGeneId(result.getCurrent().getGeneId());
				String ucscUrl = criteria
						.generateKey(GeneRankCriteria.ImageFileTempalte);
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
		String isremember = isRemembercookie == null ? "false"
				: isRemembercookie.getValue();
		map.put("isRemember", isremember);
		Cookie usernamecookie = RequestUtils.getCookie("userName");
		Cookie pwdcookie = RequestUtils.getCookie("userPassword");
		if (usernamecookie != null && pwdcookie != null) {
			map.put("cookiename", usernamecookie.getValue());
			map.put("cookiepassword", pwdcookie.getValue());
		}
		List<TxrRef> trList = TxrRefCache.getInstance().getTxrRefBySymbol(geneSymbol);
		if(CollectionUtils.isNotEmpty(trList)){
			TxrRef tr = trList.get(0);
			if(tr.getAlias()!=null){
				map.put("alias", tr.getAlias());
			}
		}
		
		map.put("isLink", isLink);
		map.put("isHistory", isHistory);
		map.put("isHistoryResult", isHistoryResult);
		map.put("pageSize", pageSize);
		if (logger.isDebugEnabled() && geneItemList != null) {
			logger.debug("at last: isLink: {}, find geneItemList size is: {}",
					isLink, geneItemList.size());
		}
		Integer [] counts = searchService.findProcessedAndInProcessSample("gene");
		map.put("processed_human",counts[0]);
		map.put("inProcess_human", counts[1]);
		map.put("processed_mouse",counts[2]);
		map.put("inProcess_mouse", counts[3]);
		return "result";
	}


	@RequestMapping("result_datasetSearch.htm")
	public Object result_datasetSearch(ModelMap map, HttpServletRequest request)
			throws Throwable {
		int page = ServletRequestUtils.getIntParameter(request, "page", 1);
		String cell = (String) request.getParameter("cell");
		String factor = (String) request.getParameter("factor");
		Boolean isLink = ServletRequestUtils.getBooleanParameter(request,
				"isLink", false);
		Boolean isHistory = ServletRequestUtils.getBooleanParameter(request,
				"isHistory", false);
		Boolean isHistoryResult = ServletRequestUtils.getBooleanParameter(
				request, "isHistoryResult", false);

		if (!isLink) {
			RequestUtils.setAttribute("cell", cell);
			RequestUtils.setAttribute("factor", factor);
		}
		String sort = (String) RequestUtils.getRequest().getParameter("sort");
		User user = (User) RequestUtils.getAttribute("user");
		if(user == null){
			return "redirect:/index.html";
		}
		if(user.getUserType() > 1){
			return "redirect:/result.htm";
		}
		if (user.getPageSize() != null) {
			pageSize = user.getPageSize();
		}
		SettingDTO settingDTO = new SettingDTO();
		if (isHistory || isHistoryResult) {
//			String keyword = (String) RequestUtils.getAttribute("geneSymbol");
//			SearchHistory history = searchHistoryService.findByKeyword(keyword,
//					user);
//			history_id = history.get_id();
//			constructSettinsData(settingDTO, null, history);
//			String formateDate = DateUtils.format(history.getCreateDate(),
//					"yyyy/MM/dd");
//			map.put("date", formateDate);
		} else {
			constructSettinsData(settingDTO, user, null);
		}
		
		map.put("settingDTO", settingDTO);
		Map<String, Integer> sampleSumMap = SampleCache.getInstance()
				.getSampleSumMap();
		map.put("sampleSumMap", sampleSumMap);
		// find Sample by geneSymbol or refSeq
		SortType sortType = StringUtils.isEmpty(sort) ? SortType.ASC : SortType
				.parse(sort);
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
		// 判断是否是点击搜索结果
		Integer start = (page - 1) * pageSize;
		if (isHistoryResult) {
//			result = searchHistoryService.searchSample(history_id, sourceList,
//					experimentsList, sortType, start, pageSize);
		} else if (StringUtils.isNotBlank(cell)||StringUtils.isNotBlank(factor)) {
			long begin = System.nanoTime();
			result = searchService.searchSample(cell,factor, sourceList,
					experimentsList, sortType, start, pageSize);
			result.setUsedTime(begin, System.nanoTime());
		}
		int totalRecords = result.getTotal();
		int totalPage = totalRecords % pageSize == 0 ? totalRecords / pageSize
				: (totalRecords / pageSize) + 1;
		if (page > totalPage) {
			int total_all = result.getTotal_all();
			result = new SampleResult();
			result.setTotal_all(total_all);
		}
		Map<String, String> fdMap = factorDescService.findAll();
		Map<String, String> cdMap = cellTypeDescService.findAll();
		if (result.getSampleItemList() != null) {
			for (SampleItem sampleItem : result.getSampleItemList()) {
				String factor_db = sampleItem.getFactor();
				if (factor_db == null || "".equals(factor_db)) {
					sampleItem.setFactor_desc(sampleItem.getDetail());
					continue;
				}
				String factor_desc = fdMap.get(factor_db);
				if (factor_desc == null || "".equals(factor_desc)) {
					factor_desc = factorDescService.findByFactorRegex(factor_db);
					if (factor_desc == null || "".equals(factor_desc)) {
						factor_desc = sampleItem.getDetail();
					}
				}
				sampleItem.setFactor_desc(factor_desc);
			}

			for (SampleItem sampleItem : result.getSampleItemList()) {
				String cell_db = sampleItem.getCell();
				if (cell_db == null || "".equals(cell_db) || "Summary Track".equals(sampleItem.getDataType()) || "Somatic Mutations".equals(sampleItem.getDataType())) {
					sampleItem.setCell_desc(cell_db);
					continue;
				}
				cell_db = cell_db.replace("-tumor", "").replace("-normal", "")
						.replace("-control", "");
				String cell_desc = cdMap.get(cell_db);

				if (cell_desc == null || "".equals(cell_desc)) {
					cell_desc = cellTypeDescService.findByCellRegex(cell_db);
					if (cell_desc == null || "".equals(cell_desc)) {
						cell_desc = cell_db;
					}
				}

				String cell_x = sampleItem.getCell();
				if (cell_x.contains("tumor")) {
					cell_desc += " tumor";
				}
				if (cell_x.contains("normal")) {
					cell_desc += " normal";
				}
				if (cell_x.contains("control")) {
					cell_desc += " control";
				}
				sampleItem.setCell_desc(cell_desc);
			}
		}
		map.put("result", result);
		if (!isLink) {
			RequestUtils.setAttribute("geneItemList", result.getGeneItemList());
			logger.debug("isLink: {}, find geneItemList size is: {}", isLink,
					result.getGeneItemList().size());
		}
		@SuppressWarnings("unchecked")
		List<GeneItem> geneItemList = (List<GeneItem>) RequestUtils.getAttribute("geneItemList");
		if (geneItemList == null) {
			map.put("geneSize", 0);
		} else {
			map.put("geneSize", geneItemList.size());
		}
		int beginPage = page > 6 ? page - 5 : 1;
		int endPage = totalPage;
		if (totalPage > 10 && totalPage > page + 5) {
			endPage = page > 6 ? page + 5 : 10;
		}
		if (isHistoryResult) {
			beginPage = 1;
			endPage = totalPage;
		} else {
			if (result.getCurrent() != null) {
				GeneRankCriteria criteria = new GeneRankCriteria();
				criteria.setGeneId(result.getCurrent().getGeneId());
				String ucscUrl = criteria
						.generateKey(GeneRankCriteria.ImageFileTempalte);
				map.put("ucscUrl", ucscUrl);
			}
		}
		map.put("totalPage", totalPage);
		map.put("page", page);
		map.put("beginPage", beginPage);
		map.put("endPage", endPage);
		map.put("cell", cell);
		map.put("factor", factor);
		map.put("totalRecords", totalRecords);
		map.put("sortType", sortType.name());
		Cookie isRemembercookie = RequestUtils.getCookie("isRemember");
		String isremember = isRemembercookie == null ? "false"
				: isRemembercookie.getValue();
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
			logger.debug("at last: isLink: {}, find geneItemList size is: {}",
					isLink, geneItemList.size());
		}
		
		List<Integer> intSourceList = sampleSearchServiceHelper.toSourceTypies(sourceList);
        List<Integer> intEtypeList = sampleSearchServiceHelper.toEtypies(experimentsList);
		List<Sample> sampleList = SampleCache.getInstance().getSampleByCellAndDetail(cell, factor, intSourceList, intEtypeList);
		if(sampleList != null && sampleList.size() > 0)
		{
			JFreeChartUtils j = new JFreeChartUtils();
			try {
				String fileName = j.chartByList(sampleList);
				map.put("charFileName", fileName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Integer [] counts = searchService.findProcessedAndInProcessSample("datasetSearch");
		map.put("processed",counts[0]);
		map.put("inProcess", counts[1]);
		return "result_datasetSearch";
	}
	@RequestMapping("result_pathway.htm")
	public String result_pathway(ModelMap map, HttpServletRequest request)
			throws Throwable {
		int page = ServletRequestUtils.getIntParameter(request, "page", 1);
		String pathwayName = (String) request.getParameter("pathWayName");
		Integer pathwayId = null; // 后期传递PathWayId
		Boolean isLink = ServletRequestUtils.getBooleanParameter(request,
				"isLink", false);
		Boolean isHistory = ServletRequestUtils.getBooleanParameter(request,
				"isHistory", false);

		if (!isLink) {
			RequestUtils.setAttribute("pathwayName", pathwayName);
		}
		String sort = (String) RequestUtils.getRequest().getParameter("sort");
		SortType sortType = StringUtils.isEmpty(sort) ? SortType.ASC : SortType
				.parse(sort);
		User user = (User) RequestUtils.getAttribute("user");
		if(user == null){
			return "redirect:/index.html";
		}
		if(user.getUserType() > 1){
			return "redirect:/result.htm";
		}
		if (user.getPageSize() != null) {
			pageSize = user.getPageSize();
		}
		SettingDTO settingDTO = new SettingDTO();
		if (isHistory) {
			// String keyword = (String)
			// RequestUtils.getAttribute("pathwayName");
			// SearchHistory history =
			// searchHistoryService.findByKeyword(keyword);
			// history_id = history.get_id();
			// constructSettinsData(settingDTO, null, history);
			// String formateDate = DateUtils.format(history.getCreateDate(),
			// "yyyy/MM/dd");
			// map.put("date", formateDate);
		} else {
			constructSettinsData(settingDTO, user, null);
		}
		map.put("settingDTO", settingDTO);
		Map<String, Integer> sampleSumMap = SampleCache.getInstance()
				.getSampleSumMap();
		map.put("sampleSumMap", sampleSumMap);
		// find Sample by geneSymbol or refSeq
		// find Sample by pathway_sample
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
		// 判断是否是点击搜索结果
		Integer start = (page - 1) * pageSize;
		if (isHistory) {
			// result = searchHistoryService.searchSample(history_id,
			// sourceList, experimentsList, sortType, start, pageSize);
		} else if (StringUtils.isNotBlank(pathwayName)) {
			long begin = System.nanoTime();
			result = searchService.searchSampleByPathway(pathwayName,
					pathwayId, sourceList, experimentsList, sortType, start,
					pageSize);
			result.setUsedTime(begin, System.nanoTime());
		}
		int totalRecords = result.getTotal();
		int totalPage = totalRecords % pageSize == 0 ? totalRecords / pageSize
				: (totalRecords / pageSize) + 1;
		if (page > totalPage) {
			result = new SampleResult();
		}
		Map<String, String> fdMap = factorDescService.findAll();
		Map<String, String> cdMap = cellTypeDescService.findAll();
		if (result.getSampleItemList() != null) {
			for (SampleItem sampleItem : result.getSampleItemList()) {
				String factor = sampleItem.getFactor();
				if (factor == null || "".equals(factor)) {
					sampleItem.setFactor_desc(sampleItem.getDetail());
					continue;
				}
				String factor_desc = fdMap.get(factor);
				if (factor_desc == null || "".equals(factor_desc)) {
					factor_desc = factorDescService.findByFactorRegex(factor);
					if (factor_desc == null || "".equals(factor_desc)) {
						factor_desc = sampleItem.getDetail();
					}
				}
				sampleItem.setFactor_desc(factor_desc);
			}

			for (SampleItem sampleItem : result.getSampleItemList()) {
				String cell = sampleItem.getCell();
				if (cell == null || "".equals(cell) || "Summary Track".equals(sampleItem.getDataType()) || "Somatic Mutations".equals(sampleItem.getDataType())) {
					sampleItem.setCell_desc(cell);
					continue;
				}
				cell = cell.replace("-tumor", "").replace("-normal", "")
						.replace("-control", "");
				String cell_desc = cdMap.get(cell);

				if (cell_desc == null || "".equals(cell_desc)) {
					cell_desc = cellTypeDescService.findByCellRegex(cell);
					if (cell_desc == null || "".equals(cell_desc)) {
						cell_desc = cell;
					}
				}

				String cell_x = sampleItem.getCell();
				if (cell_x.contains("tumor")) {
					cell_desc += " tumor";
				}
				if (cell_x.contains("normal")) {
					cell_desc += " normal";
				}
				if (cell_x.contains("control")) {
					cell_desc += " control";
				}
				sampleItem.setCell_desc(cell_desc);
			}
		}
		map.put("result", result);
		if (!isLink) {
			RequestUtils.setAttribute("geneItemList", result.getGeneItemList());
			logger.debug("isLink: {}, find geneItemList size is: {}", isLink,
					result.getGeneItemList().size());
		}
		@SuppressWarnings("unchecked")
		List<GeneItem> geneItemList = (List<GeneItem>) RequestUtils.getAttribute("geneItemList");
		if (geneItemList == null) {
			map.put("geneSize", 0);
		} else {
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
		} else {
			if (result.getCurrent() != null) {
				GeneRankCriteria criteria = new GeneRankCriteria();
				criteria.setGeneId(result.getCurrent().getGeneId());
				String ucscUrl = criteria
						.generateKey(GeneRankCriteria.ImageFileTempalte);
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
		String isremember = isRemembercookie == null ? "false"
				: isRemembercookie.getValue();
		map.put("isRemember", isremember);
		Cookie usernamecookie = RequestUtils.getCookie("userName");
		Cookie pwdcookie = RequestUtils.getCookie("userPassword");
		if (usernamecookie != null && pwdcookie != null) {
			map.put("cookiename", usernamecookie.getValue());
			map.put("cookiepassword", pwdcookie.getValue());
		}
		map.put("isLink", isLink);
		map.put("isHistory", isHistory);
		// map.put("isHistoryResult", isHistoryResult);
		map.put("pageSize", pageSize);
		if (logger.isDebugEnabled() && geneItemList != null) {
			logger.debug("at last: isLink: {}, find geneItemList size is: {}",
					isLink, geneItemList.size());
		}
		Integer [] counts = searchService.findProcessedAndInProcessSample("pathway");
		map.put("processed",counts[0]);
		map.put("inProcess", counts[1]);
		return "result_pathway";
	}

	@RequestMapping("result_multigene.htm")
	public String result_multigene(ModelMap map, HttpServletRequest request)
			throws Throwable {
		int page = ServletRequestUtils.getIntParameter(request, "page", 1);
		String multigene = (String) request.getParameter("multigene");
		if (multigene != null) {
			multigene = multigene.substring(0, multigene.length() - 1);
			String multigen = "";
			String[] genes = multigene.split(",");
			for (String s : genes) {
				multigen += s + "\n";
			}
			map.put("multigene", multigen);
		}
		Boolean isLink = ServletRequestUtils.getBooleanParameter(request,
				"isLink", false);
		Boolean isHistory = ServletRequestUtils.getBooleanParameter(request,
				"isHistory", false);
		Boolean isHistoryResult = ServletRequestUtils.getBooleanParameter(
				request, "isHistoryResult", false);
		if (!isLink) {
			RequestUtils.setAttribute("multigene", multigene);
		}
		String sort = (String) RequestUtils.getRequest().getParameter("sort");
		User user = (User) RequestUtils.getAttribute("user");
		if(user == null){
			return "redirect:/index.html";
		}
		if(user.getUserType() > 1){
			return "redirect:/result.htm";
		}
		if (user.getPageSize() != null) {
			pageSize = user.getPageSize();
		}
		SettingDTO settingDTO = new SettingDTO();
		if (isHistory || isHistoryResult) {
			// String keyword = (String)
			// RequestUtils.getAttribute("geneSymbol");
			// SearchHistory history =
			// searchHistoryService.findByKeyword(keyword);
			// history_id = history.get_id();
			// constructSettinsData(settingDTO, null, history);
			// String formateDate = DateUtils.format(history.getCreateDate(),
			// "yyyy/MM/dd");
			// map.put("date", formateDate);
		} else {
			constructSettinsData(settingDTO, user, null);
		}
		map.put("settingDTO", settingDTO);
		Map<String, Integer> sampleSumMap = SampleCache.getInstance()
				.getSampleSumMap();
		map.put("sampleSumMap", sampleSumMap);
		// find Sample by geneSymbol or refSeq
		SortType sortType = StringUtils.isEmpty(sort) ? SortType.ASC : SortType
				.parse(sort);
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
		// 判断是否是点击搜索结果
		Integer start = (page - 1) * pageSize;
		if (isHistoryResult) {
			// result = searchHistoryService.searchSample(history_id,
			// sourceList, experimentsList, sortType, start, pageSize);
		} else if (StringUtils.isNotBlank(multigene)) {
			long begin = System.nanoTime();
			// result = searchService.searchSample(geneSymbol, sourceList,
			// experimentsList, sortType, start, pageSize);
			result = searchService.searchSampleByMultigene(multigene,
					sourceList, experimentsList, sortType, start, pageSize);
			result.setUsedTime(begin, System.nanoTime());
		}
		int totalRecords = result.getTotal();
		int totalPage = totalRecords % pageSize == 0 ? totalRecords / pageSize
				: (totalRecords / pageSize) + 1;
		if (page > totalPage) {
			result = new SampleResult();
		}
		Map<String, String> fdMap = factorDescService.findAll();
		Map<String, String> cdMap = cellTypeDescService.findAll();
		if (result.getSampleItemList() != null) {
			for (SampleItem sampleItem : result.getSampleItemList()) {
				String factor = sampleItem.getFactor();
				if (factor == null || "".equals(factor)) {
					sampleItem.setFactor_desc(sampleItem.getDetail());
					continue;
				}
				String factor_desc = fdMap.get(factor);
				if (factor_desc == null || "".equals(factor_desc)) {
					factor_desc = factorDescService.findByFactorRegex(factor);
					if (factor_desc == null || "".equals(factor_desc)) {
						factor_desc = sampleItem.getDetail();
					}
				}
				sampleItem.setFactor_desc(factor_desc);
			}

			for (SampleItem sampleItem : result.getSampleItemList()) {
				String cell = sampleItem.getCell();
				if (cell == null || "".equals(cell) || "Summary Track".equals(sampleItem.getDataType()) || "Somatic Mutations".equals(sampleItem.getDataType())) {
					sampleItem.setCell_desc(cell);
					continue;
				}
				cell = cell.replace("-tumor", "").replace("-normal", "")
						.replace("-control", "");
				String cell_desc = cdMap.get(cell);

				if (cell_desc == null || "".equals(cell_desc)) {
					cell_desc = cellTypeDescService.findByCellRegex(cell);
					if (cell_desc == null || "".equals(cell_desc)) {
						cell_desc = cell;
					}
				}

				String cell_x = sampleItem.getCell();
				if (cell_x.contains("tumor")) {
					cell_desc += " tumor";
				}
				if (cell_x.contains("normal")) {
					cell_desc += " normal";
				}
				if (cell_x.contains("control")) {
					cell_desc += " control";
				}
				sampleItem.setCell_desc(cell_desc);
			}
		}
		map.put("result", result);
		if (!isLink) {
			RequestUtils.setAttribute("geneItemList", result.getGeneItemList());
			logger.debug("isLink: {}, find geneItemList size is: {}", isLink,
					result.getGeneItemList().size());
		}
		@SuppressWarnings("unchecked")
		List<GeneItem> geneItemList = (List<GeneItem>) RequestUtils.getAttribute("geneItemList");
		if (geneItemList == null) {
			map.put("geneSize", 0);
		} else {
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
		} else {
			if (result.getCurrent() != null) {
				GeneRankCriteria criteria = new GeneRankCriteria();
				criteria.setGeneId(result.getCurrent().getGeneId());
				String ucscUrl = criteria
						.generateKey(GeneRankCriteria.ImageFileTempalte);
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
		String isremember = isRemembercookie == null ? "false"
				: isRemembercookie.getValue();
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
			logger.debug("at last: isLink: {}, find geneItemList size is: {}",
					isLink, geneItemList.size());
		}
		Integer [] counts = searchService.findProcessedAndInProcessSample("multigene");
		map.put("processed",counts[0]);
		map.put("inProcess", counts[1]);
		return "result_multigene";
	}

/*	@RequestMapping("search.json")
	@ResponseBody
	public Object search(String geneSymbol, Integer page, String sort,
			String sources, String etypes) {
		SortType sortType = StringUtils.isEmpty(sort) ? SortType.ASC : SortType
				.parse(sort);
		Integer start = (page - 1) * pageSize;
		String[] sourceArray = StringUtils.isNotBlank(sources) ? sources
				.split(",") : new String[] {};
		String[] etypesArray = StringUtils.isNotBlank(etypes) ? etypes
				.split(",") : new String[] {};
		return searchService.searchSample(geneSymbol,
				Arrays.asList(sourceArray), Arrays.asList(etypesArray),
				sortType, start, pageSize);
	}*/

	@RequestMapping("autocomplete_multigene.json")
	@ResponseBody
	public Object autocomplete_multigene() {
		String key = (String) RequestUtils.getRequest().getParameter("term");
		List<String> list = typeAheadService.search(key, "");
		return list.toArray();
	}

	@RequestMapping("findPathwayByCharactor.json")
	@ResponseBody
	public Object findPathwayByCharactor() {
		List<PathWay> result = new ArrayList<PathWay>();
		String key = (String) RequestUtils.getRequest().getParameter("term");
		result = pathWaySearchService.searchByFirstCharactor(key);
		return result.toArray();
	}
	
	@RequestMapping("autocomplete.json")
	@ResponseBody
	public Object autocomplete() {
		String key = (String) RequestUtils.getRequest().getParameter("term");
		List<String> list = typeAheadService.search(key, "");
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for (String key1 : list) {
			if (key1.startsWith("NM_") || key1.startsWith("NR_")) {
				String geneSymbol = TxrRefCache.getInstance()
						.getGeneSymbolByRefSeq(key1);
				Gene gene = GeneCache.getInstance().getGeneByName(key1);
				if (gene != null) {
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
			} else {
				List<TxrRef> txrref = TxrRefCache.getInstance()
						.getTxrRefBySymbol(key1.toLowerCase());
				for (TxrRef t : txrref) {
					if (t.getRefseq() != null && !"".equals(t.getRefseq())) {
						Gene gene = GeneCache.getInstance().getGeneByName(
								t.getRefseq());
						if (gene != null) {
							Map<String, Object> map = new HashMap<String, Object>();
							if(t.getAlias()==null){
								map.put("show", key1);
								map.put("geneSymbol", key1);
							}else{
								map.put("show",key1);
								map.put("geneSymbol", t.getAlias());
							}
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

	@RequestMapping("autocomplete_Mouse.json")
	@ResponseBody
	public Object autocomplete_Mouse() {
		String key = (String) RequestUtils.getRequest().getParameter("term");
		List<String> list = typeAheadService.search_Mouse(key, "");
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for (String key1 : list) {
			if (key1.startsWith("NM_") || key1.startsWith("NR_")) {
				String geneSymbol = MouseTxrRefCache.getInstance()
						.getGeneSymbolByRefSeq(key1);
				Gene gene = MouseGeneCache.getInstance().getGeneByName(key1);
				if (gene != null) {
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
			} else {
				List<TxrRef> txrref = MouseTxrRefCache.getInstance()
						.getTxrRefBySymbol(key1.toLowerCase());
				for (TxrRef t : txrref) {
					if (t.getRefseq() != null && !"".equals(t.getRefseq())) {
						Gene gene = MouseGeneCache.getInstance().getGeneByName(
								t.getRefseq());
						if (gene != null) {
							Map<String, Object> map = new HashMap<String, Object>();
							if(t.getAlias()==null){
								map.put("show", key1);
								map.put("geneSymbol", key1);
							}else{
								map.put("show",key1);
								map.put("geneSymbol", t.getAlias());
							}
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
		for (PathWay pw : list) {
			slist.add(pw.getPathwayName());
		}
		return slist.toArray();
	}

	@RequestMapping("autocomplete_miRNA.json")
	@ResponseBody
	public Object autocompleteMiRNA() {
		String key = (String) RequestUtils.getRequest().getParameter("term");
		List<MiRNA> list = miRNAService.search(key);
		List<String> slist = new ArrayList<String>();
		for (MiRNA mi : list) {
			slist.add(mi.getMiRNAName());
		}
		return slist.toArray();
	}

	@RequestMapping("chart.json")
	@ResponseBody
	public Object chart() {
		String geneId = (String) RequestUtils.getRequest().getParameter(
				"geneId");
		JFreeChartUtils j = new JFreeChartUtils();
		try {
			String fileName = j.chart(Integer.parseInt(geneId));
			List<String> slist = new ArrayList<String>();
			slist.add(fileName);
			return slist.toArray();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void constructSettinsData(SettingDTO settingDTO, User user,
			SearchHistory history) {
		for (ExperimentType type : ExperimentType.getUiMap().values()) {
			settingDTO.getExperimentsMap().put(type.getDesc(), false);
			settingDTO.getExperimentsMap_mouse().put(type.getDesc(), false);
		}
		for (SourceType type : SourceType.getUiMap().values()) {
			settingDTO.getSourcesMap().put(type.desc(), false);
			settingDTO.getSourcesMap_mouse().put(type.desc(), false);
		}
		String experiments = "", source = "", cellType = "", experiments_mouse = "", source_mouse = "";

		String experimentsAll = "", sourceAll = "";

		Set<String> exSet = settingDTO.getExperimentsMap().keySet();
		for (Iterator<String> it = exSet.iterator(); it.hasNext();) {
			experimentsAll += ExperimentType.getType(it.next().toString())
					.value() + ",";
		}
		experimentsAll = experimentsAll.substring(0,
				experimentsAll.length() - 1);

		Set<String> sourceSet = settingDTO.getSourcesMap().keySet();
		for (Iterator<String> it = sourceSet.iterator(); it.hasNext();) {
			sourceAll += SourceType.getType(it.next().toString()).value() + ",";
		}
		sourceAll = sourceAll.substring(0, sourceAll.length() - 1);

		if (user != null) {
			logger.debug("set SettingDTO by user.");
			experiments = user.getExperiments();
			if (StringUtils.isEmpty(experiments)) {
				experiments = experimentsAll;
			}
			source = user.getSource();
			if (StringUtils.isEmpty(source)) {
				source = sourceAll;
			}
			experiments_mouse = user.getExperiments_mouse();
			if (StringUtils.isEmpty(experiments_mouse)) {
				experiments_mouse = experimentsAll;
			}
			source_mouse = user.getSource_mouse();
			if (StringUtils.isEmpty(source_mouse)) {
				source_mouse = sourceAll;
			}
			cellType = user.getCellType();
		} else if (history != null) {
			logger.debug("set SettingDTO by history.");
			experiments = history.getExperiments();
			if (StringUtils.isEmpty(experiments)) {
				experiments = experimentsAll;
			}
			source = history.getSource();
			if (StringUtils.isEmpty(source)) {
				source = sourceAll;
			}
			experiments_mouse = history.getExperiments_mouse();
			if (StringUtils.isEmpty(experiments_mouse)) {
				experiments_mouse = experimentsAll;
			}
			source_mouse = history.getSource_mouse();
			if (StringUtils.isEmpty(source_mouse)) {
				source_mouse = sourceAll;
			}
			cellType = history.getCellType();
		} 
		/*else {
			logger.debug("set SettingDTO by cookie.");
			Cookie experimentsCookie = RequestUtils.getCookie("experiments");
			if (experimentsCookie != null) {
				experiments = experimentsCookie.getValue();
				if ("".equals(experiments) || experiments == null) {
					experiments = experimentsAll;
				}
			} else {
				experiments = experimentsAll;
			}
			Cookie sourceCookie = RequestUtils.getCookie("source");
			if (sourceCookie != null) {
				source = sourceCookie.getValue();
				if ("".equals(source) || source == null) {
					source = sourceAll;
				}
			} else {
				source = sourceAll;
			}
			Cookie cellTypeCookie = RequestUtils.getCookie("cellType");
			if (cellTypeCookie != null) {
				cellType = cellTypeCookie.getValue();
			}
		}*/
		if (StringUtils.isNotBlank(experiments)) {
			String[] itemsArray = experiments.split(",");
			for (String value : itemsArray) {
				ExperimentType type = ExperimentType.parse(Integer
						.valueOf(value));
				settingDTO.getExperimentsMap().put(type.getDesc(), true);
			}
		}
		if (StringUtils.isNotBlank(source)) {
			String[] sourcesArray = source.split(",");
			for (String value : sourcesArray) {
				SourceType type = SourceType.parse(Integer.valueOf(value));
				if(type == null)
				{
					continue;
				}
				settingDTO.getSourcesMap().put(type.desc(), true);
			}
		}
		if (StringUtils.isNotBlank(experiments_mouse)) {
			String[] itemsArray = experiments_mouse.split(",");
			for (String value : itemsArray) {
				ExperimentType type = ExperimentType.parse(Integer
						.valueOf(value));
				settingDTO.getExperimentsMap_mouse().put(type.getDesc(), true);
			}
		}
		if (StringUtils.isNotBlank(source_mouse)) {
			String[] sourcesArray = source_mouse.split(",");
			for (String value : sourcesArray) {
				SourceType type = SourceType.parse(Integer.valueOf(value));
				if(type == null)
				{
					continue;
				}
				settingDTO.getSourcesMap_mouse().put(type.desc(), true);
			}
		}
		settingDTO.setCellType(cellType);
	}

	@RequestMapping("about.htm")
	public String about(ModelMap map) {
		return "about";
	}
	
	@RequestMapping("news.htm")
	public String news() {
		return "news";
	}
	
	@RequestMapping("factorMapping.json")
	@ResponseBody
	public Object factorMapping() {
		List<String> list = searchService.findFactorMapingNotExist();
		return list.toArray();
	}

	@RequestMapping("waitForUcsc.htm")
	public String waitForUcsc(ModelMap map, HttpServletRequest request) {
		String ucscUrl = request.getParameter("url");
		ucscUrl = ucscUrl.replaceAll("\\$", "&");
		map.put("ucscUrl", ucscUrl);
		return "waitForUcsc";
	}
	
	@RequestMapping("comment.htm")
	@ResponseBody
	public void showComment( HttpServletRequest request,HttpServletResponse response) {
		StringBuffer html = new StringBuffer() ;
		String sampleId = (String) RequestUtils.getRequest().getParameter("sampleId");
		if(StringUtils.isNoneBlank(sampleId)){
			List<Comment> comments = searchService.showCommentsBySampleId(Integer.parseInt(sampleId));
			for (Comment comment : comments){
				html.append("<strong>"+comment.getUserName()+"</strong>" + ":" + comment.getContent() +"</br>");
			}
			html.append("<textarea id='comment_"+sampleId+"' style='width:95%; height:25%; text-align: left;'></textarea>");
			String errorMessage = messageSourceHelper.getMessage("error.alreadySubmit");
			html.append("<span id='submit_error' class='errorMessage' style='display:none;'>"+errorMessage+"</span>");
			String message = messageSourceHelper.getMessage("button.publish");
			html.append("<button onclick='submitComment("+sampleId+");'>"+message+"</button>");
		}
		PrintWriter out = null;
		response.setContentType("text/html;charset=UTF-8");
		try {
			out = response.getWriter();
			out.write(html.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
//		return html.toString();
	}
	
	@RequestMapping("submitComment.htm")
	@ResponseBody
	public void submitComment(HttpServletRequest request, HttpServletResponse response) {
		String sampleId = request.getParameter("sampleId");
		ICommentDAO commentDAO = DAOFactory.getDAO(ICommentDAO.class);
		User user = (User) RequestUtils.getAttribute("user");
		int userId = user == null ? 0 : user.getUserId();
		int sid = Integer.parseInt(sampleId);
		SmartDBObject query = new SmartDBObject();
		query.put("sampleId", Integer.parseInt(sampleId));
		query.put("userId", userId);
		List<Comment> commentList = commentDAO.find(query);
		boolean flag = false;
		for(Comment com : commentList){
			if(com.getContent().equals(request.getParameter("content").trim())){
//				return JsonResult(null,"already submit", "1000");
				PrintWriter out = null;
				response.setContentType("text/html;charset=UTF-8");
				try {
					out = response.getWriter();
					out.write("error");
					flag = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if(!flag)
		{
			Comment comment = new Comment();
			comment.setCommentId(commentDAO.getSequenceId("CCLEComment"));
			comment.setContent(request.getParameter("content"));
			comment.setUserId(userId);
			comment.setUserName(user.getName());
			comment.setSampleId(sid);
			commentDAO.create(comment);
			PrintWriter out = null;
			response.setContentType("text/html;charset=UTF-8");
			try {
				out = response.getWriter();
				out.write("success");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
//		String html = (String)showComment(request, response);
//		return html;
	}

	@RequestMapping("dataDownload.htm")
	public String dataDownload(ModelMap map, HttpServletRequest request) {
		String[] column = { "Rank", "DataSetID", "DataType", "Cell", "Factor",
				"Order/Total", "Percentile(%)", "tssTesCount", "tss5KCount",
				"Study", "Lab" };
		SettingDTO settingDTO = new SettingDTO();
		constructSettinsData(settingDTO, null, null);
		map.put("settingDTO", settingDTO);
		map.put("columns", column);
		return "dataDownload";
	}

	@RequestMapping("download.htm")
	public void download(ModelMap map, HttpServletRequest request,
			HttpServletResponse res) throws IOException {
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
		String cell = request.getParameter("cell") == null ? "" : request
				.getParameter("cell");
		String factor = request.getParameter("factor") == null ? "" : request
				.getParameter("factor");
		String geneSymbols = request.getParameter("geneSymbols");
		if (geneSymbols != null && geneSymbols.endsWith(",")) {
			geneSymbols = geneSymbols.substring(0, geneSymbols.length() - 1);
		}
		if (cell != null && cell.endsWith(",")) {
			cell = cell.substring(0, cell.length() - 1);
		}
		if (factor != null && factor.endsWith(",")) {
			factor = factor.substring(0, factor.length() - 1);
		}
		String columns = request.getParameter("columns");
		String[] titles = { "Rank", "DataSetID", "DataType", "Cell", "Factor",
				"Order/Total", "Percentile(%)", "tssTesCount", "tss5KCount",
				"Study", "Lab" };

		if (columns != null) {
			titles = columns.split(",");
		}
		XSSFWorkbook wb = null;
		if (StringUtils.isNotEmpty(geneSymbols) && !geneSymbols.equals(",")) {
			wb = GeneRankExport.getInstance().buildWorkbookByDownload(
					geneSymbols, sources, experiments, cell, factor, titles);

			// 定义文件名
			res.setHeader("Content-Disposition", "attachment;filename=\""
					+ geneSymbols + ".csv\"");
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

	@RequestMapping("result_miRNA.htm")
	public Object result_miRNA(ModelMap map, HttpServletRequest request)
			throws Throwable {
		String miRNA = request.getParameter("miRNA");
		int page = ServletRequestUtils.getIntParameter(request, "page", 1);
		Boolean isLink = ServletRequestUtils.getBooleanParameter(request,
				"isLink", false);
		Boolean isHistory = ServletRequestUtils.getBooleanParameter(request,
				"isHistory", false);
		Boolean isHistoryResult = ServletRequestUtils.getBooleanParameter(
				request, "isHistoryResult", false);

		if (!isLink) {
			RequestUtils.setAttribute("miRNA",
					miRNA!=null && miRNA.length() > 4 ? miRNA.substring(4) : "");
		}
		String sort = (String) RequestUtils.getRequest().getParameter("sort");
		SortType sortType = StringUtils.isEmpty(sort) ? SortType.ASC : SortType
				.parse(sort);
		User user = (User) RequestUtils.getAttribute("user");
		if(user == null){
			return "redirect:/index.html";
		}
		if(user.getUserType() > 1){
			return "redirect:/result.htm";
		}
		if (user != null && user.getPageSize() != null) {
			pageSize = user.getPageSize();
		}
		
		SampleResult result = new SampleResult();
		List<String> experimentsList = new ArrayList<String>();
		List<String> sourceList = new ArrayList<String>();
		experimentsList.add("MIRNA-seq");
		experimentsList.add("Summary Track");
		sourceList.add("TCGA");
		Integer start = (page - 1) * pageSize;
		if (StringUtils.isNotBlank(miRNA)) {
			long beginTime = System.nanoTime();
			result = searchMiRNAService.searchMiRNA(miRNA, sourceList,
					experimentsList, sortType, start, pageSize);
			result.setUsedTime(beginTime, System.nanoTime());
			int totalRecords = result.getTotal();
			int totalPage= totalRecords % pageSize == 0 ? totalRecords
						/ pageSize : (totalRecords / pageSize) + 1;
			if (page > totalPage) {
				int total_all = result.getTotal_all();
				result = new SampleResult();
				result.setTotal_all(total_all);
			}
			Map<String, String> fdMap = factorDescService.findAll();
			Map<String, String> cdMap = cellTypeDescService.findAll();
			if (result.getSampleItemList() != null) {
				for (SampleItem sampleItem : result.getSampleItemList()) {
					String factor = sampleItem.getFactor();
					if (factor == null || "".equals(factor)) {
						sampleItem.setFactor_desc(sampleItem.getDetail());
						continue;
					}
					String factor_desc = fdMap.get(factor);
					if (factor_desc == null || "".equals(factor_desc)) {
						factor_desc = factorDescService
								.findByFactorRegex(factor);
						if (factor_desc == null || "".equals(factor_desc)) {
							factor_desc = factor;
						}
					}
					sampleItem.setFactor_desc(factor_desc);
				}

				for (SampleItem sampleItem : result.getSampleItemList()) {
					String cell = sampleItem.getCell();
					if (cell == null || "".equals(cell) || "Summary Track".equals(sampleItem.getDataType())) {
						sampleItem.setCell_desc(cell);
						continue;
					}
					cell = cell.replace("-tumor", "").replace("-normal", "")
							.replace("-control", "");
					String cell_desc = cdMap.get(cell);

					if (cell_desc == null || "".equals(cell_desc)) {
						cell_desc = cellTypeDescService.findByCellRegex(cell);
						if (cell_desc == null || "".equals(cell_desc)) {
							cell_desc = cell;
						}
					}

					String cell_x = sampleItem.getCell();
					if (cell_x.contains("tumor")) {
						cell_desc += " tumor";
					}
					if (cell_x.contains("normal")) {
						cell_desc += " normal";
					}
					if (cell_x.contains("control")) {
						cell_desc += " control";
					}
					sampleItem.setCell_desc(cell_desc);
				}
			}
			map.put("result", result);
			if (!isLink) {
				RequestUtils.setAttribute("geneItemList",
						result.getGeneItemList());
				logger.debug("isLink: {}, find geneItemList size is: {}",
						isLink, result.getGeneItemList().size());
			}
			@SuppressWarnings("unchecked")
			List<GeneItem> geneItemList = (List<GeneItem>) RequestUtils.getAttribute("geneItemList");
			if (geneItemList == null) {
				map.put("geneSize", 0);
			} else {
				map.put("geneSize", geneItemList.size());
			}
			int beginPage = page > 6 ? page - 5 : 1;
			int endPage = totalPage;
			if (totalPage > 10 && totalPage > page + 5) {
				endPage = page > 6 ? page + 5 : 10;
			}
			if (isHistoryResult) {
				beginPage = 1;
				endPage = totalPage;
			} else {
				if (result.getCurrent() != null) {
					GeneRankCriteria criteria = new GeneRankCriteria();
					criteria.setGeneId(result.getCurrent().getGeneId());
					String ucscUrl = criteria
							.generateKey(GeneRankCriteria.ImageFileTempalte);
					map.put("ucscUrl", ucscUrl);
				}
			}
			map.put("totalPage", totalPage);
			map.put("page", page);
			map.put("beginPage", beginPage);
			map.put("endPage", endPage);
			map.put("miRNA", miRNA.length() > 4 ? miRNA.substring(4) : "");
			map.put("totalRecords", totalRecords);
			map.put("sortType", sortType.name());
			Cookie isRemembercookie = RequestUtils.getCookie("isRemember");
			String isremember = isRemembercookie == null ? "false"
					: isRemembercookie.getValue();
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
				logger.debug(
						"at last: isLink: {}, find geneItemList size is: {}",
						isLink, geneItemList.size());
			}
		}
		Integer [] counts = searchService.findProcessedAndInProcessSample("miRNA");
		map.put("processed",counts[0]);
		map.put("inProcess", counts[1]);
		return "result_miRNA";
	}
	
	@RequestMapping("result_diseasesRank.htm")
	public String result_diseasesRank(ModelMap map, HttpServletRequest request) {
		User user = (User) RequestUtils.getAttribute("user");
		if(user == null){
			return "redirect:/index.html";
		}
		if(user.getUserType() > 1){
			return "redirect:/result.htm";
		}
		SettingDTO settingDTO = new SettingDTO();
		constructSettinsData(settingDTO, null, null);
		map.put("settingDTO", settingDTO);
		String cancerType = RequestUtils.getRequest().getParameter("cancerType");
		if(cancerType != null && !"".equals(cancerType))
		{
//			map.put("currentCancerType", cancerType);
			RequestUtils.setAttribute("currentCancerType", cancerType);
			cancerType = cancerType.toLowerCase();
			List<Integer> intSourceList = sampleSearchServiceHelper.toSourceTypies("1");
	        List<Integer> intEtypeList = sampleSearchServiceHelper.toEtypies("0");
			List<Sample> sampleList = SampleCache.getInstance().getSampleByCellAndDetail("TCGA-"+cancerType+"-total tumor/normal diff", null, intSourceList, intEtypeList);
			if(sampleList != null && sampleList.size() > 0)
			{
				for(Sample sample :sampleList)
				{
					Integer sampleId = sample.getSampleId();
					List<Gene> geneList = geneRankSearchService.searchGeneTop10BySampleId(sampleId);
					map.put(sample.getSettype().replace("-", "")+"_UnMatched_top10", geneList);
				}
			}
			
			List<Sample> sampleList_matched = SampleCache.getInstance().getSampleByCellAndDetail("TCGA-"+cancerType+"-matched tumor/normal diff", null, intSourceList, intEtypeList);
			if(sampleList_matched != null && sampleList_matched.size() > 0)
			{
				for(Sample sample :sampleList_matched)
				{
					Integer sampleId = sample.getSampleId();
					List<Gene> geneList = geneRankSearchService.searchGeneTop10BySampleId(sampleId);
					map.put(sample.getSettype().replace("-", "")+"_Matched_top10", geneList);
				}
			}
	
			List<MiRNASample> sampleList_unMatched = MiRNASampleCache.getInstance().getSampleByCell("TCGA-"+cancerType+"-total tumor/normal diff");
			if(sampleList_unMatched != null && sampleList_unMatched.size() > 0)
			{
				for(MiRNASample sample :sampleList_unMatched)
				{
					Integer sampleId = sample.getMiRNASampleId();
					List<String> rankList = miRNAService.searchTopMiRNA(sampleId, 10);
//					List<Gene> geneList = geneRankSearchService.searchGeneTop10BySampleId(sampleId);
					map.put("miRNA_top10", rankList);
					break;
				}
			}
			
			List<MiRNASample> mirnasampleList_Matched = MiRNASampleCache.getInstance().getSampleByCell("TCGA-"+cancerType+"-matched tumor/normal diff");
			if(mirnasampleList_Matched != null && mirnasampleList_Matched.size() > 0)
			{
				for(MiRNASample sample :mirnasampleList_Matched)
				{
					Integer sampleId = sample.getMiRNASampleId();
					List<String> rankList = miRNAService.searchTopMiRNA(sampleId, 10);
//					List<Gene> geneList = geneRankSearchService.searchGeneTop10BySampleId(sampleId);
					map.put("miRNA_matched_top10", rankList);
					break;
				}
			}
			
			map.put("diseasesType", cancerType.toUpperCase());
		}
		Integer [] counts = searchService.findProcessedAndInProcessSample("diseasesRank");
		map.put("processed",counts[0]);
		map.put("inProcess", counts[1]);
		return "result_diseasesRank";
	}
	
	@RequestMapping("showTopGenes.json")
	@ResponseBody
	public Object showTopGenes(){
		String html = "" ;
		String sampleId = (String) RequestUtils.getRequest().getParameter("sampleId");
		String ctx = (String )RequestUtils.getRequest().getParameter("ctx");
		String pSize = (String)RequestUtils.getRequest().getParameter("size");
		int size = Integer.parseInt(pSize);
		if(StringUtils.isNoneBlank(sampleId)){
			Map<String,Gene> topGenes =  searchService.searchTop5Genes(Integer.parseInt(sampleId),size);
			html = "<span style='margin-left:12px;'>search Top <input type='text' id='showGeneSize_"+ sampleId +"' style ='width: 20px;height:20px;'> Gene<a class='btn btn-mini' id='showTop10Gene_"+sampleId+"' href='javascript:;' onclick='showTop5Genes("+sampleId+",false)' targetDrag='"+sampleId+"Drag' style='display: inline; width:20px;height:20px;'>Go</a></span><br>";
			for (Iterator<String> it = topGenes.keySet().iterator();it.hasNext();){
				String key = it.next(); 
				Gene gene = topGenes.get(key);
				String str = "<a style='margin-left: 15px;' href='"+ctx+"/result.htm?page=1&geneSymbol="+key+"&sort=ASC&isLink=false&isHistory=false' target='_blank'><strong>"+key+"</strong></a><br>"
						+ "<span style='margin-left: 20px;'>RefseqId:"+gene.getTxName() +"</span><span style='margin-left: 10px;'>Chr:"+gene.getSeqName().substring(3)+"</span><span style='margin-left: 10px;'>start:"+gene.getStart() +"</span><span style='margin-left: 10px;'>end:"+gene.getEnd()
						+"</span><br>";
				html+=str;
			}
		}
		return html;
	}
	
	@RequestMapping("showTopMiRNAGenes.json")
	@ResponseBody
	public Object showTopMiRNAGenes(){
		String html = "" ;
		int size = 10;
		String sampleId = (String) RequestUtils.getRequest().getParameter("miRNASampleId");
		String ctx = (String )RequestUtils.getRequest().getParameter("ctx");
		String pSize = (String )RequestUtils.getRequest().getParameter("size");
		if(pSize!=null){
			size = Integer.parseInt(pSize);
		}
		if(StringUtils.isNoneBlank(sampleId)){
			List<String> miRNANames =  miRNAService.searchTopMiRNA(Integer.parseInt(sampleId),size);
			html = "<span style='margin-left:12px;'>search Top <input type='text' id='showGeneSize_" + sampleId + "' style ='width: 20px;height:20px;'> MiRNA<a class='btn btn-mini' id='showTop10Gene_"+sampleId+"' href='javascript:;' onclick='showTop10Genes("+sampleId+",false)' targetDrag='"+sampleId+"Drag' style='display: inline; width:20px;height:20px;margin-left:5px;'>Go</a></span><br>";
			for (String s : miRNANames){ 
				String str = "<a style='margin-left: 15px;' href='"+ctx+"/result_miRNA.htm?page=1&miRNA="+s+"&sort=ASC&isLink=false&isHistory=false' target='_blank'><strong>"+s+"</strong></a><br>";
				html+=str;
			}
		}
		return html;
	}
	
	@RequestMapping("result_genomicRegion.htm")
	public String result_genomicRegion(ModelMap map, HttpServletRequest request){
		int page = ServletRequestUtils.getIntParameter(request, "page", 1);
		String genomicRegion = request.getParameter("genomicRegion");
		User user = (User) RequestUtils.getAttribute("user");
		if(user == null){
			return "redirect:/index.html";
		}
		if(user.getUserType() > 1){
			return "redirect:/result.htm";
		}
		if (user.getPageSize() != null) {
			pageSize = user.getPageSize();
		}
		Integer start = (page -1)*pageSize;
		long begin = System.nanoTime();
		SampleResult result = searchService.findSampleByGenomicRegion(genomicRegion, start ,  pageSize);
		result.setUsedTime(begin, System.nanoTime());
		int totalRecords = result.getTotal();
		int totalPage = totalRecords % pageSize == 0 ? totalRecords / pageSize
				: (totalRecords / pageSize) + 1;
		if (page > totalPage) {
			int total_all = result.getTotal_all();
			result = new SampleResult();
			result.setTotal_all(total_all);
		}
		int beginPage = page > 6 ? page - 5 : 1;
		int endPage = totalPage;
		if (totalPage > 10 && totalPage > page + 5) {
			endPage = page > 6 ? page + 5 : 10;
		}
		if(StringUtils.isNotEmpty(genomicRegion)){
			RequestUtils.setAttribute("genomicRegion", genomicRegion);
			String path = null;
			if(genomicRegion.equals("promoters")){
				path = "promoter.bed";
			}else if(genomicRegion.equals("H1")){
				path = "H1.bed";
			}else if(genomicRegion.equals("IMR90")){
				path = "IMR90.bed";
			}else if(genomicRegion.equals("vista.neg")){
				path = "vista.neg.bed";
			}else if(genomicRegion.equals("vista.pos")){
				path = "vista.pos.bed";
			}else if(genomicRegion.equals("cpghg19")){
				path = "cpghg19.bed2";
			}else if(genomicRegion.equals("U87")){
				path = "U87_Enhancers.bed2 ";
			}else if(genomicRegion.equals("Super_U87")){
				path = "Super_U87_Enhancers.bed2";
			}else if(genomicRegion.equals("Super_MM1S")){
				path = "Super_MM1S_Enhancers.bed2";
			}else if(genomicRegion.equals("Super_H2171")){
				path = "Super_H2171_Enhancers.bed2";
			}else if(genomicRegion.equals("MM1S")){
				path = "MM1S_Enhancers.bed2";
			}else if(genomicRegion.equals("H2171")){
				path = "H2171_Enhancers.bed2";
			}
			map.put("bedPath", path);
		}
		map.put("result", result);
		map.put("totalRecords", totalRecords);
		map.put("page", page);
		map.put("pageSize", pageSize);
		map.put("totalPage", totalPage);
		map.put("beginPage", beginPage);
		map.put("endPage", endPage);
		Integer [] counts = searchService.findProcessedAndInProcessSample("genomicRegion");
		map.put("processed",counts[0]);
		map.put("inProcess", counts[1]);
		return "result_genomicRegion";
	}
	
	@RequestMapping("showRegionList.json")
	public String showRegionList(ModelMap map){
		String bedPath = RequestUtils.getRequest().getParameter("bedPath");
		List<String> list = searchService.regionList(bedPath);
		map.put("regionList",list );
		return "regionList";
	}
	
	@RequestMapping("modifyCell.json")
	public String modifyCell(){
		String cellOld = RequestUtils.getRequest().getParameter("cellOld").trim();
		String cellNew = RequestUtils.getRequest().getParameter("cellNew").trim();
		if(!cellOld.equals(cellNew) && StringUtils.isNotEmpty(cellNew)){
			searchService.modifyCell(cellOld,cellNew);
		}
		return "success";
	}
	
	@RequestMapping("modifyDetail.json")
	public String modifyDetail(){
		String factorOld = RequestUtils.getRequest().getParameter("factorOld").trim();
		String factorNew = RequestUtils.getRequest().getParameter("factorNew").trim();
		String cell = RequestUtils.getRequest().getParameter("cell").trim();
		Integer sampleId =Integer.parseInt(RequestUtils.getRequest().getParameter("sampleId"));
		if(!factorOld.equals(factorNew) && StringUtils.isNotEmpty(factorNew) && StringUtils.isNotEmpty(cell)){
			searchService.modifyDetail(cell,factorOld,factorNew,sampleId);
		}
		return "success";
	}
	
	@RequestMapping("modifyLab.json")
	public Object modifyLab(){
		String labOld = RequestUtils.getRequest().getParameter("labOld").trim();
		String labNew = RequestUtils.getRequest().getParameter("labNew").trim();
		if(!labOld.equals(labNew) && StringUtils.isNotEmpty(labNew) ){
			searchService.modifyLab(labOld,labNew);
		}
		return "SUCCESS";
	}
	
	@RequestMapping("result_geneSearchAdvanced.htm")
	public String result_geneSearchAdvanced(ModelMap map, HttpServletRequest request)
			throws Throwable {
		int page = ServletRequestUtils.getIntParameter(request, "page", 1);
		String geneSymbol = (String) request.getParameter("geneSymbol");
		String geneCell = request.getParameter("cell");
		String geneDetail = request.getParameter("detail");
		String experiments = request.getParameter("experiments");
		String sources = request.getParameter("sources");
		String genome = (String) request.getParameter("genome") != null ? (String) request.getParameter("genome") : "Human";
		Boolean isLink = ServletRequestUtils.getBooleanParameter(request,
				"isLink", false);
		Boolean isHistory = ServletRequestUtils.getBooleanParameter(request,
				"isHistory", false);
		Boolean isHistoryResult = ServletRequestUtils.getBooleanParameter(
				request, "isHistoryResult", false);
		/*if(StringUtils.isEmpty(geneCell) && StringUtils.isEmpty(geneDetail)){
			return null;
		}*/
		if (!isLink) {
			RequestUtils.setAttribute("geneSymbol", geneSymbol);
		}
		String sort = (String) RequestUtils.getRequest().getParameter("sort");
		User user = (User) RequestUtils.getAttribute("user");
		if(user == null){
			return "redirect:/index.html";
		}
		if(user.getUserType() > 1){
			return "redirect:/result.htm";
		}
		if (user.getPageSize() != null) {
			pageSize = user.getPageSize();
		}
		SettingDTO settingDTO = new SettingDTO();
		String history_id = "";
		if (isHistory || isHistoryResult) {
			String keyword = (String) RequestUtils.getAttribute("geneSymbol");
			SearchHistory history = searchHistoryService.findByKeyword(keyword,
					user);
			history_id = history.get_id();
			constructSettinsData(settingDTO, null, history);
			String formateDate = DateUtils.format(history.getCreateDate(),
					"yyyy/MM/dd");
			map.put("date", formateDate);
		} else {
			constructSettinsData(settingDTO, user, null);
		}
		map.put("settingDTO", settingDTO);
		Map<String, Integer> sampleSumMap = SampleCache.getInstance()
				.getSampleSumMap();
		map.put("sampleSumMap", sampleSumMap);
		Map<String, Integer> mouseSampleSumMap = SampleCache.getInstance().getMouseSampleSumMap();
		map.put("mouseSampleSumMap", mouseSampleSumMap);
		// find Sample by geneSymbol or refSeq
		SortType sortType = StringUtils.isEmpty(sort) ? SortType.ASC : SortType
				.parse(sort);
		List<String> experimentsList = new ArrayList<String>();
		List<String> sourceList = new ArrayList<String>();
		
		Map<String,Boolean> experimentsMap = new HashMap<String, Boolean>();
		Map<String,Boolean> sourcesMap = new HashMap<String, Boolean>();
		for (ExperimentType type : ExperimentType.getUiMap().values()) {
			experimentsMap.put(type.getDesc(), false);
		}
		for (SourceType type : SourceType.getUiMap().values()) {
			sourcesMap.put(type.desc(), false);
		}
		if(StringUtils.isNotEmpty(experiments)){
			for (String exp : experiments.split(",")) {
				experimentsList.add(exp);
				experimentsMap.put(exp, true);
			}
		}else{
			Set<String> exSet = experimentsMap.keySet();
			for (Iterator<String> it = exSet.iterator(); it.hasNext();) {
				experimentsMap.put(it.next().toString(), true);
			}
		}
		
		if(StringUtils.isNotEmpty(sources)){
			for (String source : sources.split(",")) {
				sourceList.add(source);
				sourcesMap.put(source, true);
			}
		}else{
			Set<String> sourceSet = sourcesMap.keySet();
			for (Iterator<String> it = sourceSet.iterator(); it.hasNext();) {
				sourcesMap.put(it.next().toString(), true);
			}
		}
		map.put("experimentsMap", experimentsMap);
		map.put("sourcesMap", sourcesMap);
		SampleResult result = new SampleResult();
		// 判断是否是点击搜索结果
		Integer start = (page - 1) * pageSize;
		if (isHistoryResult) {
			result = searchHistoryService.searchSample(history_id, sourceList,
					experimentsList, sortType, start, pageSize);
		} else if (StringUtils.isNotBlank(geneSymbol)) {
			long begin = System.nanoTime();
			if(genome.equals("Human")){
				result = searchService.advancedSearch(geneSymbol, sourceList, experimentsList, geneCell, geneDetail, sortType, start, pageSize);
			}
			else if(genome.equals("Mouse")){
				result = mouseSearchService.advancedSearch(geneSymbol, sourceList,experimentsList, geneCell, geneDetail, sortType, start, pageSize);
			}
			result.setUsedTime(begin, System.nanoTime());
		}
		int totalRecords = result.getTotal();
		int totalPage = totalRecords % pageSize == 0 ? totalRecords / pageSize
				: (totalRecords / pageSize) + 1;
		if (page > totalPage) {
			int total_all = result.getTotal_all();
			result = new SampleResult();
			result.setTotal_all(total_all);
		}
		Map<String, String> fdMap = factorDescService.findAll();
		Map<String, String> cdMap = cellTypeDescService.findAll();
		if (result.getSampleItemList() != null) {
			for (SampleItem sampleItem : result.getSampleItemList()) {
				String factor = sampleItem.getFactor();
				String factor_desc = fdMap.get(factor);

				
				if (factor_desc == null || "".equals(factor_desc)) {
					factor_desc = sampleItem.getDetail();
				}
				
				sampleItem.setFactor_desc(factor_desc);
			}

			for (SampleItem sampleItem : result.getSampleItemList()) {
				String cell = sampleItem.getCell();
				if (cell == null || "".equals(cell) || "Summary Track".equals(sampleItem.getDataType()) || "Somatic Mutations".equals(sampleItem.getDataType())) {
					sampleItem.setCell_desc(cell);
					continue;
				}
				cell = cell.replace("-tumor", "").replace("-normal", "")
						.replace("-control", "");
				String cell_desc = cdMap.get(cell);

				if (cell_desc == null || "".equals(cell_desc)) {
					cell_desc = cell;
				}

				String cell_x = sampleItem.getCell();
				if (cell_x.contains("tumor")) {
					cell_desc += " tumor";
				}
				if (cell_x.contains("normal")) {
					cell_desc += " normal";
				}
				if (cell_x.contains("control")) {
					cell_desc += " control";
				}
				sampleItem.setCell_desc(cell_desc);
			}
		}
		map.put("result", result);
		map.put("genome", genome);
		if (!isLink) {
			RequestUtils.setAttribute("geneItemList", result.getGeneItemList());
			logger.debug("isLink: {}, find geneItemList size is: {}", isLink,
					result.getGeneItemList().size());
		}
		@SuppressWarnings("unchecked")
		List<GeneItem> geneItemList = (List<GeneItem>) RequestUtils.getAttribute("geneItemList");
		if (geneItemList == null) {
			map.put("geneSize", 0);
		} else {
			map.put("geneSize", geneItemList.size());
		}
		int beginPage = page > 6 ? page - 5 : 1;
		int endPage = totalPage;
		if (totalPage > 10 && totalPage > page + 5) {
			endPage = page > 6 ? page + 5 : 10;
		}
		if (isHistoryResult) {
			beginPage = 1;
			endPage = totalPage;
		} else {
			if (result.getCurrent() != null) {
				GeneRankCriteria criteria = new GeneRankCriteria();
				criteria.setGeneId(result.getCurrent().getGeneId());
				String ucscUrl = criteria
						.generateKey(GeneRankCriteria.ImageFileTempalte);
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
		map.put("geneCell", geneCell);
		map.put("geneDetail",geneDetail);
		Cookie isRemembercookie = RequestUtils.getCookie("isRemember");
		String isremember = isRemembercookie == null ? "false"
				: isRemembercookie.getValue();
		map.put("isRemember", isremember);
		Cookie usernamecookie = RequestUtils.getCookie("userName");
		Cookie pwdcookie = RequestUtils.getCookie("userPassword");
		if (usernamecookie != null && pwdcookie != null) {
			map.put("cookiename", usernamecookie.getValue());
			map.put("cookiepassword", pwdcookie.getValue());
		}
		List<TxrRef> trList = TxrRefCache.getInstance().getTxrRefBySymbol(geneSymbol);
		if(CollectionUtils.isNotEmpty(trList)){
			TxrRef tr = trList.get(0);
			if(tr.getAlias()!=null){
				map.put("alias", tr.getAlias());
			}
		}
		
		map.put("isLink", isLink);
		map.put("isHistory", isHistory);
		map.put("isHistoryResult", isHistoryResult);
		map.put("pageSize", pageSize);
		if (logger.isDebugEnabled() && geneItemList != null) {
			logger.debug("at last: isLink: {}, find geneItemList size is: {}",
					isLink, geneItemList.size());
		}
		Integer [] counts = searchService.findProcessedAndInProcessSample("geneSearchAdvance");
		map.put("processed",counts[0]);
		map.put("inProcess", counts[1]);
		return "result_geneSearchAdvanced";
	}
	
	@RequestMapping("toGenatlas.htm")
	public String toGenatlas(ModelMap map, HttpServletRequest request) {
		String geneSymbol = (String) request.getParameter("geneSymbol");
		map.put("mot", geneSymbol);
		return "toGenatlas";
	}
	
	@RequestMapping("goPubmed.htm")
	public String goPubmed(ModelMap map,HttpServletRequest request){
		String geneSymbol = (String) request.getParameter("geneSymbol");
		map.put("u1", geneSymbol);
		return "toGopubmed";
	}
	
	@RequestMapping("hInvDB.htm")
	public String hInvDB(ModelMap map,HttpServletRequest request){
		String geneSymbol = (String) request.getParameter("geneSymbol");
		map.put("KEN_STR", geneSymbol);
		return "toHinvdb";
	}
	
	@RequestMapping("result_single.htm")
	public String result_single(ModelMap map,HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		int page = ServletRequestUtils.getIntParameter(request, "page", 1);
		String geneSymbol = (String) request.getParameter("geneSymbol");
		Boolean isLink = ServletRequestUtils.getBooleanParameter(request,
				"isLink", false);
		Boolean isHistory = ServletRequestUtils.getBooleanParameter(request,
				"isHistory", false);
		Boolean isHistoryResult = ServletRequestUtils.getBooleanParameter(
				request, "isHistoryResult", false);
		String genome =  request.getParameter("genome");
		if (!isLink) {
			RequestUtils.setAttribute("geneSymbol", geneSymbol);
		}
		String sort = (String) RequestUtils.getRequest().getParameter("sort");
		User user = (User) RequestUtils.getAttribute("user");
		if(user == null){
			return "redirect:/index.html";
		}
		if (user != null && user.getPageSize() != null) {
			pageSize = user.getPageSize();
		}
		SettingDTO settingDTO = new SettingDTO();
		String history_id = "";
		if (isHistory || isHistoryResult) {
			String keyword = (String) RequestUtils.getAttribute("geneSymbol");
			SearchHistory history = searchHistoryService.findByKeyword(keyword,
					user);
			history_id = history.get_id();
			constructSettinsData(settingDTO, null, history);
			String formateDate = DateUtils.format(history.getCreateDate(),
					"yyyy/MM/dd");
			map.put("date", formateDate);
		} else {
			constructSettinsData(settingDTO, user, null);
		}
		map.put("settingDTO", settingDTO);
		Map<String, Integer> sampleSumMap = SampleCache.getInstance()
				.getSampleSumMap();
		map.put("sampleSumMap", sampleSumMap);
		// find Sample by geneSymbol or refSeq
		SortType sortType = StringUtils.isEmpty(sort) ? SortType.ASC : SortType
				.parse(sort);
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
		// 判断是否是点击搜索结果
		Integer start = (page - 1) * pageSize;
		if (isHistoryResult) {
			result = searchHistoryService.searchSample(history_id, sourceList,
					experimentsList, sortType, start, pageSize);
		} else if (StringUtils.isNotBlank(geneSymbol)) {
			long begin = System.nanoTime();
			if(genome.equals("Human")){
				result = searchService.searchSample(geneSymbol, sourceList,
						experimentsList, sortType, start, pageSize);
			}
			else if(genome.equals("Mouse")){
				result = mouseSearchService.searchSample(geneSymbol, sourceList,
						experimentsList, sortType, start, pageSize);
			}
			result.setUsedTime(begin, System.nanoTime());
		}
		int totalRecords = result.getTotal();
		int totalPage = totalRecords % pageSize == 0 ? totalRecords / pageSize
				: (totalRecords / pageSize) + 1;
		if (page > totalPage) {
			int total_all = result.getTotal_all();
			result = new SampleResult();
			result.setTotal_all(total_all);
		}
		Map<String, String> fdMap = factorDescService.findAll();
		Map<String, String> cdMap = cellTypeDescService.findAll();
		if (result.getSampleItemList() != null) {
			for (SampleItem sampleItem : result.getSampleItemList()) {
				String factor = sampleItem.getFactor();
				String factor_desc = fdMap.get(factor);
		
				if ((factor_desc == null || "".equals(factor_desc)) && factor != null) {
					factor_desc = factorDescService.findByFactorRegex(factor);
				}
				
				if (factor_desc == null || "".equals(factor_desc)) {
					factor_desc = sampleItem.getDetail();
				}
				
				sampleItem.setFactor_desc(factor_desc);
			}

			for (SampleItem sampleItem : result.getSampleItemList()) {
				String cell = sampleItem.getCell();
				if (cell == null || "".equals(cell) || "Summary Track".equals(sampleItem.getDataType()) || "Somatic Mutations".equals(sampleItem.getDataType())) {
					sampleItem.setCell_desc(cell);
					continue;
				}
				cell = cell.replace("-tumor", "").replace("-normal", "")
						.replace("-control", "");
				String cell_desc = cdMap.get(cell);

				if (cell_desc == null || "".equals(cell_desc)) {
					cell_desc = cellTypeDescService.findByCellRegex(cell);
					if (cell_desc == null || "".equals(cell_desc)) {
						cell_desc = cell;
					}
				}

				String cell_x = sampleItem.getCell();
				if (cell_x.contains("tumor")) {
					cell_desc += " tumor";
				}
				if (cell_x.contains("normal")) {
					cell_desc += " normal";
				}
				if (cell_x.contains("control")) {
					cell_desc += " control";
				}
				sampleItem.setCell_desc(cell_desc);
			}
		}
		map.put("result", result);
		if (!isLink) {
			RequestUtils.setAttribute("geneItemList", result.getGeneItemList());
			logger.debug("isLink: {}, find geneItemList size is: {}", isLink,
					result.getGeneItemList().size());
		}
		@SuppressWarnings("unchecked")
		List<GeneItem> geneItemList = (List<GeneItem>) RequestUtils.getAttribute("geneItemList");
		if (geneItemList == null) {
			map.put("geneSize", 0);
		} else {
			map.put("geneSize", geneItemList.size());
		}
		int beginPage = page > 6 ? page - 5 : 1;
		int endPage = totalPage;
		if (totalPage > 10 && totalPage > page + 5) {
			endPage = page > 6 ? page + 5 : 10;
		}
		if (isHistoryResult) {
			beginPage = 1;
			endPage = totalPage;
		} else {
			if (result.getCurrent() != null) {
				GeneRankCriteria criteria = new GeneRankCriteria();
				criteria.setGeneId(result.getCurrent().getGeneId());
				String ucscUrl = criteria
						.generateKey(GeneRankCriteria.ImageFileTempalte);
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
		String isremember = isRemembercookie == null ? "false"
				: isRemembercookie.getValue();
		map.put("isRemember", isremember);
		Cookie usernamecookie = RequestUtils.getCookie("userName");
		Cookie pwdcookie = RequestUtils.getCookie("userPassword");
		if (usernamecookie != null && pwdcookie != null) {
			map.put("cookiename", usernamecookie.getValue());
			map.put("cookiepassword", pwdcookie.getValue());
		}
		List<TxrRef> trList = TxrRefCache.getInstance().getTxrRefBySymbol(geneSymbol);
		if(CollectionUtils.isNotEmpty(trList)){
			TxrRef tr = trList.get(0);
			if(tr.getAlias()!=null){
				map.put("alias", tr.getAlias());
			}
		}
		
		map.put("isLink", isLink);
		map.put("isHistory", isHistory);
		map.put("isHistoryResult", isHistoryResult);
		map.put("pageSize", pageSize);
		if (logger.isDebugEnabled() && geneItemList != null) {
			logger.debug("at last: isLink: {}, find geneItemList size is: {}",
					isLink, geneItemList.size());
		}
		Integer [] counts = searchService.findProcessedAndInProcessSample("gene");
		map.put("processed",counts[0]);
		map.put("inProcess", counts[1]);
		return "result_single";
	}
	
	@RequestMapping("result_variation.htm")
	public String result_variation(ModelMap map, HttpServletRequest request)
			throws Throwable {
		int page = ServletRequestUtils.getIntParameter(request, "page", 1);
		String variationGene = (String) request.getParameter("variationGene");
		Boolean isLink = ServletRequestUtils.getBooleanParameter(request,
				"isLink", false);
		Boolean isHistory = ServletRequestUtils.getBooleanParameter(request,
				"isHistory", false);
		Boolean isHistoryResult = ServletRequestUtils.getBooleanParameter(
				request, "isHistoryResult", false);
		String genome = (String) request.getParameter("genome") == null ? "Human" : (String) request.getParameter("genome"); 
		map.put("genome", genome);
		if (!isLink) {
			RequestUtils.setAttribute("variationGene", variationGene);
		}
		String sort = (String) RequestUtils.getRequest().getParameter("sort");
		User user = (User) RequestUtils.getAttribute("user");
		if(user == null){
			return "redirect:/index.html";
		}
		if (user.getPageSize() != null) {
			pageSize = user.getPageSize();
		}
		
		if(user.getUserType() != null)
		{
			if(user.getUserType() > 1){
				pageSize = 20;
				page = 1;
			}
		}
		
		SettingDTO settingDTO = new SettingDTO();
		String history_id = "";
		if (isHistory || isHistoryResult) {
			String keyword = (String) RequestUtils.getAttribute("geneSymbol");
			SearchHistory history = searchHistoryService.findByKeyword(keyword,
					user);
			history_id = history.get_id();
			constructSettinsData(settingDTO, null, history);
			String formateDate = DateUtils.format(history.getCreateDate(),
					"yyyy/MM/dd");
			map.put("date", formateDate);
		} else {
			constructSettinsData(settingDTO, user, null);
		}
		map.put("settingDTO", settingDTO);
		Map<String, Integer> sampleSumMap = SampleCache.getInstance()
				.getSampleSumMap();
		map.put("sampleSumMap", sampleSumMap);
		Map<String, Integer> mouseSampleSumMap = SampleCache.getInstance().getMouseSampleSumMap();
		map.put("mouseSampleSumMap", mouseSampleSumMap);
		// find Sample by geneSymbol or refSeq
		SortType sortType = StringUtils.isEmpty(sort) ? SortType.ASC : SortType
				.parse(sort);
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
		// 判断是否是点击搜索结果
		Integer start = (page - 1) * pageSize;
		if (isHistoryResult) {
//			result = searchHistoryService.searchSample(history_id, sourceList,
//					experimentsList, sortType, start, pageSize);
		} else if (StringUtils.isNotBlank(variationGene)) {
			long begin = System.nanoTime();
			result = variationSearchService.searchSample(variationGene, sourceList,
						experimentsList, sortType, start, pageSize);
			result.setUsedTime(begin, System.nanoTime());
		}
		int totalRecords = result.getTotal();
		int totalPage = totalRecords % pageSize == 0 ? totalRecords / pageSize
				: (totalRecords / pageSize) + 1;
		if (page > totalPage) {
			int total_all = result.getTotal_all();
//			result = new SampleResult();
			result.setTotal_all(total_all);
		}
		Map<String, String> fdMap = factorDescService.findAll();
		Map<String, String> cdMap = cellTypeDescService.findAll();
		if (result.getSampleItemList() != null) {
			for (SampleItem sampleItem : result.getSampleItemList()) {
				String factor = sampleItem.getFactor();
				String factor_desc = fdMap.get(factor);
		
				if ((factor_desc == null || "".equals(factor_desc)) && factor != null) {
					factor_desc = factorDescService.findByFactorRegex(factor);
				}
				
				if (factor_desc == null || "".equals(factor_desc)) {
					factor_desc = sampleItem.getDetail();
				}
				
				sampleItem.setFactor_desc(factor_desc);
			}

			for (SampleItem sampleItem : result.getSampleItemList()) {
				String cell = sampleItem.getCell();
				if (cell == null || "".equals(cell) || "Summary Track".equals(sampleItem.getDataType()) || "Somatic Mutations".equals(sampleItem.getDataType())) {
					sampleItem.setCell_desc(cell);
					continue;
				}
				cell = cell.replace("-tumor", "").replace("-normal", "")
						.replace("-control", "");
				String cell_desc = cdMap.get(cell);

				if (cell_desc == null || "".equals(cell_desc)) {
					cell_desc = cellTypeDescService.findByCellRegex(cell);
					if (cell_desc == null || "".equals(cell_desc)) {
						cell_desc = cell;
					}
				}

				String cell_x = sampleItem.getCell();
				if (cell_x.contains("tumor")) {
					cell_desc += " tumor";
				}
				if (cell_x.contains("normal")) {
					cell_desc += " normal";
				}
				if (cell_x.contains("control")) {
					cell_desc += " control";
				}
				sampleItem.setCell_desc(cell_desc);
			}
		}
		map.put("result", result);
		if (!isLink) {
			RequestUtils.setAttribute("geneItemList", result.getGeneItemList());
			logger.debug("isLink: {}, find geneItemList size is: {}", isLink,
					result.getGeneItemList().size());
		}
		List<String> relKeyList = new ArrayList<String>();
		if (result.getCurrent() != null) {
			String relKey = result.getCurrent().getRelKey();
			if(StringUtils.isNotBlank(relKey))
			{
				String[] keys = relKey.split(",");
				for(int i=0; i<keys.length; i++)
				{
					relKeyList.add(keys[i]);
				}
			}
		}
		map.put("relKeyList", relKeyList);
		@SuppressWarnings("unchecked")
		List<GeneItem> geneItemList = (List<GeneItem>) RequestUtils.getAttribute("geneItemList");
		if (geneItemList == null) {
			map.put("geneSize", 0);
		} else {
			map.put("geneSize", geneItemList.size());
		}
		int beginPage = page > 6 ? page - 5 : 1;
		int endPage = totalPage;
		if (totalPage > 10 && totalPage > page + 5) {
			endPage = page > 6 ? page + 5 : 10;
		}
		if (isHistoryResult) {
			beginPage = 1;
			endPage = totalPage;
		} else {
			if (result.getCurrent() != null) {
				GeneRankCriteria criteria = new GeneRankCriteria();
				criteria.setGeneId(result.getCurrent().getGeneId());
				String ucscUrl = criteria
						.generateKey(GeneRankCriteria.ImageFileTempalte);
				map.put("ucscUrl", ucscUrl);
			}
		}
		map.put("totalPage", totalPage);
		map.put("page", page);
		map.put("beginPage", beginPage);
		map.put("endPage", endPage);
		map.put("variationGene", variationGene);
		map.put("totalRecords", totalRecords);
		map.put("sortType", sortType.name());
		Cookie isRemembercookie = RequestUtils.getCookie("isRemember");
		String isremember = isRemembercookie == null ? "false"
				: isRemembercookie.getValue();
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
			logger.debug("at last: isLink: {}, find geneItemList size is: {}",
					isLink, geneItemList.size());
		}
		Integer [] counts = searchService.findProcessedAndInProcessSample("variation");
		map.put("processed_human",counts[0]);
		map.put("inProcess_human", counts[1]);
		map.put("processed_mouse",counts[2]);
		map.put("inProcess_mouse", counts[3]);
		return "result_variation";
	}
	
	@RequestMapping("result_variations.htm")
	public String result_variations(ModelMap map, HttpServletRequest request)
			throws Throwable {
		int page = ServletRequestUtils.getIntParameter(request, "page", 1);
		String variationIds = (String) request.getParameter("variations");
		String mulVariants = "";
		if (variationIds != null) {
			if(variationIds.endsWith(",")){
				variationIds = variationIds.substring(0, variationIds.length() - 1);
			}
			
			String[] genes = variationIds.split(",");
			for (String s : genes) {
				mulVariants += s + "\n";
			}
			map.put("multiVariants", mulVariants);
		}
		Boolean isLink = ServletRequestUtils.getBooleanParameter(request,
				"isLink", false);
		Boolean isHistory = ServletRequestUtils.getBooleanParameter(request,
				"isHistory", false);
		Boolean isHistoryResult = ServletRequestUtils.getBooleanParameter(
				request, "isHistoryResult", false);
		if (!isLink) {
			RequestUtils.setAttribute("multiVariants", mulVariants);
		}
		String sort = (String) RequestUtils.getRequest().getParameter("sort");
		User user = (User) RequestUtils.getAttribute("user");
		if(user == null){
			return "redirect:/index.html";
		}
		if(user.getUserType() > 1){
			return "redirect:/result.htm";
		}
		if (user.getPageSize() != null) {
			pageSize = user.getPageSize();
		}
		SettingDTO settingDTO = new SettingDTO();
		if (isHistory || isHistoryResult) {
			// String keyword = (String)
			// RequestUtils.getAttribute("geneSymbol");
			// SearchHistory history =
			// searchHistoryService.findByKeyword(keyword);
			// history_id = history.get_id();
			// constructSettinsData(settingDTO, null, history);
			// String formateDate = DateUtils.format(history.getCreateDate(),
			// "yyyy/MM/dd");
			// map.put("date", formateDate);
		} else {
			constructSettinsData(settingDTO, user, null);
		}
		map.put("settingDTO", settingDTO);
		Map<String, Integer> sampleSumMap = SampleCache.getInstance()
				.getSampleSumMap();
		map.put("sampleSumMap", sampleSumMap);
		// find Sample by geneSymbol or refSeq
		SortType sortType = StringUtils.isEmpty(sort) ? SortType.ASC : SortType
				.parse(sort);
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
		// 判断是否是点击搜索结果
		Integer start = (page - 1) * pageSize;
		if (isHistoryResult) {
			// result = searchHistoryService.searchSample(history_id,
			// sourceList, experimentsList, sortType, start, pageSize);
		} else if (StringUtils.isNotBlank(variationIds)) {
			long begin = System.nanoTime();
			// result = searchService.searchSample(geneSymbol, sourceList,
			// experimentsList, sortType, start, pageSize);
			result = variationSearchService.searchSampleByVariationGenes(variationIds, sourceList, experimentsList, sortType, start, pageSize);
			result.setUsedTime(begin, System.nanoTime());
		}
		int totalRecords = result.getTotal();
		int totalPage = totalRecords % pageSize == 0 ? totalRecords / pageSize
				: (totalRecords / pageSize) + 1;
		if (page > totalPage) {
			result = new SampleResult();
		}
		Map<String, String> fdMap = factorDescService.findAll();
		Map<String, String> cdMap = cellTypeDescService.findAll();
		if (result.getSampleItemList() != null) {
			for (SampleItem sampleItem : result.getSampleItemList()) {
				String factor = sampleItem.getFactor();
				if (factor == null || "".equals(factor)) {
					sampleItem.setFactor_desc(sampleItem.getDetail());
					continue;
				}
				String factor_desc = fdMap.get(factor);
				if (factor_desc == null || "".equals(factor_desc)) {
					factor_desc = factorDescService.findByFactorRegex(factor);
					if (factor_desc == null || "".equals(factor_desc)) {
						factor_desc = sampleItem.getDetail();
					}
				}
				sampleItem.setFactor_desc(factor_desc);
			}

			for (SampleItem sampleItem : result.getSampleItemList()) {
				String cell = sampleItem.getCell();
				if (cell == null || "".equals(cell) || "Summary Track".equals(sampleItem.getDataType()) || "Somatic Mutations".equals(sampleItem.getDataType())) {
					sampleItem.setCell_desc(cell);
					continue;
				}
				cell = cell.replace("-tumor", "").replace("-normal", "")
						.replace("-control", "");
				String cell_desc = cdMap.get(cell);

				if (cell_desc == null || "".equals(cell_desc)) {
					cell_desc = cellTypeDescService.findByCellRegex(cell);
					if (cell_desc == null || "".equals(cell_desc)) {
						cell_desc = cell;
					}
				}

				String cell_x = sampleItem.getCell();
				if (cell_x.contains("tumor")) {
					cell_desc += " tumor";
				}
				if (cell_x.contains("normal")) {
					cell_desc += " normal";
				}
				if (cell_x.contains("control")) {
					cell_desc += " control";
				}
				sampleItem.setCell_desc(cell_desc);
			}
		}
		map.put("result", result);
		if (!isLink) {
			RequestUtils.setAttribute("geneItemList", result.getGeneItemList());
			logger.debug("isLink: {}, find geneItemList size is: {}", isLink,
					result.getGeneItemList().size());
		}
		@SuppressWarnings("unchecked")
		List<GeneItem> geneItemList = (List<GeneItem>) RequestUtils.getAttribute("geneItemList");
		if (geneItemList == null) {
			map.put("geneSize", 0);
		} else {
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
		} else {
			if (result.getCurrent() != null) {
				GeneRankCriteria criteria = new GeneRankCriteria();
				criteria.setGeneId(result.getCurrent().getGeneId());
				String ucscUrl = criteria
						.generateKey(GeneRankCriteria.ImageFileTempalte);
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
		String isremember = isRemembercookie == null ? "false"
				: isRemembercookie.getValue();
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
			logger.debug("at last: isLink: {}, find geneItemList size is: {}",
					isLink, geneItemList.size());
		}
		Integer [] counts = searchService.findProcessedAndInProcessSample("variations");
		map.put("processed",counts[0]);
		map.put("inProcess", counts[1]);
		return "result_variations";
	}
}
	
	