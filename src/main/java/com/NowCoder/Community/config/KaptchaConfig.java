package com.NowCoder.Community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {
    /**
     * 创建一个验证码的producer实例，将其装配到Spring容器中
     * 在需要使用验证码时从容器中实例化一个然后使用
     * @return
     */
    @Bean
    public Producer kaptchaProducer()
    {
        Properties properties=new Properties();
        //对验证码图片大小和字体大小的的具体配置
        properties.setProperty("kaptcha.image.width","100");
        properties.setProperty("kaptcha.image.height","40");
        properties.setProperty("kaptcha.textproducer.font.size","32");
        //设置字体为黑色
        properties.setProperty("kaptcha.textproducer.font.color", "0,0,0");
        //设置验证码字符的范围
        properties.setProperty("kaptcha.textproducer.char.string","0123456789abcdefghijklmnopqrstuvwxyz");
        //设置验证码的长度
        properties.setProperty("kaptcha.textproducer.char.length","4");
        //设置验证码的干扰类
        properties.setProperty("kaptcha.noise.impl","com.google.code.kaptcha.impl.NoNoise");
        DefaultKaptcha kaptcha=new DefaultKaptcha();
        Config config=new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
