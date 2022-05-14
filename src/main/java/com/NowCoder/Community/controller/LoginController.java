package com.NowCoder.Community.controller;

import ch.qos.logback.core.OutputStreamAppender;
import com.NowCoder.Community.Service.UserService;
import com.NowCoder.Community.dao.UserMapper;
import com.NowCoder.Community.entity.User;
import com.NowCoder.Community.util.CommunityConstant;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.SmartView;
import org.springframework.web.servlet.View;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private Producer kaptchaProducer;
    @Value("${server.servlet.context-path}")
    private String contextPath;
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
    @RequestMapping(path = "/forgetPassword" ,method = RequestMethod.GET)
    public String getForgetPasswordPage() {return "/site/forget";}
    @RequestMapping(path = "/forgetPassword",method = RequestMethod.POST)
    public String forgetPassword (Model model,String email,String newPassword,String code,
                                  HttpSession session,HttpServletResponse response)
    {
        //检查验证码
        String kaptcha= (String) session.getAttribute("kaptcha");
        if (StringUtils.isBlank(code) || StringUtils.isBlank(kaptcha) || !kaptcha.equalsIgnoreCase(code))
        {
            model.addAttribute("codeMsg","验证码有误");
            return "/site/forget";
        }
        Map<String,Object> map=userService.forgetPassword(email,newPassword);
        if (map==null || map.isEmpty())
        {
            model.addAttribute("msg","我们已经向您的邮箱发送了确认邮件，请确认密码更改");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }
        else
        {
            model.addAttribute("emailMsg",map.get("emailMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/forget";
        }
    }
    @RequestMapping(path = "/confirm/{userId}/{newPassword}/{notExpiredDay}",method = RequestMethod.GET)
    public String confirmChangePassword(Model model, @PathVariable("userId")int userId, @PathVariable("newPassword")String newPassword,
                                        @PathVariable("notExpiredDay")String Day) throws ParseException {
        Date now=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date notExpiredDay=sdf.parse(Day);
        int isOutDate=now.compareTo(notExpiredDay);
        if (isOutDate!=-1)
        {
            model.addAttribute("msg","该修改密码请求已过期，请重新请求");
            model.addAttribute("target","/forgetPassword");
            return "/site/operate-result";
        }
        userService.changePassword(userId,newPassword);
        model.addAttribute("msg","已确认密码更改，可以使用新密码了");
        model.addAttribute("target","/index");
        return "/site/operate-result";

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
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username,String password,String code,boolean rememberMe,
                        Model model,HttpSession session,HttpServletResponse response)
    {
        //验证码检查
        String kaptcha= (String) session.getAttribute("kaptcha");
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code))
        {
            model.addAttribute("codeMsg","验证码有误");
            return "/site/login";
        }
        //检查账号密码
        int expiredSeconds=rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String,Object> map=userService.login(username,password,expiredSeconds);
        if (map.containsKey("ticket"))
        {
            Cookie cookie=new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }
        else
        {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket")String ticket)
    {
        userService.logout(ticket);
        return"redirect:/login";
    }



}
