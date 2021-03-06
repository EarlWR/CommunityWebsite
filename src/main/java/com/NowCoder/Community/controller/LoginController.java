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

/**
 * @author Earl_WR
 */
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
        //???????????????
        String kaptcha= (String) session.getAttribute("kaptcha");
        if (StringUtils.isBlank(code) || StringUtils.isBlank(kaptcha) || !kaptcha.equalsIgnoreCase(code))
        {
            model.addAttribute("codeMsg","???????????????");
            return "/site/forget";
        }
        Map<String,Object> map=userService.forgetPassword(email,newPassword);
        if (map==null || map.isEmpty())
        {
            model.addAttribute("msg","????????????????????????????????????????????????????????????????????????");
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
                                        @PathVariable("notExpiredDay")String day) throws ParseException {
        Date now=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date notExpiredDay=sdf.parse(day);
        int isOutDate=now.compareTo(notExpiredDay);
        if (isOutDate!=-1)
        {
            model.addAttribute("msg","????????????????????????????????????????????????");
            model.addAttribute("target","/forgetPassword");
            return "/site/operate-result";
        }
        userService.changePassword(userId,newPassword);
        model.addAttribute("msg","????????????????????????????????????????????????");
        model.addAttribute("target","/index");
        return "/site/operate-result";

    }
    @RequestMapping(path = "/register",method = RequestMethod.POST)
    public String register (Model model, User user)
    {
        Map<String,Object> map=userService.register(user);
        if (map==null  || map.isEmpty())
        {
            model.addAttribute("msg","????????????,????????????????????????????????????????????????,???????????????");
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
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId,@PathVariable("code") String code)
    {
        int result =userService.activation(userId,code);
        if (result==ACTIVATION_SUCCESS)
        {
            model.addAttribute("msg","???????????????????????????????????????????????????");
            model.addAttribute("target","/login");
        }
        else if (result==ACTIVATION_REPEAT)
        {
            model.addAttribute("msg","?????????????????????????????????????????????");
            model.addAttribute("target","/index");
        }
        else
        {
            model.addAttribute("msg","??????????????????????????????????????????");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }
    /**
     * ????????????????????????
     * ????????????void??????????????????????????????????????????????????????????????????Response??????
     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????cookie??????
     * ??????????????????????????????session
     */
    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session)
    {
        //???????????????
        String text=kaptchaProducer.createText();
        BufferedImage image=kaptchaProducer.createImage(text);
        //??????????????????session
        session.setAttribute("kaptcha",text);
        //???????????????????????????
        //??????????????????
        response.setContentType("image/png");
        //????????????????????????????????????????????????
        try {
            OutputStream os=response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("?????????????????????"+e.getMessage());
        }
    }
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username,String password,String code,boolean rememberMe,
                        Model model,HttpSession session,HttpServletResponse response)
    {
        //???????????????
        String kaptcha= (String) session.getAttribute("kaptcha");
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code))
        {
            model.addAttribute("codeMsg","???????????????");
            return "/site/login";
        }
        //??????????????????
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
