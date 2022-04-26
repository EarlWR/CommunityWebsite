package com.NowCoder.Community.Service;

import com.NowCoder.Community.dao.DiscussPostMapper;
import com.NowCoder.Community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    public List<DiscussPost> findDiscuss (int userId, int offset, int limit)
    {
        return discussPostMapper.selectDiscussPosts(userId,offset,limit);
    }

    public int findDiscussRows(int userId)
    {
        return discussPostMapper.selectDiscussPostRows(userId);
    }
}
