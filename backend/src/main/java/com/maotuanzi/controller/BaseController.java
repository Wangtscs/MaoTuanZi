package com.maotuanzi.controller;

import com.maotuanzi.common.util.TraceIdUtil;
import javax.servlet.http.HttpServletRequest;

public abstract class BaseController {
    protected String getTraceId() {
        return TraceIdUtil.get();
    }
}