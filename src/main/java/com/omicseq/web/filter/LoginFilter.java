package com.omicseq.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/** 
* 类名称：LoginFilter 
* 类描述： 登录验证Filter
* 
* 
* 创建人：Liangxiaoyan
* 创建时间：2014-3-22 下午2:54:53 
* @version 
* 
*/
public class LoginFilter implements Filter  {
    private static String loginUrl = "user/login.htm";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest request = (HttpServletRequest)arg0; 
        HttpServletResponse response = (HttpServletResponse)arg1; 
        HttpSession session = request.getSession(); 
       if(session.getAttribute("user")== null){ 
           if(request.getRequestURI().indexOf("login.htm") != -1 || request.getRequestURI().indexOf("index.htm") != -1) {
               response.sendRedirect(request.getContextPath() + loginUrl);
               return ;
           }
       } 
       chain.doFilter(arg0, arg1); 
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
        
    }


}
