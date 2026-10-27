package com.maotuanzi.common.result;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页响应
 */
@Data
@ApiModel("分页结果")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("列表")
    private List<T> list;

    @ApiModelProperty("分页信息")
    private Pagination pagination;

    @Data
    @ApiModel("分页信息")
    public static class Pagination implements Serializable {
        private static final long serialVersionUID = 1L;
        private Integer page;
        private Integer pageSize;
        private Long total;
        private Boolean hasMore;
    }

    public static <T> PageResult<T> of(List<T> list) {
        PageResult<T> r = new PageResult<>();
        if (list == null) {
            r.setList(Collections.emptyList());
            Pagination p = new Pagination();
            p.setPage(1);
            p.setPageSize(20);
            p.setTotal(0L);
            p.setHasMore(false);
            r.setPagination(p);
            return r;
        }
        PageInfo<T> info = new PageInfo<>(list);
        r.setList(info.getList());
        Pagination p = new Pagination();
        p.setPage(info.getPageNum());
        p.setPageSize(info.getPageSize());
        p.setTotal(info.getTotal());
        p.setHasMore(info.isHasNextPage());
        r.setPagination(p);
        return r;
    }

    public static <T> PageResult<T> empty() {
        return of(Collections.emptyList());
    }
}