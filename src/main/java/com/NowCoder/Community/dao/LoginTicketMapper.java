package com.NowCoder.Community.dao;

import com.NowCoder.Community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

//I have nothing to offer, but blood, toil, tears, and sweat

@Mapper
public interface LoginTicketMapper {
//    @Insert({"insert into login_ticket (user_id,ticket,status,expired) ",
//             "values(#{userId},#{ticket},#{status},#{expired})"
//    })
//    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket ticket);
//    @Select({"select id,user_id,ticket,status,expired" ,
//            "from login_ticket where ticket=#{ticket}"})
    LoginTicket selectByTicket(String ticker);
//    @Update({"update login_ticket set status=#{status} where ticket=#{ticket}" })
    int updateStatus(String ticket,int status);

}
