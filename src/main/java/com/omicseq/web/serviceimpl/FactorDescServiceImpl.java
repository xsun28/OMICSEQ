package com.omicseq.web.serviceimpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.omicseq.domain.FactorDes;
import com.omicseq.store.dao.IFactorDescDao;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.web.service.IFactorDescService;

@Service
public class FactorDescServiceImpl implements IFactorDescService {
	protected static IFactorDescDao factorDescDAO = DAOFactory.getDAO(IFactorDescDao.class);
	
	@Override
	public Map<String, String> findAll() {
		Map<String, String> map = new HashMap<String, String>();
		List<FactorDes> fds = factorDescDAO.find(new SmartDBObject());
		for(FactorDes fd : fds)
		{
			map.put(fd.getFactor(), fd.getFactorDesc());
		}
		return map;
	}

	@Override
	public String findByFactorRegex(String factor) {
		List<FactorDes> fds = factorDescDAO.find(new SmartDBObject("factor", new SmartDBObject("$regex", factor)));
		if(fds != null && fds.size()>0)
		{
			return fds.get(0).getFactorDesc();
		}
		return null;
	}

}
