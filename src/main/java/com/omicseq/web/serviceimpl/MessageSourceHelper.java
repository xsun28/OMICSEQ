package com.omicseq.web.serviceimpl;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

@Service
public class MessageSourceHelper {
    @Autowired
    private MessageSource messageSource;

    public String getMessage(String code, Object[] args, String defaultMessage) {
        Locale locale = Locale.CHINA;
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes) {
            ServletRequestAttributes attr = (ServletRequestAttributes) attrs;
            HttpServletRequest req = attr.getRequest();
            locale = RequestContextUtils.getLocale(req);
        }
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }

    public String getMessage(String code, String defaultMessage) {
        return getMessage(code, null, defaultMessage);
    }

    public String getMessage(String code) {
        return getMessage(code, null, code);
    }

}
