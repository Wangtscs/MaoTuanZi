package com.maotuanzi.service.impl;

import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.entity.Notification;
import com.maotuanzi.mapper.NotificationMapper;
import com.maotuanzi.security.UserContext;
import com.maotuanzi.service.NotificationService;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public PageResult<Notification> list(int page, int pageSize) {
        Long uid = UserContext.getUserId();
        PageHelper.startPage(page, pageSize);
        List<Notification> list = notificationMapper.selectByUserId(uid, (page - 1) * pageSize, pageSize);
        return PageResult.of(list);
    }

    @Override
    public Long unreadCount() {
        Long uid = UserContext.getUserId();
        return notificationMapper.countUnread(uid);
    }

    @Override
    public void markRead(Long id) {
        Long uid = UserContext.getUserId();
        notificationMapper.markRead(id, uid);
    }

    @Override
    public void markAllRead() {
        Long uid = UserContext.getUserId();
        notificationMapper.markAllRead(uid);
    }

    @Override
    public void send(Long userId, String type, String title, String content, String payload) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setPayload(payload);
        n.setIsRead(0);
        notificationMapper.insert(n);
    }
}