/* ---------------------
 * ScatterPlotDemo4.java
 * ---------------------
 * (C) Copyright 2004-2006, by Object Refinery Limited.
 *
 */

package com.omicseq.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.Stroke;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

/**
 * A demo of the fast scatter plot.
 *
 */
public class FastScatterPlotDemo extends ApplicationFrame {

    /** A constant for the number of items in the sample dataset. */
    private static final int COUNT = 41;

    /** The data. */
    private float[][] data = new float[2][COUNT];

   
    public FastScatterPlotDemo(final String title) {

        super(title);
        populateData();
        final NumberAxis domainAxis = new NumberAxis("X");
        domainAxis.setAutoRangeIncludesZero(false);
        final NumberAxis rangeAxis = new NumberAxis("Y");
        rangeAxis.setAutoRangeIncludesZero(false);
        final FastScatterPlot plot = new FastScatterPlot(this.data, domainAxis, rangeAxis);
        final JFreeChart chart = new JFreeChart("����", plot);
        
        //plot.setPaint(Color.green);//落点颜色
        // ValueAxis va = plot.getDomainAxis();
       // va.setAxisLineStroke(new BasicStroke(1.5f));  
//        chart.setLegend(null);
       // plot.setBackgroundPaint(Color.BLUE)//设置背景色
        // force aliasing of the rendered content..
        chart.getRenderingHints().put
            (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final ChartPanel panel = new ChartPanel(chart, true);
        panel.setPreferredSize(new java.awt.Dimension(500, 270));
  //     panel.setHorizontalZoom(true);
    //    panel.setVerticalZoom(true);
        
        panel.setMinimumDrawHeight(10);
        panel.setMaximumDrawHeight(2000);
        panel.setMinimumDrawWidth(20);
        panel.setMaximumDrawWidth(2000);
        setContentPane(panel);

    }


    /**
     * Populates the data array with random values.
     */
    private void populateData() {

       /* for (int i = 0; i < this.data[0].length; i++) {
            final float x = (float) i + 1000;
            this.data[0][i] = x;
            this.data[1][i] = 1000 + (float) Math.random() * COUNT;
        }
       */
    	int j = 0;
    	for(float i = -1; i <= 1;i+=0.05){
    		j++;
    		this.data[0][j] = i;
    	}
    	double [] a =  {2.57566048279567E-31,
    			2.88161448001024E-28,
    			2.24924688281016E-25,
    			1.22487667131671E-22,
    			4.65373431809629E-20,
    			1.23357282860025E-17,
    			2.28129784548952E-15,
    			2.94342634283568E-13,
    			2.64958795576452E-11,
    			1.66401599299834E-09,
    			7.29105941978784E-08,
    			0.0000022288342134663,
    			0.0000475355890923839,
    			0.000707316813078465,
    			0.00734282316136515,
    			0.0531821809432554,
    			0.268734363538112,
    			0.947401899610723,
    			2.33023265979854,
    			3.99869523470159,
    			4.78730736481719,
    			3.99869523470161,
    			2.33023265979857,
    			0.947401899610737,
    			0.268734363538117,
    			0.0531821809432568,
    			0.00734282316136537,
    			0.000707316813078491,
    			0.0000475355890923859,
    			2.22883421346641E-06,
    			7.29105941978818E-08,
    			1.66401599299844E-09,
    			2.64958795576471E-11,
    			2.94342634283587E-13,
    			2.28129784548969E-15,
    			1.23357282860035E-17,
    			4.65373431809668E-20,
    			1.22487667131683E-22,
    			2.24924688281036E-25,
    			2.88161448001053E-28,
    			2.57566048279597E-31
    	};
    	for(int i = 0; i<a.length;i++){
    		this.data[1][i] = (float) a[i];
    	}
    }

   
    public static void main(final String[] args) {

        final FastScatterPlotDemo demo = new FastScatterPlotDemo("Fast Scatter Plot");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
        
    }

}
