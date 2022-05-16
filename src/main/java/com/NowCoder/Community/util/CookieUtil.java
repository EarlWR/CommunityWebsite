package com.NowCoder.Community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {

    public static String getValue(HttpServletRequest request,String key)
    {
        if (request==null || key==null)
        {
            throw new IllegalArgumentException("参数为空");
        }
        Cookie[] cookies = request.getCookies();
        if (cookies!=null)
        {
            for (Cookie cookie:cookies)
            {
                if (cookie.getName().equals(key))
                {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
