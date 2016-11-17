package com.omicseq.pathway;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.omicseq.store.dao.IPathWayDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;

public class SavePathwayGrops {
	private static Logger logger = LoggerFactory.getLogger(SavePathwayGrops.class);
	private static IPathWayDAO pathWayDao = DAOFactory.getDAO(IPathWayDAO.class);

	public static void main(String[] args) {
		SavePathwayGrops sp = new SavePathwayGrops();
//		sp.readFile("F://pathway.gmt");
		sp.updatePathWayNamesToUpper();
	}
	
	private void updatePathWayNamesToUpper()
	{
		SmartDBObject query = new SmartDBObject("pathwayName", new SmartDBObject("$regex", "^CHR"));
		query.put("status", new SmartDBObject("$ne", 1));
		List<PathWay> pathWayList = pathWayDao.find(query);
		
		for(PathWay pathway : pathWayList)
		{
			String pathName = pathway.getPathwayName();
			
			pathway.setPathwayName(pathName.toUpperCase());
			
			pathway.setStatus((short)1);
			pathway.setLastmodified(System.currentTimeMillis());
			
			pathWayDao.update(pathway);
		}
	}
	
	public void readFile(String path){
		File file = new File(path);
		if(!file.exists())
		{
			System.out.println("文件不存在！");
			return;
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			while((line = br.readLine()) != null)
			{
				String[] s = line.split("	");
				String pathwayName = s[0];
				String url = s[1];
				String geneNames = "";
				for(int i=2; i<s.length; i++)
				{
					if(i != s.length-1)
					{
						geneNames += s[i] + ",";
					} else {
						geneNames += s[s.length-1];
					}
				}
				PathWay pw = new PathWay();
				pw.setGeneNames(geneNames);
				pw.setPathwayName(pathwayName);
				pw.setUrl(url);
				this.writeInfoToDB(pw);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void writeInfoToDB(PathWay pw) {
		pw.setPathId(pathWayDao.getSequenceId("pathWay"));
		pathWayDao.create(pw);
	}

}
