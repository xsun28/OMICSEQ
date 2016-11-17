package com.omicseq.robot.parse;

/**
 * 
 * 
 * @author zejun.du
 */
public interface IParser {

    /**
     * 解析 document
     * 
     * @param url
     */
    void parser(String url);

    /**
     * 处理需要下载的内容
     * 
     * @param url
     */
    void process(String url);

}
