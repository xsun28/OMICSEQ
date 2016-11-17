package com.omicseq.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.web.common.WebServiceResult;

public class BaseController {

    // 日志
    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 前台json数据返回成功
     * 
     * @param data
     * @return
     */
    protected WebServiceResult JsonSuccess(Object data) {
        if (logger.isDebugEnabled()) {
            logger.debug("return data is " + data);
        }
        return JsonResult(data, null);
    }

    /**
     * 前台json数据返回失败
     * 
     * @param ex
     * @return
     */
    protected WebServiceResult JsonFailed(Exception ex) {
        logger.error(ex.getMessage(), ex);
        WebServiceResult result = new WebServiceResult();
        result.setResult(false);
        result.setMessage(ex.getLocalizedMessage());
        return result;
    }

    /**
     * 前台json数据返回
     * 
     * @param data
     * @param msg
     * @return
     */
    protected WebServiceResult JsonResult(Object data, String msg) {
        WebServiceResult result = new WebServiceResult();
        if (data == null) {
            result.setResult(false);
        }
        result.setMessage(msg);
        result.setData(data);
        if (logger.isDebugEnabled()) {
            logger.debug("return  is " + result);
        }
        return result;
    }
    
    protected WebServiceResult JsonResult(Object data, String msg, String code) {
        WebServiceResult result = new WebServiceResult();
        if (data == null) {
            result.setResult(false);
        }
        result.setMessage(msg);
        result.setData(data);
        result.setCode(code);
        if (logger.isDebugEnabled()) {
            logger.debug("return  is " + result);
        }
        return result;
    }

}
