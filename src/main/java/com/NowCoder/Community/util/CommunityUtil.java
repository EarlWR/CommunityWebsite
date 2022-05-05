package com.NowCoder.Community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {

    //生成随机的字符串
    public static String generateUUID()
    {
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    //MD5算法加密（对密码加密）
    //MD5只能加密，不能解密
    //一般要对密码加一个随机字符串，即salt,例如3e4a8，防止黑客直接对简单密码对应的暗文库进行破解
    public static String md5(String key)
    {
        //判断字符串是否为空
        if (StringUtils.isBlank(key))
        {
            return null;
        }
        //返回暗文
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

}
