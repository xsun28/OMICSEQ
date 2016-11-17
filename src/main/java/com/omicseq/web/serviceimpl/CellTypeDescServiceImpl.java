package com.omicseq.web.serviceimpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.omicseq.domain.CellTypeDesc;
import com.omicseq.store.dao.ICellDescDao;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.web.service.ICellTypeDescService;

@Service
public class CellTypeDescServiceImpl implements ICellTypeDescService {

	protected static ICellDescDao cellTypeDescDAO = DAOFactory.getDAO(ICellDescDao.class);
	
	@Override
	public Map<String, String> findAll() {
		Map<String, String> map = new HashMap<String, String>();
		List<CellTypeDesc> fds = cellTypeDescDAO.find(new SmartDBObject());
		for(CellTypeDesc fd : fds)
		{
			map.put(fd.getCell(), fd.getCell_desc());
		}
		return map;
	}

	@Override
	public String findByCellRegex(String cell) {
		List<CellTypeDesc> fds = cellTypeDescDAO.find(new SmartDBObject("cell", new SmartDBObject("$regex", cell)));
		if(fds != null && fds.size()>0)
		{
			return fds.get(0).getCell_desc();
		}
		return null;
	}

}
