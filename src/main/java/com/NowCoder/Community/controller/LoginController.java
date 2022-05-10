package com.NowCoder.Community.controller;

import ch.qos.logback.core.OutputStreamAppender;
import com.NowCoder.Community.Service.UserService;
import com.NowCoder.Community.entity.User;
import com.NowCoder.Community.util.CommunityConstant;
import com.google.code.kaptcha.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.SmartView;
import org.springframework.web.servlet.View;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private Producer kaptchaProducer;
    /**
     * 以下两个方法是向浏览器返回两个html，所以使用get方法。
     * 当得到html后，注册页面需要获取注册是否成功的信息，登录页面需要获取验证码
     * 此时浏览器便会向服务器请求注册信息和验证码，所以还需要再写两个post方法
     * @return
     */
    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage()
    {
        return "/site/register";
    }
    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage()
    {
        return "/site/login";
    }

    @RequestMapping(path = "/register",method = RequestMethod.POST)
    public String register (Model model, User user)
    {
        Map<String,Object> map=userService.register(user);
        if (map==null  || map.isEmpty())
        {
            model.addAttribute("msg","注册成功,我们已经向您的邮箱发送了激活邮件,请尽快激活");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }
        else
        {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }
    //http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId,@PathVariable("code") String code)
    {
        int result =userService.activation(userId,code);
        if (result==ACTIVATION_SUCCESS)
        {
            model.addAttribute("msg","激活成功，您的账号已经可以正常使用");
            model.addAttribute("target","/login");
        }
        else if (result==ACTIVATION_REPEAT)
        {
            model.addAttribute("msg","无效账号，该账号已经被激活过了");
            model.addAttribute("target","/index");
        }
        else
        {
            model.addAttribute("msg","激活失败，您提供的激活码有误");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }
    /**
     * 生成验证码的方法
     * 返回值为void，因为返回格式特殊，是一张图片，所以要自己用Response返回
     * 且服务器需要判断验证码是否正确，所以该方法是跨请求的。但又因为验证码作为敏感信息不能存在cookie当中
     * 所以该方法还需要使用session
     */
    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session)
    {
        //生成验证码
        String text=kaptchaProducer.createText();
        BufferedImage image=kaptchaProducer.createImage(text);
        //将验证码存入session
        session.setAttribute("kaptcha",text);
        //将图片输出给浏览器
        //设置返回类型
        response.setContentType("image/png");
        //将图片以字节流的方式输出给浏览器
        try {
            OutputStream os=response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("响应验证码失败"+e.getMessage());
        }


    }
}
