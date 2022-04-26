package com.NowCoder.Community.dao;

import com.NowCoder.Community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);   //userId参数是用于在“我的帖子”功能中查询所发布的帖子(若参数为0，则代表不使用该功能，无视该参数)
                                                                                //offset参数是在分页功能中表示每一页的起始行号，limit表示每一页最多显示多少个帖子
    int selectDiscussPostRows(@Param("userId") int uerId);             //查询帖子的行数，方便计算页数，UserId参数同上。@Param用于取别名，若需要在<if>中使用，且方法有且只有一个参数，则该参数必须取别名



}
