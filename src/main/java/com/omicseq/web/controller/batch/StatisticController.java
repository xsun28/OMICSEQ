package com.omicseq.web.controller.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.omicseq.bean.Paginator;
import com.omicseq.common.SourceType;
import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.domain.StatisticInfo;
import com.omicseq.exception.OmicSeqException;
import com.omicseq.robot.exec.DownLoad;
import com.omicseq.robot.exec.Process;
import com.omicseq.store.criteria.StatisticCriteria;
import com.omicseq.utils.MiscUtils;
import com.omicseq.web.controller.BaseController;
import com.omicseq.web.service.IStatisticService;

@Controller
@RequestMapping("/batch/stat/")
public class StatisticController extends BaseController {
    @Autowired
    private IStatisticService statisticService;

    @RequestMapping("download.json")
    @ResponseBody
    public Object download(Boolean stop) {
        try {
            final DownLoad instance = DownLoad.getInstance();
            if (Boolean.TRUE.equals(stop)) {
                if (instance.isRunning()) {
                    instance.stop();
                }
                return JsonSuccess("文件下载线程停止.");
            }
            if (instance.isRunning()) {
                return JsonSuccess("文件下载线程运行中..");
            } else {
                FutureTask<Boolean> task = new FutureTask<Boolean>(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {

                        instance.start();
                        return true;
                    }
                });
                ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor().run(task);
                return JsonSuccess(true);
            }
        } catch (Exception e) {
            return JsonFailed(e);
        }
    }

    @RequestMapping("init.json")
    @ResponseBody
    public Object init(Integer source) {
        try {
            Object data = statisticService.initInfo(SourceType.parse(source));
            return JsonSuccess(data);
        } catch (Exception e) {
            return JsonFailed(e);
        }
    }

    @RequestMapping("proc.json")
    @ResponseBody
    public Object proc(Boolean input, final Boolean refresh, Boolean stop) {
        final Process instance = Process.getInstance();
        if (Boolean.TRUE.equals(stop)) {
            if (instance.isRunning()) {
                instance.stop();
            }
            return JsonSuccess("解析文件,线程停止.");
        }
        if (instance.isRunning()) {
            return JsonSuccess("解析文件,线程执行中...");
        } else {
            instance.setInput(input);
            FutureTask<Boolean> task = new FutureTask<Boolean>(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    if (Boolean.TRUE.equals(refresh)) {
                        instance.refresh();
                    }
                    instance.start();
                    return true;
                }
            });
            ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor().run(task);
            return JsonSuccess(true);
        }
    }
    
    @RequestMapping("stopProc.json")
    @ResponseBody
    public Object stopProc(Boolean input, final Boolean refresh, Boolean stop) {
        final Process instance = Process.getInstance();
        instance.stop();
        return JsonSuccess(true);
    }

    @RequestMapping("index.htm")
    public String index(StatisticCriteria criteria, Paginator paginator, ModelMap map, HttpServletRequest req) {
        List<StatisticInfo> data = new ArrayList<StatisticInfo>(0);
        if (null != criteria.getSource() || CollectionUtils.isNotEmpty(criteria.getSources())) {
            criteria.setServerIp(MiscUtils.getServerIP());
            data = statisticService.findByCriteria(criteria, paginator);
        }
        map.put("sources", SourceType.values());
        map.put("data", data);
        return "batch/stat/index";
    }

    @RequestMapping("exec.json")
    @ResponseBody
    public Object exec(Integer sampleId) {
        try {
            if (null == sampleId) {
                throw new OmicSeqException("参数无效!");
            }
            statisticService.exec(sampleId);
            return JsonSuccess(true);
        } catch (Exception e) {
            return JsonFailed(e);
        }
    }

    @RequestMapping("check.json")
    @ResponseBody
    public Object check(Integer sampleId, Boolean all, Integer source) {
        try {
            if (null != sampleId) {
                statisticService.check(sampleId);
            } else if (null != source) {
                statisticService.check(all, SourceType.parse(source));
            } else {
                throw new OmicSeqException("参数无效!");
            }
            return JsonSuccess(true);
        } catch (Exception e) {
            return JsonFailed(e);
        }
    }
}
