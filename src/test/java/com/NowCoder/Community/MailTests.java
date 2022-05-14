package com.NowCoder.Community;

import com.NowCoder.Community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;
    @Test
    public void timeTest()
    {
        Date now = new Date();
        Calendar c=Calendar.getInstance();
        c.setTime(now);
        c.add(Calendar.DAY_OF_MONTH,1);
        Date tomorrow=c.getTime();
        System.out.println(tomorrow);
    }

    @Test
    public void testHtmlMail()
    {
        Context context=new Context();
        context.setVariable("username","sunday");
        String content= templateEngine.process("/mail/demo",context);
        System.out.println(content);

        mailClient.sendMail("541905573@qq.com","htmlTest",content);
    }
    @Test
    public void testForgetPwdMail()
    {
        Context context=new Context();
        Random rand=new Random();
        int p=0;
        while (p<10000) {
            p=rand.nextInt(99999);
        }
        StringBuffer stringBuffer=new StringBuffer();
        while (p!=0)
        {
            int temp=p%10;
            p/=10;
            stringBuffer.append(temp);
        }
        String str=stringBuffer.toString();
        context.setVariable("code",str);
        String content=templateEngine.process("/mail/forget",context);
        mailClient.sendMail("541905573@qq.com","找回密码",content);
    }
    @Test
    public void testTextMail()
    {
        mailClient.sendMail("541905573@qq.com","Test","这是一封测试邮件");
    }



}
