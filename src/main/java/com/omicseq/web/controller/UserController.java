package com.omicseq.web.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.omicseq.common.ExperimentType;
import com.omicseq.common.SourceType;
import com.omicseq.domain.SearchHistory;
import com.omicseq.domain.User;
import com.omicseq.exception.OmicSeqException;
import com.omicseq.web.common.CompanyType;
import com.omicseq.web.common.WebServiceResult;
import com.omicseq.web.service.ISearchHistoryService;
import com.omicseq.web.service.IUserService;
import com.omicseq.web.serviceimpl.MessageSourceHelper;
import com.omicseq.web.utils.RequestUtils;

/**
 * 
 * 用户控制层
 * 
 * @author zejun.du
 */
@Controller
@RequestMapping("/user/")
public class UserController extends BaseController {
    @Autowired
    private IUserService userService;
    @Autowired
    private ISearchHistoryService searchHistoryService;
    @Autowired
    private MessageSourceHelper messageSourceHelper;

    @RequestMapping("toLogin.htm")
    public String toLogin(HttpServletRequest request) {
        return "loginpage";
    }

    @RequestMapping("login.json")
    @ResponseBody
    public Object login(HttpServletRequest request, HttpServletResponse response) {
        try {
            String userName = (String) request.getParameter("userName");
            String password = (String) request.getParameter("password");
            String isRemember = request.getParameter("isRemember");
            String idCardAndPhone = (String)request.getParameter("idCardAndPhone");
            User user = userService.login(userName, password);
            if (user != null) {
            	if(user.getUserType() == 99)
            	{
            		user.setIdCardAndPhone(idCardAndPhone);
                	user.setUserType(3);
                	user.setPassword(password);
                	userService.update(user);
            	}
            	
                RequestUtils.setAttribute("user", user);
                RequestUtils.setAttribute("isRemember", isRemember);
                String tempUserName = isRemember.equalsIgnoreCase("true") ? user.getName() : "";
                String tempPassword = isRemember.equalsIgnoreCase("true") ? user.getPassword() : "";
                RequestUtils.setCookie("userName", tempUserName, response);
                RequestUtils.setCookie("userPassword", tempPassword, response);
                RequestUtils.setCookie("jforumSSOCookieNameUser", userName + "," + password +"," + user.getUserType(), response);
                RequestUtils.setCookie("isRemember", isRemember, response);
                return JsonSuccess(user);
            } else {
                String message = messageSourceHelper.getMessage("error.login.userwrong");
                throw new OmicSeqException(message);
            }
        } catch (Exception e) {
            return JsonFailed(e);
        }
    }
    
    @RequestMapping("checkStatus.json")
    @ResponseBody
    public Object checkStatus(HttpServletRequest request, HttpServletResponse response) {
    	try {
            String userName = (String) request.getParameter("userName");
            String password = (String) request.getParameter("password");
            User user = userService.login(userName, password);
            if (user != null) {
//                RequestUtils.setAttribute("user", user);
//                RequestUtils.setAttribute("isRemember", isRemember);
//                String tempUserName = isRemember.equalsIgnoreCase("true") ? user.getName() : "";
//                String tempPassword = isRemember.equalsIgnoreCase("true") ? user.getPassword() : "";
//                RequestUtils.setCookie("userName", tempUserName, response);
//                RequestUtils.setCookie("userPassword", tempPassword, response);
//                RequestUtils.setCookie("jforumSSOCookieNameUser", userName + "," + password +"," + user.getUserType(), response);
//                RequestUtils.setCookie("isRemember", isRemember, response);
            	if(user.getUserType() == 99)
            	{
            		return JsonSuccess(user);
            	}else{
            		WebServiceResult result = new WebServiceResult();
                    result.setResult(false);
            		return result;
            	}
                
            } else {
                String message = messageSourceHelper.getMessage("error.login.userwrong");
                throw new OmicSeqException(message);
            }
        } catch (Exception e) {
            return JsonFailed(e);
        }
    }

