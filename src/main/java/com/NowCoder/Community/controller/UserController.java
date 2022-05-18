package com.NowCoder.Community.controller;

import com.NowCoder.Community.Service.UserService;
import com.NowCoder.Community.annotation.LoginRequired;
import com.NowCoder.Community.entity.User;
import com.NowCoder.Community.util.CommunityUtil;
import com.NowCoder.Community.util.CookieUtil;
import com.NowCoder.Community.util.HostHolder;
import com.sun.deploy.net.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Earl_WR
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger= LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;
    /**
     * 获取当前用户
     */
    @Autowired
    private HostHolder hostHolder;
    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage()
    {
        return "/site/setting";
    }

    /**
     * 修改密码功能
     * @param request 用于提取登陆凭证
     * @param formerPassword 原密码
     * @param nowPassword 新密码
     * @param repeatPassword 确认密码
     * @param model 模板
     * @return 返回路径，修改失败返回当前页面，成功则退出登录并返回登陆页面
     */
    @LoginRequired
    @RequestMapping(path = "/changePassword", method = RequestMethod.POST)
    public String changePassword(HttpServletRequest request, String formerPassword, String nowPassword, String repeatPassword, Model model) {
        //异常判断
        if (formerPassword == null || StringUtils.isBlank(formerPassword)) {
            model.addAttribute("formerPasswordMsg", "原密码不能为空");
            return "/site/setting";
        }
        if (nowPassword == null || StringUtils.isBlank(nowPassword)) {
            model.addAttribute("nowPasswordMsg", "新密码不能为空");
            return "/site/setting";
        }
        if (repeatPassword == null || StringUtils.isBlank(repeatPassword)) {
            model.addAttribute("repeatPasswordMsg", "确认密码不能为空");
            return "/site/setting";
        }
        if (formerPassword.equals(nowPassword))
        {
            model.addAttribute("nowPasswordMsg", "原密码与新密码一致");
            model.addAttribute("formerPasswordMsg", "原密码与新密码一致");
            return "/site/setting";
        }
        if (!nowPassword.equals(repeatPassword))
        {
            model.addAttribute("repeatPasswordMsg", "确认密码与新密码不一致");
            return "/site/setting";
        }
        User user=hostHolder.getUser();
        String userPassword=user.getPassword();
        formerPassword=CommunityUtil.md5(formerPassword+user.getSalt());
        if (!formerPassword.equals(userPassword))
        {
            model.addAttribute("formerPasswordMsg", "密码有误");
            return "/site/setting";
        }

        //开始更新密码
        nowPassword = CommunityUtil.md5(nowPassword + user.getSalt());
        userService.changePassword(user.getId(), nowPassword);
        //登出，并重定向到login，重新登录
        String loginTicket = CookieUtil.getValue(request, "ticket");
        userService.logout(loginTicket);
        return "redirect:/login";
    }


    /**
     * 上传头像功能
     * @param headerImage 头像文件对象
     * @param model 模板
     * @return 返回路径，成功返回首页，失败返回当前页
     */
    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model)
    {
        if (headerImage==null)
        {
            model.addAttribute("error","您未选择图片");
            return "/site/setting";
        }
        //获取后缀名，并对后缀名进行合法性判断
        String fileName=headerImage.getOriginalFilename();
        String suffix= fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix))
        {
            model.addAttribute("error", "文件格式有误");
            return "/site/setting";
        }
        //生成随机文件名
        fileName= CommunityUtil.generateUUID()+suffix;
        //确定文件存放路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败"+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常，请联系微信Tars_L，他会想个理由甩锅",e);
        }
        //更新当前用户的头像路径(Web访问路径)
        //http://localhost:8080/community/user/header/xxx.png
        User user=hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";
    }

    /**
     * 显示头像功能
     * @param fileName 在upload文件夹中投降的名字
     * @param response 该方法使用return无法返回，所以使用response来返回图片
     */
    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        //服务器存放路径
        fileName=uploadPath+"/"+fileName;
        //获取后缀名
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/"+suffix);
        //放置在try括号中的资源会在try后自动释放
        try(FileInputStream fis=new FileInputStream(fileName);
            OutputStream os = response.getOutputStream();)
        {

            //建立缓冲区，一次多输出几个字节，提高效率
            byte[] buffer=new byte[1024];
            int b=0;
            while ((b=fis.read(buffer))!=-1)
            {
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败"+e.getMessage());
        }

    }

}
