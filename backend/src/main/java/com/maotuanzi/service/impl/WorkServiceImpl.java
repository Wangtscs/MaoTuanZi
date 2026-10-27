package com.maotuanzi.service.impl;

import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.enums.WorkStatusEnum;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.entity.Work;
import com.maotuanzi.mapper.WorkMapper;
import com.maotuanzi.security.UserContext;
import com.maotuanzi.service.WorkService;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkServiceImpl implements WorkService {

    private final WorkMapper workMapper;

    @Override
    public PageResult<Work> listMine(Integer status, int page, int pageSize) {
        Long uid = UserContext.getUserId();
        PageHelper.startPage(page, pageSize);
        List<Work> list = workMapper.selectByUserId(uid, status, (page - 1) * pageSize, pageSize);
        return PageResult.of(list);
    }

    @Override
    public Work detail(Long id) {
        Long uid = UserContext.getUserId();
        Work w = workMapper.selectById(id);
        if (w == null) throw BizException.of(ErrorCode.WORK_NOT_FOUND);
        if (!w.getUserId().equals(uid)) throw BizException.of(ErrorCode.FORBIDDEN);
        return w;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long uid = UserContext.getUserId();
        Work w = workMapper.selectById(id);
        if (w == null || !w.getUserId().equals(uid)) {
            throw BizException.of(ErrorCode.WORK_NOT_FOUND);
        }
        workMapper.softDelete(id, LocalDateTime.now());
    }

    @Override
    @Transactional
    public Work unlockResult(Long id) {
        Long uid = UserContext.getUserId();
        Work w = workMapper.selectById(id);
        if (w == null || !w.getUserId().equals(uid)) {
            throw BizException.of(ErrorCode.WORK_NOT_FOUND);
        }
        if (w.getResultUrl() == null) {
            // 模拟：补一个高清结果 URL
            workMapper.updateResultUrl(id, "https://cdn.maotuanzi.ai/mock/result/" + UUID.randomUUID() + ".jpg");
            return workMapper.selectById(id);
        }
        return w;
    }

    @Override
    public List<Work> publicFeed(String topic, int offset, int limit) {
        return workMapper.selectPublicFeed(topic, offset, limit);
    }
}