package com.NowCoder.Community;

import com.NowCoder.Community.Service.DiscussPostService;
import com.NowCoder.Community.dao.DiscussPostMapper;
import com.NowCoder.Community.dao.UserMapper;
import com.NowCoder.Community.entity.DiscussPost;
import com.NowCoder.Community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Test
    public void testSelectUserById()
    {
        User user=userMapper.selectByEmail("nowcoder1@sina.com");
        System.out.println(user.toString());
    }
    @Test
    public void testSelectUserByUsername()
    {
        User user=userMapper.selectByName("test");
        System.out.println(user);
    }
    @Test
    public void testSelectUserByEmail()
    {
        User user=userMapper.selectByEmail("test@qq.com");
        System.out.println(user);
    }
    @Test
    public void testInsertUser()
    {
        User user=new User();
        user.setUsername("test");
        user.setPassword("234556");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows=userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }
    @Test
    public void testUpdateUser()
    {
        userMapper.updatePassword(150,"test");
    }
    @Test
    public void testSelectPosts()
    {
        List<DiscussPost> list=discussPostMapper.selectDiscussPosts(0,0,10);
        for (DiscussPost discussPost:list)
        {
            System.out.println(discussPost);
        }

        int rows=discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }
    @Autowired
    DiscussPostService discussPostService;
    @Test
    public void testFindDiscuss()
    {
        List<DiscussPost> list= discussPostService.findDiscuss(0,0,10);
        for (DiscussPost discussPost:list)
        {
            System.out.println(discussPost.toString());
        }
    }
}