    @RequestMapping("logout.json")
    @ResponseBody
    public Object logout(HttpServletResponse response) {
        RequestUtils.removeAttribute("user");
        Cookie cookie = new Cookie("jforumSSOCookieNameUser", null);
        cookie.setMaxAge(0); // delete the cookie.
        cookie.setPath("/");
        response.addCookie(cookie);
        return JsonSuccess(true);
    }

    @RequestMapping("toRegister.htm")
    public String toRegister(ModelMap map) {
        Cookie isRemembercookie = RequestUtils.getCookie("isRemember");
        String isremember = isRemembercookie == null ? "false" : isRemembercookie.getValue();
        map.put("isRemember", isremember);
        Cookie usernamecookie = RequestUtils.getCookie("userName");
        Cookie pwdcookie = RequestUtils.getCookie("userPassword");
        if (usernamecookie != null && pwdcookie != null) {
            map.put("cookiename", usernamecookie.getValue());
            map.put("cookiepassword", pwdcookie.getValue());
        }
        map.put("companyTypeMap", CompanyType.getDescMap());
        return "register";
    }

    @RequestMapping("register.json")
    @ResponseBody
    public Object register(User dto, HttpServletResponse response) {
        try {
            User user = userService.register(dto);
            if (user != null) {
                RequestUtils.setAttribute("user", user);
                RequestUtils.setCookie("jforumSSOCookieNameUser", dto.getName() + "," + dto.getPassword() + "," +dto.getUserType(), response);
                return JsonSuccess(user);
            } else {
                String message = messageSourceHelper.getMessage("failed.register");
                throw new OmicSeqException(message);
            }
        } catch (Exception e) {
            return JsonFailed(e);
        }
    }

    @RequestMapping("update.json")
    @ResponseBody
    public Object update(User user, HttpServletResponse response) {
        try {
            User temp = (User) RequestUtils.getAttribute("user");
            String experiments = user.getExperiments();
            if (StringUtils.isNoneEmpty(experiments)) {
                String[] expArray = experiments.split(",");
                StringBuilder sb = new StringBuilder();
                for (String exp : expArray) {
                    sb.append(ExperimentType.getType(exp).getValue()).append(",");
                }
                sb.delete(sb.length() - 1, sb.length());
                user.setExperiments(sb.toString());
            }
            String sources = user.getSource();
            if (StringUtils.isNoneEmpty(sources)) {
                String[] sourceArray = sources.split(",");
                StringBuilder sb = new StringBuilder();
                for (String source : sourceArray) {
                    sb.append(SourceType.getType(source).value()).append(",");
                }
                sb.delete(sb.length() - 1, sb.length());
                user.setSource(sb.toString());
            }
            String experiments_mouse = user.getExperiments_mouse();
            if (StringUtils.isNoneEmpty(experiments_mouse)) {
                String[] expArray = experiments_mouse.split(",");
                StringBuilder sb = new StringBuilder();
                for (String exp : expArray) {
                    sb.append(ExperimentType.getType(exp).getValue()).append(",");
                }
                sb.delete(sb.length() - 1, sb.length());
                user.setExperiments_mouse(sb.toString());
            }
            String sources_mouse = user.getSource_mouse();
            if (StringUtils.isNoneEmpty(sources_mouse)) {
                String[] sourceArray = sources_mouse.split(",");
                StringBuilder sb = new StringBuilder();
                for (String source : sourceArray) {
                    sb.append(SourceType.getType(source).value()).append(",");
                }
                sb.delete(sb.length() - 1, sb.length());
                user.setSource_mouse(sb.toString());
            }
            if (temp == null) {
            	RequestUtils.setCookie("experiments", user.getExperiments(), response);
                RequestUtils.setCookie("cellType", user.getCellType(), response);
            	RequestUtils.setCookie("experiments_mouse", user.getExperiments(), response);
                RequestUtils.setCookie("source_mouse", user.getSource(), response);
                RequestUtils.setCookie("source", user.getSource(), response);
            } else {
                user.setName(temp.getName());
                user.setUserId(temp.getUserId());
                user.setUserType(temp.getUserType());
                //temp = userService.update(user);
                RequestUtils.setAttribute("user", user);
            }
            return JsonSuccess(true);
        } catch (Exception e) {
            return JsonFailed(e);
        }
    }

