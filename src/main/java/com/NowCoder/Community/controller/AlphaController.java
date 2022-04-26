package com.NowCoder.Community.controller;

import com.NowCoder.Community.Service.AlphaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @Autowired
    private AlphaService alphaService;
    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello()
    {
        return "Hello Spring Boot!";
    }

    @RequestMapping("/data")
    @ResponseBody
    public String getData()
    {
        return alphaService.find();
    }

    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response)
    {
        //获取请求数据
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> enumeration=request.getHeaderNames();
        while (enumeration.hasMoreElements())
        {
            String name=enumeration.nextElement();
            String value=request.getHeader(name);
            System.out.println(name+":"+value);
            System.out.println(request.getParameter("code"));
        }

        //返回响应数据
        response.setContentType("text/html;charset=utf-8");
        try(PrintWriter writer=response.getWriter();)
        {
            writer.write("<h1>牛客网</h1>");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }



    }
    // Get请求 服务器向浏览器提交数据的时候用

    // /students?current=2&limit=20
    // 获取学生信息，分页处理，传进去2个参数，一个是cur(当前页数)=2一个是limit(最多页数)=20
    //@RequestMapping表示下一个方法所对应的网页路径和方法设置
    //@ResponseBody表示具体传回的参数
    //@RequestParam注解可以对传入的参数进行更详细的声明
    @RequestMapping(path="/students",method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(@RequestParam(name="cur",required = false, defaultValue = "1") int cur,
                              @RequestParam(name="limit",required = false, defaultValue = "10") int limit)
    {
        System.out.println(cur);
        System.out.println(limit);
        return "Some Students";
    }

    // /student/123 直接将参数编排为路径的一部分

    @RequestMapping(path="/student/{id}",method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id)
    {
        System.out.println(id);
        return "A student";
    }
    //结论：两种传参方式，一种是问号拼，一种是直接将参数加入路径当中
    //get请求的缺点：1.会直接在网址上传参，不够安全 2.网址的长度是有限的，参数过多的情况下容不下
    //post请求 浏览器向服务器提交数据的时候用的

    @RequestMapping(path="/student",method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name,int age)
    {
        System.out.println("姓名是"+name+"年龄是"+age);
        return "Success";
    }

    //响应HTML数据
    //第一个方法的ModelAndView直接集成了模型和视图，可以直接在一个对象当中对响应数据进行操作
    //而第二个方法则是用一个字符串指向view，用一个参数新建一个模型，也一样用
    @RequestMapping(path = "/teacher",method = RequestMethod.GET)
    public ModelAndView getTeacher() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("name", "张三");
        mav.addObject("age", 30);
        mav.setViewName("/demo/view");
        return mav;
    }

    @RequestMapping(path = "/school",method = RequestMethod.GET)
    public String getSchool(Model model)
    {
        model.addAttribute("name","南阳师范学院");
        model.addAttribute("age",100);
        return "/demo/view";
    }

    // 响应json数据，通常是在异步请求当中
    // 异步请求：例如注册一个账号，输入昵称之后，返回判断是否被占用（而页面却没有刷新），即为异步请求
    // Java对象通过json字符串转成JS对象 Java对象->JSON字符串->JS对象

    @RequestMapping(path = "/emp",method = RequestMethod.GET)
    @ResponseBody  //加了他才能返回json字符串
    public Map<String,Object> getEmp()
    {
        Map<String,Object> emp=new HashMap<>();
        emp.put("name","张三");
        emp.put("age",23);
        emp.put("salary",10000);
        return emp;
    }

    @RequestMapping(path = "/emps",method = RequestMethod.GET)
    @ResponseBody  //加了他才能返回json字符串
    public List<Map<String,Object>> getEmps()
    {
        List<Map<String,Object>> list=new ArrayList<>();
        Map<String,Object> emp=new HashMap<>();
        emp.put("name","张三");
        emp.put("age",23);
        emp.put("salary",10000);

        emp=new HashMap<>();
        emp.put("name","刘兴宇");
        emp.put("age",22);
        emp.put("salary",8000);
        list.add(emp);

        emp=new HashMap<>();
        emp.put("name","赵少然");
        emp.put("age",22);
        emp.put("salary",9000);
        list.add(emp);
        return list;
    }

}
