package com.NowCoder.Community.util;

import com.NowCoder.Community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户的信息，用于代替session对象，而且这样做是线程隔离的
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users=new ThreadLocal<User>();
    public void setUser(User user)
    {
        users.set(user);
    }
    public User getUser()
    {
        return users.get();
    }
    public void clear()
    {
        users.remove();
    }

}
