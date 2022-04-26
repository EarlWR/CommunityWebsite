package com.NowCoder.Community.Service;

import com.NowCoder.Community.dao.UserMapper;
import com.NowCoder.Community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    public User findUserById(int userId)
    {
        return userMapper.selectById(userId);
    }
}
