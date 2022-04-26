package com.NowCoder.Community.Service;
import com.NowCoder.Community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class AlphaService {
    @Autowired
    private AlphaDao alphaDao;

    public AlphaService(){
        System.out.println("实例化AlphaService");
    }
    @PostConstruct
    public void init()
    {
        System.out.println("初始化AlphaService");
    }
    @PreDestroy
    public void destory()
    {

        System.out.println("销毁AlphaService");
    }

    public String find()
        {
           return alphaDao.select();
        }
}
