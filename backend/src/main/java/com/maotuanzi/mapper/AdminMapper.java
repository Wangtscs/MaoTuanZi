package com.maotuanzi.mapper;

import com.maotuanzi.entity.AdminUser;
import com.maotuanzi.entity.AdminRole;
import com.maotuanzi.entity.AdminPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminMapper {
    AdminUser selectByUsername(@Param("username") String username);
    AdminUser selectById(@Param("id") Long id);
    int updateLastLogin(@Param("id") Long id, @Param("ip") String ip);
    int updatePassword(@Param("id") Long id, @Param("hash") String hash);
    List<AdminUser> selectAll(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
    Long countAll(@Param("keyword") String keyword);
    int insert(AdminUser admin);
    int updateById(AdminUser admin);

    List<AdminRole> selectRolesByUserId(@Param("adminUserId") Long adminUserId);
    List<AdminRole> selectAllRoles();
    AdminRole selectRoleById(@Param("id") Integer id);

    List<AdminPermission> selectPermsByUserId(@Param("adminUserId") Long adminUserId);
    List<AdminPermission> selectAllPerms();
}