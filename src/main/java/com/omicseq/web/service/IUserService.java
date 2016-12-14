package com.omicseq.web.service;

import java.util.List;

import com.omicseq.domain.User;

/** 
* 类名称：IUserService 
* 类描述： 用户相关处理
* 
* 创建人：Liangxiaoyan
* 创建时间：2014-3-20 下午7:27:28 
* @version 
* 
*/
public interface IUserService {
    
    public User login(String userName, String password);

    public User register(User user);

    public User update(User user);
    
    User findByUserName(String userName);

	boolean changePsd(String password, User user);
	
	public List<User> showRegisterUniversity();
    
}
