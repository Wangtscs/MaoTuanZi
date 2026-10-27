package com.maotuanzi.service;

import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.entity.Work;

import java.util.List;

public interface WorkService {
    PageResult<Work> listMine(Integer status, int page, int pageSize);
    Work detail(Long id);
    void delete(Long id);
    Work unlockResult(Long id);
    List<Work> publicFeed(String topic, int offset, int limit);
}