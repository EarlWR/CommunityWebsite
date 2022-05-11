package com.NowCoder.Community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class MailClient {

    private static final Logger logger= LoggerFactory.getLogger((MailClient.class));

    @Autowired
    private JavaMailSender mailSender;

    //每次发送邮件都是从同一个用户发出，所以在这里直接将username注入，省的一遍一遍读取
    @Value("${spring.mail.username}")
    private String from;

    public void sendMail(String to,String subject,String content)
    {
        try {
            MimeMessage message=mailSender.createMimeMessage();
            MimeMessageHelper helper=new MimeMessageHelper(message);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            //如果不加true会以文本发送，加了以html文件发送
            helper.setText(content,true);
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败"+e.getMessage());
        }
    }

}
