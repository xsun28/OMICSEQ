package com.omicseq.web.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class RequestUtils {

    /** 
     * 获取IP地址
    * @param request
    * @return
    */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /** 
     * 设置值到当前会话
    * @param name
    * @param value
    */
    public static void setAttribute(String name, Object value) {
        setAttribute(name, value, RequestAttributes.SCOPE_SESSION);
    }

    /** 设置值到当前会话
    * @param name
    * @param value
    * @param scope
    */
    public static void setAttribute(String name, Object value, int scope) {
        ServletRequestAttributes attr = getRequestAttributes();
        if (null != attr) {
            attr.setAttribute(name, value, scope);
        }
    }

    /** 
     * 从当前会话获取值 
    * @param name
    * @return
    */
    public static Object getAttribute(String name) {
        return getAttribute(name, RequestAttributes.SCOPE_SESSION);
    }

    /** 从当前会话获取值 
    * @param name
    * @param scope
    * @return
    */
    public static Object getAttribute(String name, int scope) {
        return null == getRequestAttributes() ? null : getRequestAttributes().getAttribute(name, scope);
    }

    /** 
     * 删除会话值 
    * @param name
    * @return
    */
    public static Object removeAttribute(String name) {
        return removeAttribute(name, RequestAttributes.SCOPE_SESSION);
    }

    /** 
     *  删除会话值 
    * @param name
    * @param scope
    * @return
    */
    public static Object removeAttribute(String name, int scope) {
        ServletRequestAttributes attr = getRequestAttributes();
        Object result = null;
        if (null != attr) {
            result = attr.getAttribute(name, scope);
            attr.removeAttribute(name, scope);
        }
        return result;
    }

    /** 
     * 获取当前会话所有缓存值 
    * @return
    */
    public static ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        return (attrs instanceof ServletRequestAttributes) ? (ServletRequestAttributes) attrs : null;
    }

    /** 
     * 获取当前会话请求
    * @return
    */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attr = getRequestAttributes();
        return null == attr ? null : attr.getRequest();
    }

    /** 
     * 获取当前session
    * @return
    */
    public static HttpSession getSession() {
        ServletRequestAttributes attr = getRequestAttributes();
        return null == attr ? null : attr.getRequest().getSession(false);
    }

    /** 
     * 获得cookie
    * @param name
    * @return
    */
    public static Cookie getCookie(String name){
        Cookie[] cookies = RequestUtils.getRequest().getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                Cookie c = cookies[i];

                if (c.getName().equals(name)) {
                    return c;
                }
            }
        }
        return null;
    }
    /** 
     * 添加cookie
    * @param name
    * @param value
    * @param response
    */
    public static void setCookie(String name, String value, HttpServletResponse response){
        Cookie cookie = new Cookie(name,value);
        cookie.setPath("/");
        cookie.setMaxAge(Integer.MAX_VALUE);
        response.addCookie(cookie);
    }
}

