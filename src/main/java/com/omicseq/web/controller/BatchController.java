package com.omicseq.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.omicseq.core.batch.GeneChart;
import com.omicseq.core.batch.GeneRankExport;

@Controller
@RequestMapping("/batch/")
public class BatchController extends BaseController {

    @RequestMapping("chart.json")
    @ResponseBody
    public Object images() {
        GeneChart instance = GeneChart.getInstance();
        if (instance.isRunning()) {
            return JsonSuccess("生成图片线程执行中...");
        } else {
            instance.refresh();
            return JsonSuccess(true);
        }
    }

    @RequestMapping("exp.json")
    @ResponseBody
    public Object export() {
        GeneRankExport instance = GeneRankExport.getInstance();
        if (instance.isRunning()) {
            return JsonSuccess("生成文件,线程执行中...");
        } else {
            instance.refresh();
            return JsonSuccess(true);
        }
    }
}
