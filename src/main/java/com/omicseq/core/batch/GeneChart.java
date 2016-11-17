package com.omicseq.core.batch;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.joda.time.DateTime;

import com.omicseq.concurrent.ThreadTaskPoolsFactory;
import com.omicseq.concurrent.WaitFutureTask;
import com.omicseq.core.PropertiesHolder;
import com.omicseq.store.criteria.GeneRankCriteria;
import com.omicseq.utils.DateTimeUtils;

public class GeneChart extends BaseGeneRankBatch {
    private static GeneChart single = new GeneChart();
    private int width = Integer.valueOf(PropertiesHolder.get(PropertiesHolder.FILES, "img.gene.width", "300"));
    private int height = Integer.valueOf(PropertiesHolder.get(PropertiesHolder.FILES, "img.gene.height", "150"));
    /**
     * 设置jfreechart 主题样式
     */
    static {
        StandardChartTheme theme = new StandardChartTheme("CN");
        // 创建主题样式
        // 设置标题字体
        theme.setExtraLargeFont(new Font("隶书", Font.BOLD, 12));
        // 设置图例的字体
        theme.setRegularFont(new Font("宋书", Font.PLAIN, 12));
        // 设置轴向的字体
        theme.setLargeFont(new Font("宋书", Font.PLAIN, 12));
        // 白色背景
        theme.setPlotBackgroundPaint(Color.white);
        // 应用主题样式
        ChartFactory.setChartTheme(theme);
    }

    private GeneChart() {
    }

    static class ChartCallable extends BaseCallable<Object, GeneChart> {
        public ChartCallable(GeneChart ref, List<GeneRankCriteria> criteries) {
            super(ref, criteries);
        }

        @Override
        public Object call() throws Exception {
            DateTime dt = DateTime.now();
            try {
                ref.start();
                for (final GeneRankCriteria criteria : criteries) {
                    List<Integer> geneIds = geneCache.getGeneIds();
                    if (single.logger.isDebugEnabled()) {
                        single.logger.debug("需要生成{}条图片数据.", geneIds.size());
                    }
                    Collections.sort(geneIds);
                    int thread = 8;
                    final BlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>(geneIds);
                    Semaphore semaphore = new Semaphore(thread);
                    List<WaitFutureTask<Object>> tasks = new ArrayList<WaitFutureTask<Object>>(thread);
                    Callable<Object> callable = new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            while (true) {
                                Integer geneId = queue.poll();
                                criteria.setGeneId(geneId);
                                DateTime dt = DateTime.now();
                                ref.chart(criteria, single.width, single.height);
                                if (single.logger.isDebugEnabled()) {
                                    single.logger.debug("生成{}图片,用时:{}", geneId, DateTimeUtils.used(dt));
                                }
                                if (queue.isEmpty()) {
                                    break;
                                }
                            }
                            return null;
                        }
                    };
                    for (int i = 0; i < thread; i++) {
                        tasks.add(new WaitFutureTask<Object>(callable, semaphore));
                    }
                    ThreadTaskPoolsFactory.getThreadTaskPoolsExecutor().blockRun(tasks, 10l, TimeUnit.DAYS);
                }
            } catch (Exception e) {
                ref.logger.error("生成图片文件出错:", e);
            } finally {
                ref.stop();
                single.logger.debug("生成Excel用时:{}", DateTimeUtils.used(dt));
            }
            return Boolean.TRUE;
        }
    }

    protected void chart(GeneRankCriteria criteria, int width, int height) {
        String fname = criteria.generateKey(GeneRankCriteria.ImageFileTempalte);
        try {
            File root = new File(PropertiesHolder.get(PropertiesHolder.FILES, "img.gene", "./"));
            File file = new File(root, fname);
            if (file.exists()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("gene chart file {} exits.", file.getAbsolutePath());
                }
                file.delete();
            }
            FileUtils.forceMkdir(file.getParentFile());
            criteria.setMixturePerc(0.01);
            List<Double> coll = geneRankDAO.percentile(criteria);
            // [idx][val/]
            XYSeriesCollection dataset = new XYSeriesCollection();
            XYSeries localXYSeries1 = new XYSeries("Datasets", true);
            XYSeries localXYSeries2 = new XYSeries("Reference Line", true);
            localXYSeries2.add(0, 0);
            for (int i = 1; i <= coll.size(); i++) {
                int val = (int) (coll.get(i - 1) * 100);
                localXYSeries1.add(i, val);
                if (i == coll.size()) {
                    localXYSeries2.add(i, val);
                }
            }
            dataset.addSeries(localXYSeries1);
            dataset.addSeries(localXYSeries2);
            String title = "Cumulative Curve of Percentile";
            JFreeChart chart = ChartFactory.createXYLineChart(title, "Number of Datasets", "Percentile(%)", dataset);
            ChartUtilities.applyCurrentTheme(chart);
            XYPlot plot = (XYPlot) chart.getPlot();
            // plot.setBackgroundAlpha(0.5f);
            // 网格线颜色
            plot.setDomainGridlinePaint(Color.black);
            plot.setRangeGridlinePaint(Color.black);
            XYItemRenderer renderer = plot.getRenderer();
            renderer.setSeriesPaint(0, Color.blue);
            renderer.setSeriesPaint(1, Color.red);
            renderer.setSeriesStroke(0, new BasicStroke(0.5f, 2, 2, 10f));
            if (logger.isDebugEnabled()) {
                logger.debug("生成图片文件 :{}", file.getAbsolutePath());
            }
            OutputStream out = new FileOutputStream(file);
            ChartUtilities.writeChartAsPNG(out, chart, width, height);
        } catch (Exception e) {
            logger.error("生成图片文件{" + fname + "}出错", e);
        }
    }

    protected Callable<Object> getCallable(List<GeneRankCriteria> criteries) {
        return new ChartCallable(new GeneChart(), criteries);
    }

    public static void main(String[] args) {
        geneCache.init();
        single.refresh();
    }

    public static GeneChart getInstance() {
        return single;
    }

}
