package com.maotuanzi.service;

import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.entity.*;

import java.util.List;
import java.util.Map;

public interface AdminService {
    Map<String, Object> login(String username, String password, String ip);
    void logout(String token);

    // 用户管理
    PageResult<User> listUsers(String keyword, Integer status, int page, int pageSize);
    User getUser(Long id);
    void banUser(Long id);
    void unbanUser(Long id);

    // 作品审核
    PageResult<Work> listWorks(Integer status, int page, int pageSize);
    void reviewWork(Long id, Integer status);

    // 订单
    PageResult<Order> listOrders(String keyword, Integer status, int page, int pageSize);
    Order getOrder(Long id);

    // 退款
    PageResult<Refund> listRefunds(Integer status, int page, int pageSize);
    void reviewRefund(Long id, boolean approve, String remark);

    // 内容
    PageResult<SquarePost> listPosts(Integer status, int page, int pageSize);
    void offlinePost(Long id);
    List<SensitiveWord> listSensitiveWords();
    void addSensitiveWord(SensitiveWord word);
    void deleteSensitiveWord(Integer id);

    // 配置
    List<SystemConfig> listConfigs();
    void updateConfig(String key, String value, String adminName);

    // 统计
    Map<String, Object> dashboard();
}