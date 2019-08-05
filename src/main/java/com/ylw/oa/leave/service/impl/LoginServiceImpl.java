package com.ylw.oa.leave.service.impl;


import com.ylw.oa.leave.mapper.LoginMapper;
import com.ylw.oa.leave.po.User;
import com.ylw.oa.leave.service.LoginService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 5)
@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    LoginMapper loginmapper;

    public String getpwdbyname(String name) {
        User s = loginmapper.getpwdbyname(name);
        if (s != null)
            return s.getPassword();
        else
            return null;
    }

}
