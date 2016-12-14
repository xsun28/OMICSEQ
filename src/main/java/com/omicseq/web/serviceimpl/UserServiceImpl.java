package com.omicseq.web.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.omicseq.domain.User;
import com.omicseq.exception.OmicSeqException;
import com.omicseq.store.dao.IUserDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.web.service.IUserService;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private MessageSourceHelper messageSourceHelper;
    
    IUserDAO userDAO = DAOFactory.getInstance().getDAO(IUserDAO.class);

    /**
     * 加密码
     * 
     * @param data
     * @return
     */
    private String encode(String data) {
        return DigestUtils.md5Hex(StringUtils.trimToEmpty(data));

    }

    @Override
    public User login(String userName, String password) {
        String pwd = encode(password);
        User user = userDAO.findByUserName(userName);
        if (null == user || !pwd.equals(user.getPassword())) {
            throw new OmicSeqException(messageSourceHelper.getMessage("error.login.userwrong"));
        } else if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new OmicSeqException(messageSourceHelper.getMessage("error.login.userdisable"));
        }
        return user;
    }

    @Override
    public User register(User user) {
        String userName = StringUtils.trimToEmpty(user.getName());
        User temp = userDAO.findByUserName(userName);
        if (temp != null) {
            throw new OmicSeqException(messageSourceHelper.getMessage("error.login.userexist"));
        } else {
            if (StringUtils.isNotBlank(user.getName())) {
                user.setName(StringUtils.trimToEmpty(user.getName()));
            }
            if (StringUtils.isNoneBlank(user.getPassword())) {
                user.setPassword(encode(user.getPassword()));
            }
            user.setUserType(0);
            userDAO.create(user);
        }
        return user;

    }

    public static void main(String[] args) {
    	UserServiceImpl us = new UserServiceImpl();
    	User user = us.findByUserName("test");
    	user.setPassword("123456");
    	us.update(user);
	}
    
    @Override
    public User update(User user) {
        User dbUser = userDAO.findByUserName(user.getName());
        if (dbUser == null) {
            throw new OmicSeqException(messageSourceHelper.getMessage("error.login.usernoexist"));
        } else {
            if (StringUtils.isNotBlank(user.getName())) {
                dbUser.setName(StringUtils.trimToEmpty(user.getName()));
            }
            if (StringUtils.isNotBlank(user.getPassword())) {
                dbUser.setPassword(encode(user.getPassword()));
            }
            if (StringUtils.isNotBlank(user.getCellType())) {
                dbUser.setCellType(user.getCellType());
            }
            if (StringUtils.isNotBlank(user.getExperiments())) {
                dbUser.setExperiments(user.getExperiments());
            }
            if (StringUtils.isNotBlank(user.getSource())) {
                dbUser.setSource(user.getSource());
            }
            if (StringUtils.isNotBlank(user.getExperiments_mouse())) {
                dbUser.setExperiments_mouse(user.getExperiments_mouse());
            }
            if (StringUtils.isNotBlank(user.getSource_mouse())) {
                dbUser.setSource_mouse(user.getSource_mouse());
            }
            if (user.getEmail() != dbUser.getEmail()) {
                dbUser.setEmail(user.getEmail());
            }
            if (user.getCompany() != dbUser.getCompany()) {
                dbUser.setCompany(user.getCompany());
            }
            if (user.getPageSize() != null) {
                dbUser.setPageSize(user.getPageSize());
            }
            if(user.getIdCardAndPhone() != null)
            {
            	dbUser.setIdCardAndPhone(user.getIdCardAndPhone());
            }
            if(user.getUserType() == null)
            {
            	dbUser.setUserType(dbUser.getUserType());
            } else {
            	dbUser.setUserType(user.getUserType());
            }
            userDAO.update(dbUser);
        }
        return dbUser;
    }

    @Override
    public User findByUserName(String userName) {
        return userDAO.findByUserName(userName);
    }
    

    @Override
    public boolean changePsd(String password,User user){
    	try {
    		String pass = encode(password.trim());
    		user.setPassword(pass);
    		userDAO.update(user);
		} catch (Exception e) {
			return false;
		}
    	return true;
    }

	@Override
	public List<User> showRegisterUniversity() {
		List<User> userList = userDAO.findAll();
		List<User> universities = new ArrayList<User>();
		for(User user : userList){
			if(user.getCompany()==null) continue;
			if(user.getCompany().contains("大学") || user.getCompany().contains("医学院")){
				universities.add(user);
			}
		}
		return universities;
	}
    
/*   public static void main(String[] args) {
	System.out.println(DigestUtils.md5Hex(StringUtils.trimToEmpty("000000")));
	System.out.println(DigestUtils.md5Hex(StringUtils.trimToEmpty("123456")));
}*/ 
}
