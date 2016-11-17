package com.omicseq.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import com.omicseq.utils.ExcelReader;

public class Test4 {

	public static void main(String[] args) {
		/*double d = Math.sqrt(12*33);
		int time_avg = 25;
		double b = d*time_avg;
		double x = NORMSDIST(b);
		System.out.println(x);*/
		ExcelReader er = new ExcelReader();
		InputStream is2;
		try {
			is2 = new FileInputStream("E:\\其他文档\\balls.xls");
			Map<Integer,String> map =  er.readExcelContent(is2);
			for(int i=1; i<map.size(); i++)
			{
				String row = map.get(i);
				String[] values = row.split("@");
				Double num = Double.parseDouble(values[0]);
				Double time = Double.parseDouble(values[1]);
				Integer avg = 290;
				System.out.println(num + ":"+ time + ":");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	public static double NORMSDIST(double b)
    {
        double p = 0.2316419;
        double b1 = 0.31938153;
        double b2 = -0.356563782;
        double b3 = 1.781477937;
        double b4 = -1.821255978;
        double b5 = 1.330274429;
         
        double x = Math.abs(b);
        double t = 1/(1+p*x);
         
        double val = 1 - (1/(Math.sqrt(2*Math.PI))  * Math.exp(-1*Math.pow(b, 2)/2)) 
						* (b1*t + b2 * Math.pow(t,2) + b3*Math.pow(t,3) + b4 * Math.pow(t,4) + b5 * Math.pow(t,5));
        if(b < 0)
        {
        	val = 1 - val;
        }
        return val;
    }
}
