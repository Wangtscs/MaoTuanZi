package com.maotuanzi.service;

import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.entity.Notification;

public interface NotificationService {
    PageResult<Notification> list(int page, int pageSize);
    Long unreadCount();
    void markRead(Long id);
    void markAllRead();
    void send(Long userId, String type, String title, String content, String payload);
}