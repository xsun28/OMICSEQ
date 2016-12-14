package com.omicseq.web.controller;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.omicseq.common.Charsets;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;

/**
 * 
 * 
 * @author zejun.du
 */
@Controller
@RequestMapping("/logger/")
public class LoggerController extends BaseController {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(LoggerController.class);
    private static LoggerContext lc = null;
    static {
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        if (factory instanceof LoggerContext) {
            lc = (LoggerContext) factory;
        }
    }

    @RequestMapping("level.json")
    @ResponseBody
    public Object level(String name, String level) {
        if (null != lc) {
            Logger _log = lc.getLogger(name);
            _log.setLevel(Level.toLevel(level, Level.OFF));
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("change [%s] set new level is [%s] ", name, level));
            }
            return JsonSuccess("success");
        } else {
            return JsonResult(null, "LoggerContext is empty !");
        }
    }

    @RequestMapping("newFile.json")
    @ResponseBody
    public Object newFile(String file, String loggerName, String level) {
        Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        logger.detachAndStopAllAppenders();
        // 创建 FileAppender
        FileAppender<ILoggingEvent> appender = new FileAppender<ILoggingEvent>();
        appender.setFile(file);
        appender.setContext(loggerContext);
        // 同步处理
        appender.setPrudent(true);
        // 创建 encode
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setCharset(Charsets.UTF_8);
        encoder.setPattern("%n[%level] - %date - %logger#%line%n - %msg%n");
        encoder.start();

        appender.setEncoder(encoder);
        appender.start();

        logger.addAppender(appender);
        logger.setLevel(Level.toLevel(level, Level.ERROR));
        logger.setAdditive(false);
        return JsonSuccess("success");
    }
}
