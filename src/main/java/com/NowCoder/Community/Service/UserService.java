package com.NowCoder.Community.Service;

import com.NowCoder.Community.dao.UserMapper;
import com.NowCoder.Community.entity.User;
import com.NowCoder.Community.util.CommunityConstant;
import com.NowCoder.Community.util.CommunityUtil;
import com.NowCoder.Community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService  implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    public User findUserById(int userId)
    {
        return userMapper.selectById(userId);
    }

    public Map<String,Object> register (User user)
    {
        Map <String,Object> map=new HashMap<>();
        //空值处理
        if (user==null)
        {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername()))
        {
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword()))
        {
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail()))
        {
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
        //判断账号是否存在
        User u=userMapper.selectByName(user.getUsername());
        if (u!=null)
        {
            map.put("usernameMsg","用户名已经存在");
            return map;
        }
        u=userMapper.selectByEmail(user.getEmail());
        if (u!=null)
        {
            map.put("emailMsg","邮箱已被注册");
            return map;
        }
        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        //设置为普通用户
        user.setType(0);
        //设置为未激活账户（需要邮箱验证激活)
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://image.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //发送激活邮件
        Context context=new Context();
        context.setVariable("email",user.getEmail());
        //http://localhost:8080/community/activation/101/code
        String url=domain+contextPath + "/activation/" + user.getId() +'/'+ user.getActivationCode();
        context.setVariable("url",url);
        String content=templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);
        return map;
    }

    //激活账号
    public int activation(int userId,String code)
    {
        User user = userMapper.selectById(userId);
        //重复激活逻辑
        if (user.getStatus()==1)
        {
            return ACTIVATION_REPEAT;
        }
        //激活码错误逻辑
        if (!user.getActivationCode().equals(code))
        {
            return ACTIVATION_FAILURE;
        }
        if (user.getActivationCode().equals(code))
        {
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        }
        return ACTIVATION_FAILURE;
    }


}
