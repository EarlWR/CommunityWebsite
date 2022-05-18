package com.NowCoder.Community.controller.interceptor;

import com.NowCoder.Community.annotation.LoginRequired;
import com.NowCoder.Community.entity.User;
import com.NowCoder.Community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    @Autowired
    HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //拦截方法，并提取出LoginRequired注解，若有注解且登录状态为false（即hostHolder中提取到的用户为null)，则拒绝访问并重定向到登陆页面
        if (handler instanceof HandlerMethod)
        {
            HandlerMethod handlerMethod=(HandlerMethod) handler;
            Method method=handlerMethod.getMethod();
            LoginRequired loginRequired=method.getAnnotation(LoginRequired.class);
            if (loginRequired != null && hostHolder.getUser()==null) {
                response.sendRedirect(request.getContextPath()+"/login");
                return false;
            }
        }
        return true;
    }
}
