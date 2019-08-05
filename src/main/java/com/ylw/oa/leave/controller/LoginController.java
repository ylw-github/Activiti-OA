package com.ylw.oa.leave.controller;

import com.ylw.oa.leave.pagemodel.MSG;
import com.ylw.oa.leave.service.LoginService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

import io.swagger.annotations.Api;


/**
 * =======================================================
 *
 * @desc: 登录控制层
 * @author： YangLinWei
 * @version: V1.0
 * @FileName: com.ylw.oa.leave.controller LoginController
 * @date: 2019/8/2 11:22
 * <p>
 * =======================================================
 */

@Api(value = "登录登出接口")
@Controller
public class LoginController {

    @Autowired
    LoginService loginservice;


    /**
     * 默认访问（登录页）
     *
     * @author YangLinWei
     * @date 2019/8/2  11:23
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String login() {
        return "login";
    }

    /**
     * 登录校验接口
     *
     * @param username 用户名
     * @param pwd      密码
     * @return String
     * @author YangLinWei
     * @date 2019/8/2  11:22
     */
    @RequestMapping(value = "/loginvalidate", method = RequestMethod.POST)
    public String loginvalidate(@RequestParam("username") String username, @RequestParam("password") String pwd, HttpSession httpSession) {
        if (username == null)
            return "login";
        String realpwd = loginservice.getpwdbyname(username);
        if (realpwd != null && pwd.equals(realpwd)) {
            httpSession.setAttribute("username", username);
            return "index";
        } else
            return "fail";
    }

    /**
     * 登出
     *
     * @author YangLinWei
     * @date 2019/8/2  11:24
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpSession httpSession) {
        httpSession.removeAttribute("username");
        return "login";
    }

    /**
     * 获取当前用户
     *
     * @author YangLinWei
     * @date 2019/8/2  11:24
     */
    @RequestMapping(value = "/currentuser", method = RequestMethod.GET)
    @ResponseBody
    public MSG currentuser(HttpSession httpSession) {
        String userid = (String) httpSession.getAttribute("username");
        return new MSG(userid);
    }

}
