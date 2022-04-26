package com.NowCoder.Community.controller;

import com.NowCoder.Community.Service.DiscussPostService;
import com.NowCoder.Community.Service.UserService;
import com.NowCoder.Community.entity.DiscussPost;
import com.NowCoder.Community.entity.Page;
import com.NowCoder.Community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//controler的访问路径是可以省略的,此类用于具体定义对帖子的排序
@Controller
public class HomeController {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;

    @RequestMapping (path = "/index",method = RequestMethod.GET)  //因为方法响应的是网页，所以不写ResponseBody
    public ModelAndView getIndexPage(Page page)
    {
        //page装配到model的步骤可以省略，dispatchServlet类会自动装配page到model
        //方法调用前，SpringMVC会自动实例化Model和Page,并将Page注入Model
        //所以,在thymeleaf中可以直接访问Page对象中的数据
        page.setRows(discussPostService.findDiscussRows(0));
        page.setPath("/index");
        ModelAndView mav=new ModelAndView();
        List<DiscussPost> list= discussPostService.findDiscuss(0,page.getOffset(),page.getLim());
        List<Map<String,Object>> discussPosts=new ArrayList<>();  //discussPosts整合了用户和帖子的数据
        if (list!=null)
        {
            for (DiscussPost post:list)
            {
                Map<String,Object> map=new HashMap<>();
                map.put("post",post);
                User user= userService.findUserById(post.getUserId());
                map.put("user",user);
                discussPosts.add(map);

            }
        }
        mav.addObject("discussPosts", discussPosts);
        mav.setViewName("/index");
        return mav;
    }
}
