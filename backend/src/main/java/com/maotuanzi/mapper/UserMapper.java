package com.maotuanzi.mapper;

import com.maotuanzi.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    int insert(User user);

    int updateById(User user);

    User selectById(@Param("id") Long id);

    User selectByOpenid(@Param("openid") String openid);

    User selectByInviteCode(@Param("inviteCode") String inviteCode);

    int updateInviteCode(@Param("id") Long id, @Param("inviteCode") String inviteCode);

    int updateInviter(@Param("id") Long id, @Param("inviterId") Long inviterId);

    Long countInvited(@Param("inviterId") Long inviterId);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updateVip(@Param("id") Long id, @Param("vipLevel") Integer vipLevel, @Param("vipExpireAt") java.time.LocalDateTime vipExpireAt);

    int updateLastLogin(@Param("id") Long id, @Param("time") java.time.LocalDateTime time);

    int updateAgreement(@Param("id") Long id, @Param("version") String version);

    Long countByStatus(@Param("status") Integer status);

    List<User> selectByPage(@Param("keyword") String keyword, @Param("status") Integer status,
                            @Param("offset") int offset, @Param("limit") int limit);

    Long countByPage(@Param("keyword") String keyword, @Param("status") Integer status);
}