    @RequestMapping("history.htm")
    public String history(ModelMap map, HttpServletRequest request) {
        String keyword = request.getParameter("keyword");
        User user = (User) RequestUtils.getAttribute("user");
        List<SearchHistory> histories = new ArrayList<SearchHistory>();
        if (user != null) {
            histories = searchHistoryService.findAll(user.getUserId(), keyword);
        }
        map.put("histories", histories);
        return "history";
    }

    @RequestMapping("saveHistory.json")
    @ResponseBody
    public Object saveHistory(String keyword, Integer geneId,String genome, HttpServletRequest request, HttpServletResponse response) {
        try {
            User user = (User) RequestUtils.getAttribute("user");
            if (user != null) {
                // 判断是否已存在该keyword的历史
                SearchHistory searchHistory = searchHistoryService.findByKeyword(keyword, user);
                if (searchHistory != null) {
                    String message = messageSourceHelper.getMessage("error.collection.override");
                    return JsonResult(null, message, "1001");
                }
                User tempUser = userService.findByUserName(user.getName());
                SearchHistory history = new SearchHistory();
                history.setKeyword(keyword);
                history.setUserId(tempUser.getUserId());
                history.setCellType(user.getCellType());
                history.setGenome(genome);
                if(genome.equals("Human")){
                	history.setExperiments(user.getExperiments());
                    history.setSource(user.getSource());
                }
                else if(genome.equals("Mouse")){
                	history.setExperiments_mouse(user.getExperiments_mouse());
                    history.setSource_mouse(user.getSource_mouse());
                }
                history.setCreateDate(new Date());
                history.setGeneId(geneId);
                searchHistoryService.saveOrUpdateSearchHistory(history);
                return JsonSuccess(true);
            } else {
                String message = messageSourceHelper.getMessage("error.collection");
                return JsonResult(null, message, "1000");
            }
        } catch (Exception e) {
            return JsonFailed(e);
        }
    }

    @RequestMapping("updateHistory.json")
    @ResponseBody
    public Object updateHistory(String keyword, Integer geneId, HttpServletRequest request, HttpServletResponse response) {
        try {
            User user = (User) RequestUtils.getAttribute("user");
            if (user != null) {
                SearchHistory searchHistory = searchHistoryService.findByKeyword(keyword, user);
                searchHistory.setCellType(user.getCellType());
                searchHistory.setExperiments(user.getExperiments());
                searchHistory.setSource(user.getSource());
                searchHistory.setCreateDate(new Date());
                searchHistory.setGeneId(geneId);
                searchHistoryService.saveOrUpdateSearchHistory(searchHistory);
                return JsonSuccess(true);
            } else {
                String message = messageSourceHelper.getMessage("error.collection");
                return JsonResult(null, message, "1000");
            }
        } catch (Exception e) {
            return JsonFailed(e);
        }
    }
    /*
     * 判断用户类型，功能使用限制
     */
    @RequestMapping("trialAccount.json")
    @ResponseBody
    public Object trialAccount(HttpServletRequest request, HttpServletResponse response) {
    	User user = (User) RequestUtils.getAttribute("user");
    	if(user == null){
    		return "redirect:/index.html";
    	}else{
    		if(user.getUserType() > 1){
    			String message = messageSourceHelper.getMessage("error.trialAccount");
    			return JsonResult(null, message, "1000");
    		}
    		return JsonSuccess(true);
    	}
    }
    
    @RequestMapping("showRegisterUniversity.htm")
    public String showRegisterUniversity(ModelMap map,HttpServletRequest request){
    	List<User> userList = userService.showRegisterUniversity();
    	map.put("users", userList);
		return "showRegisterUniversity";
    }
}
