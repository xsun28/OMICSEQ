package com.omicseq.utils;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.servlet.ServletUtilities;
import org.jfree.data.general.DefaultPieDataset;

import com.omicseq.bean.SampleItem;
import com.omicseq.common.ExperimentType;
import com.omicseq.domain.GeneRank;
import com.omicseq.domain.Sample;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

public class JFreeChartUtils {
	
	private IGeneRankDAO geneRankDAO = DAOFactory.getDAO(IGeneRankDAO.class);

	public static void main(String[] args) throws Exception {
		JFreeChartUtils j = new JFreeChartUtils();
		j.chart(30236);
	}
	
	public String chart(Integer geneId) throws Exception {
		
		DefaultPieDataset dataset = new DefaultPieDataset();
		
		List<GeneRank> sampleList = geneRankDAO.listByGeneId(geneId);
		int chip_seq = 0;
		int rna_seq = 0;
		int cnv =0;
		int methylation=0;
		int dnase_seq=0;
		int microarray = 0;
		int summaryTrack = 0;
		int mutation = 0;
		for(GeneRank gr : sampleList)
		{
			if(gr.getEtype() ==  ExperimentType.CHIP_SEQ_HISTONE.value() || gr.getEtype() ==  ExperimentType.CHIP_SEQ_TF.value())
			{
				chip_seq += 1;
			}
			if(gr.getEtype() ==  ExperimentType.RNA_SEQ.value())
			{
				rna_seq += 1;
			}
			if(gr.getEtype() ==  ExperimentType.DNASE_SEQ.value())
			{
				dnase_seq += 1;
			}
			if(gr.getEtype() ==  ExperimentType.CVN.value())
			{
				cnv += 1;
			}
			if(gr.getEtype() ==  ExperimentType.METHYLATION.value())
			{
				methylation += 1;
			}
			if(gr.getEtype() == ExperimentType.MICROARRAY.value())
			{
				microarray += 1;
			}
			if(gr.getEtype() == ExperimentType.SUMMARY_TRACK.value())
			{
				summaryTrack += 1;
			}
			if(gr.getEtype() == ExperimentType.MUTATION.value())
			{
				mutation += 1;
			}
		}
		int sum = chip_seq + rna_seq + dnase_seq + cnv + methylation + microarray + mutation + summaryTrack;
		if(chip_seq > 0) {
			dataset.setValue("ChIP-seq", chip_seq);
		}
		if(rna_seq > 0)
		{
			dataset.setValue("RNA-seq", rna_seq);
		}
		if(dnase_seq > 0) {
			dataset.setValue("Dnase-seq", dnase_seq);
		}
		if(cnv > 0)
		{
			dataset.setValue("CNV", cnv);
		}
		if(methylation > 0 )
		{
			dataset.setValue("MethyLation", methylation);
		}
		if(microarray > 0)
		{
			dataset.setValue("Microarray", microarray);
		}
		if(summaryTrack > 0)
		{
			dataset.setValue("Summary Track", summaryTrack);
		}
		if(mutation > 0)
		{
			dataset.setValue("Somatic Mutations", mutation);
		}
		
		
//		PiePlot3D pieplot = new PiePlot3D(dataset);//生成一个3D饼图
		
		JFreeChart chart = ChartFactory.createPieChart3D("Distributed of Data Type, total:"+ sum, dataset, true, true, true);
//		JFreeChart chart = new JFreeChart("Distributed of Data Type",JFreeChart.DEFAULT_TITLE_FONT, pieplot, true); 
		
		PiePlot3D pieplot = (PiePlot3D) chart.getPlot();
		pieplot.setCircular(true);
		pieplot.setForegroundAlpha(0.65f);
		pieplot.setStartAngle(360);
		pieplot.setSectionPaint("ChIP-seq", new Color(252, 254, 184));
		pieplot.setSectionPaint("RNA-seq", new Color(253, 199, 199));
		pieplot.setSectionPaint("Dnase-seq", new Color(166, 211, 119));
		pieplot.setSectionPaint("CNV", new Color(176, 224, 230));
		pieplot.setSectionPaint("MethyLation", new Color(133, 142, 250));
		pieplot.setSectionPaint("Microarray", new Color(140, 141, 138));
		pieplot.setSectionPaint("Summary Track", new Color(174, 230, 64));
		pieplot.setSectionPaint("Somatic Mutations", new Color(66, 146, 209));
		pieplot.setToolTipGenerator(new StandardPieToolTipGenerator());
		StandardPieSectionLabelGenerator standarPieIG = new StandardPieSectionLabelGenerator("{0}:{1}", NumberFormat.getNumberInstance(), NumberFormat.getCurrencyInstance());  
		pieplot.setLabelGenerator(standarPieIG);
		pieplot.setNoDataMessage("please wait for a moment");  
		pieplot.setLabelGap(0.02D);
		
		try {
			String fileName = ServletUtilities.saveChartAsPNG(chart, 400, 300, null, null);
			return fileName;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	public String chartByList(List<Sample> sampleList) throws Exception {
		DefaultPieDataset dataset = new DefaultPieDataset();
		int chip_seq = 0;
		int rna_seq = 0;
		int cnv =0;
		int methylation=0;
		int dnase_seq=0;
		int microarray = 0;
		int summaryTrack = 0;
		int mutation = 0;
		for(Sample sample : sampleList)
		{
			if(sample.getEtype() == ExperimentType.CHIP_SEQ_HISTONE.value() || sample.getEtype() == ExperimentType.CHIP_SEQ_TF.value())
			{
				chip_seq += 1;
			}
			if(sample.getEtype() == ExperimentType.RNA_SEQ.value())
			{
				rna_seq += 1;
			}
			if(sample.getEtype() == ExperimentType.DNASE_SEQ.value())
			{
				dnase_seq += 1;
			}
			if(sample.getEtype() == ExperimentType.CVN.value())
			{
				cnv += 1;
			}
			if(sample.getEtype() == ExperimentType.METHYLATION.value())
			{
				methylation += 1;
			}
			if(sample.getEtype() == ExperimentType.MICROARRAY.value())
			{
				microarray += 1;
			}
			if(sample.getEtype() == ExperimentType.SUMMARY_TRACK.value())
			{
				summaryTrack += 1;
			}
			if(sample.getEtype() == ExperimentType.MUTATION.value())
			{
				mutation += 1;
			}
		}
		int sum = chip_seq + rna_seq + dnase_seq + cnv + methylation + microarray + mutation;
		if(chip_seq > 0) {
			dataset.setValue("ChIP-seq", chip_seq);
		}
		if(rna_seq > 0)
		{
			dataset.setValue("RNA-seq", rna_seq);
		}
		if(dnase_seq > 0) {
			dataset.setValue("Dnase-seq", dnase_seq);
		}
		if(cnv > 0)
		{
			dataset.setValue("CNV", cnv);
		}
		if(methylation > 0 )
		{
			dataset.setValue("MethyLation", methylation);
		}
		if(microarray > 0)
		{
			dataset.setValue("Microarray", microarray);
		}
		if(summaryTrack > 0)
		{
			dataset.setValue("Summary Track", summaryTrack);
		}
		if(mutation > 0)
		{
			dataset.setValue("Somatic Mutations", mutation);
		}
		
		
//		PiePlot3D pieplot = new PiePlot3D(dataset);//生成一个3D饼图
		
		JFreeChart chart = ChartFactory.createPieChart3D("Distributed of Data Type, total:"+ sum, dataset, true, true, true);
//		JFreeChart chart = new JFreeChart("Distributed of Data Type",JFreeChart.DEFAULT_TITLE_FONT, pieplot, true); 
		
		PiePlot3D pieplot = (PiePlot3D) chart.getPlot();
		pieplot.setCircular(true);
		pieplot.setForegroundAlpha(0.65f);
		pieplot.setStartAngle(360);
		pieplot.setSectionPaint("ChIP-seq", new Color(252, 254, 184));
		pieplot.setSectionPaint("RNA-seq", new Color(253, 199, 199));
		pieplot.setSectionPaint("Dnase-seq", new Color(166, 211, 119));
		pieplot.setSectionPaint("CNV", new Color(176, 224, 230));
		pieplot.setSectionPaint("MethyLation", new Color(133, 142, 250));
		pieplot.setSectionPaint("Microarray", new Color(140, 141, 138));
		pieplot.setSectionPaint("Summary Track", new Color(174, 230, 64));
		pieplot.setSectionPaint("Somatic Mutations", new Color(66, 146, 209));
		pieplot.setToolTipGenerator(new StandardPieToolTipGenerator());
		StandardPieSectionLabelGenerator standarPieIG = new StandardPieSectionLabelGenerator("{0}:{1}", NumberFormat.getNumberInstance(), NumberFormat.getCurrencyInstance());  
		pieplot.setLabelGenerator(standarPieIG);
		pieplot.setNoDataMessage("please wait for a moment");  
		pieplot.setLabelGap(0.02D);
		
		try {
			String fileName = ServletUtilities.saveChartAsPNG(chart, 400, 300, null, null);
			return fileName;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

}
