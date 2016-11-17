// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 

package com.omicseq.plot;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

// Referenced classes of package demo:
//			SampleXYDataset2

public class ScatterPlotDemo4 extends ApplicationFrame
{

	public ScatterPlotDemo4(String s)
	{
		super(s);
		JPanel jpanel = createDemoPanel();
		jpanel.setPreferredSize(new Dimension(500, 270));
		setContentPane(jpanel);
	}

	public static JPanel createDemoPanel()
	{	
		XYSeries  series = new XYSeries("1");
		series.add(1.0, 8);
		series.add(2, 7);
		series.add(3, 6);
		series.add(4, 5);
		series.add(5, 5);
		series.add(6, 4);
		series.add(7, 3);
		series.add(8, 1);
		XYSeriesCollection  c = new XYSeriesCollection();
		c.addSeries(series);
		XYDataset samplexydataset2 = c;
		JFreeChart jfreechart = ChartFactory.createScatterPlot("Scatter Plot Demo 4", "X", "Y", samplexydataset2,PlotOrientation.HORIZONTAL,false,false,false);
		XYPlot xyplot = (XYPlot)jfreechart.getPlot();
		xyplot.setRangeTickBandPaint(new Color(200, 200, 100, 100));
		XYDotRenderer xydotrenderer = new XYDotRenderer();xydotrenderer.setPaint(Color.BLACK);
		xydotrenderer.setDotWidth(4);
		xydotrenderer.setDotHeight(4);
		xyplot.setRenderer(xydotrenderer);
		xyplot.setDomainCrosshairVisible(true);
		xyplot.setRangeCrosshairVisible(true);
		NumberAxis numberaxis = (NumberAxis)xyplot.getDomainAxis();
		numberaxis.setAutoRangeIncludesZero(false);
		xyplot.getRangeAxis().setInverted(true);
		return new ChartPanel(jfreechart);
	}

	public static void main(String args[])
	{
		ScatterPlotDemo4 scatterplotdemo4 = new ScatterPlotDemo4("JFreeChart: ScatterPlotDemo4.java");
		scatterplotdemo4.pack();
		RefineryUtilities.centerFrameOnScreen(scatterplotdemo4);
		scatterplotdemo4.setVisible(true);
	}
}
