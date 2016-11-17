package com.omicseq.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GROEmail {
	public static void main(String[] args) {
		try {
			File file = new File("C:/Users/Administrator/Desktop/GRO-Seq.txt");
			BufferedReader br = new BufferedReader(new FileReader(file));
			String read;
			int i = 1;
			File file1 = new File("C:/Users/Administrator/Desktop/GRO-Seq1.txt");
			FileWriter fw = new FileWriter(file1);
			while((read = br.readLine()) != null){
				if(i++ == 1)	continue;
				String [] temp = read.split(",");
				String sampleId = temp[1];
				String url = temp[2];
				if(!url.contains("acc=")) continue;
				String gseNum = url.split("acc=")[1];
				Document doc = Jsoup.connect(url).timeout(10000).get();
				Element eles = doc.getElementById("ViewOptions").nextElementSibling().getElementsContainingOwnText("E-mail").first();
				String email = null;
				if(eles != null){
					Element ele = eles.nextElementSibling();
					email = ele.text();

				}
				System.out.println(email);
				fw.write(sampleId+"\t"+url+"\t"+gseNum+"\t"+email+"\n\r");
			}
			fw.flush();
			br.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
