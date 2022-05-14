package com.NowCoder.Community.Service;

import com.NowCoder.Community.dao.LoginTicketMapper;
import com.NowCoder.Community.dao.UserMapper;
import com.NowCoder.Community.entity.LoginTicket;
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

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class UserService  implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    public User findUserById(int userId)
    {
        return userMapper.selectById(userId);
    }

    /**
     * 注册业务逻辑
     * @param user 注册的用户的对象
     * @return 四种key值，若返回usernameMsg，说明用户名为空或已存在，若返回passwordMsg，说明密码为空，若返回emailMsg，说明邮箱为空或已经被注册了
     */
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

    /**
     * 激活账号逻辑
     * @param userId 用户id
     * @param code 激活码
     * @return 三种返回值，若返回ACTIVATION_REPEAT，说明该账户已被激活，若返回ACTIVATION_FAILURE，说明激活码错误或未知情况，若返回ACTIVATION_SUCCESS，说明激活成功
     */
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

    /**
     * 登录业务逻辑
     * @param username 输入的用户名
     * @param password 输入的密码（明文，需在逻辑中转化为密文)
     * @param expiredSeconds 登录生效时间
     * @return 三种key值，若返回usernameMsg,说明账号为空或者不存在或者未激活，若返回passwordMsg,说明密码为空或错误,若返回ticket，说明登陆成功，携带登录信息返回
     */
    public Map<String ,Object> login(String username,String password,int expiredSeconds)
    {
        Map<String,Object> map=new HashMap<>();

        //空值处理
        if (StringUtils.isBlank(username))
        {
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        //验证账号
        User user=userMapper.selectByName(username);
        if (user==null)
        {
            map.put("usernameMsg","该账号不存在");
            return map;
        }

        //验证状态
        if (user.getStatus()==0)
        {
            map.put("usernameMsg","该账号未激活");
            return map;
        }

        //验证密码
        password=CommunityUtil.md5(password+user.getSalt());
        if (!user.getPassword().equals(password))
        {
            map.put("passwordMsg","密码错误");
            return map;
        }
        //登陆成功，生成登陆凭证
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds*1000));
        loginTicketMapper.insertLoginTicket(loginTicket);
        map.put("ticket",loginTicket.getTicket());
        return map;

    }

    /**
     * 登出逻辑
     * @param ticket 登出的凭证
     */
    public void logout(String ticket)
    {
        loginTicketMapper.updateStatus(ticket,1);
    }

    public Map<String,Object> forgetPassword(String email,String newPassword)
    {
        Map<String,Object> map=new HashMap<>();
        if (StringUtils.isBlank(email))
        {
            map.put("emailMsg","电子邮箱不能为空");
            return map;
        }
        if (StringUtils.isBlank(newPassword))
        {
            map.put("passwordMsg","新密码不能为空");
            return map;
        }
        User user=userMapper.selectByEmail(email);
        if (user==null || user.getStatus()==0)
        {
            map.put("emailMsg","该用户不存在或者未激活");
            return map;
        }
        if (!StringUtils.equals(email,user.getEmail()))
        {
            map.put("emailMsg","该邮箱不存在");
            return map;
        }
        if (StringUtils.equals(CommunityUtil.md5(newPassword+user.getSalt()),user.getPassword()))
        {
            map.put("passwordMsg","新密码与旧密码一致");
            return map;
        }
        //此处获取7天后的日期，用于更改密码的时效性检查
        Date now =new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c=Calendar.getInstance();
        c.setTime(now);
        c.add(Calendar.MINUTE,10);
        Date notExpiredDay =c.getTime();
        String Day=sdf.format(notExpiredDay);
        //开始发送邮件
        Context context=new Context();
        context.setVariable("email",email);
        String url=domain+contextPath+"/confirm/"+user.getId()+'/'+CommunityUtil.md5(newPassword+user.getSalt())+'/'+Day;
        context.setVariable("url",url);
        String content=templateEngine.process("/mail/forget",context);
        mailClient.sendMail(email,"更改密码确认",content);
        return null;
    }
    public void changePassword(int id,String newPassword)
    {
        userMapper.updatePassword(id,newPassword);
        return;
    }

}
