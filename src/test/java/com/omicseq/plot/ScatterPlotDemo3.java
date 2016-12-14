// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 

package com.omicseq.plot;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.PrintStream;

import javax.swing.JPanel;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

// Referenced classes of package demo:
//			SampleXYDataset2

public class ScatterPlotDemo3 extends ApplicationFrame
{
	static class MyChartMouseListener
		implements ChartMouseListener
	{

		ChartPanel panel;

		public void chartMouseClicked(ChartMouseEvent chartmouseevent)
		{
			int i = chartmouseevent.getTrigger().getX();
			int j = chartmouseevent.getTrigger().getY();
			Point2D point2d = panel.translateScreenToJava2D(new Point(i, j));
			XYPlot xyplot = (XYPlot)panel.getChart().getPlot();
			ChartRenderingInfo chartrenderinginfo = panel.getChartRenderingInfo();
			java.awt.geom.Rectangle2D rectangle2d = chartrenderinginfo.getPlotInfo().getDataArea();
			double d = xyplot.getDomainAxis().java2DToValue(point2d.getX(), rectangle2d, xyplot.getDomainAxisEdge());
			double d1 = xyplot.getRangeAxis().java2DToValue(point2d.getY(), rectangle2d, xyplot.getRangeAxisEdge());
			ValueAxis valueaxis = xyplot.getDomainAxis();
			ValueAxis valueaxis1 = xyplot.getRangeAxis();
			double d2 = valueaxis.valueToJava2D(d, rectangle2d, xyplot.getDomainAxisEdge());
			double d3 = valueaxis1.valueToJava2D(d1, rectangle2d, xyplot.getRangeAxisEdge());
			Point point = panel.translateJava2DToScreen(new java.awt.geom.Point2D.Double(d2, d3));
			System.out.println("Mouse coordinates are (" + i + ", " + j + "), in data space = (" + d + ", " + d1 + ").");
			System.out.println("--> (" + point.getX() + ", " + point.getY() + ")");
		}

		public void chartMouseMoved(ChartMouseEvent chartmouseevent)
		{
		}

		public MyChartMouseListener(ChartPanel chartpanel)
		{
			panel = chartpanel;
		}
	}


	public ScatterPlotDemo3(String s)
	{
		super(s);
		JPanel jpanel = createDemoPanel();
		jpanel.setPreferredSize(new Dimension(500, 270));
		setContentPane(jpanel);
	}

	private static JFreeChart createChart(XYDataset xydataset)
	{
		JFreeChart jfreechart = ChartFactory.createScatterPlot("Scatter Plot", "X", "Y", xydataset, PlotOrientation.VERTICAL, true, true, false);
		XYPlot xyplot = (XYPlot)jfreechart.getPlot();
		xyplot.setDomainCrosshairVisible(true);
		xyplot.setDomainCrosshairLockedOnData(true);
		xyplot.setRangeCrosshairVisible(true);
		xyplot.setRangeCrosshairLockedOnData(true);
		xyplot.setDomainZeroBaselineVisible(true);
		xyplot.setRangeZeroBaselineVisible(true);
		xyplot.setDomainPannable(true);
		xyplot.setRangePannable(true);
		NumberAxis numberaxis = (NumberAxis)xyplot.getDomainAxis();
		numberaxis.setAutoRangeIncludesZero(false);
		return jfreechart;
	}

	public static JPanel createDemoPanel()
	{	
		XYSeriesCollection c = new XYSeriesCollection();
		XYSeries serie = new XYSeries("");
		double [] a =  {0.36,0.99,0.47,0.81,0.39,1.18,0.86,0.81,0.85,0.66,
				0.95,0.49,0.52,0.93,0.63,0.44,0.82,0.99,1.07,0.86};
		double [] b = {0.24,-0.08,0.18,0.01,0.22,-0.28,-0.01,-0.57,
			-0.55,-0.64,-0.06,-0.73,-0.71,-0.51,-0.66,-0.76,0.01,-0.08,-0.13,-0.55	
			};
		for(int i=0; i<a.length;i++){
			serie.add(b[i], a[i]);
		}
		c.addSeries(serie);
		
		JFreeChart jfreechart = createChart(c);
		ChartPanel chartpanel = new ChartPanel(jfreechart);
		chartpanel.setMouseWheelEnabled(true);
		chartpanel.addChartMouseListener(new MyChartMouseListener(chartpanel));
		return chartpanel;
	}

	public static void main(String args[])
	{
		ScatterPlotDemo3 scatterplotdemo3 = new ScatterPlotDemo3("JFreeChart: ScatterPlotDemo3.java");
		scatterplotdemo3.pack();
		RefineryUtilities.centerFrameOnScreen(scatterplotdemo3);
		scatterplotdemo3.setVisible(true);
	}
}
