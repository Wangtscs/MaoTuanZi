package com.maotuanzi.controller.api;

import com.maotuanzi.annotation.RequireAuth;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.service.OssService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "文件上传")
@RestController
@RequestMapping("/v1/uploads")
@RequiredArgsConstructor
public class UploadController extends BaseController {

    private final OssService ossService;

    @ApiOperation("上传图片")
    @PostMapping("/image")
    @RequireAuth
    public ApiResult<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String url = ossService.uploadUserFile(file.getBytes(), file.getOriginalFilename());
            Map<String, String> r = new HashMap<>();
            r.put("url", url);
            return ApiResult.ok(r);
        } catch (Exception e) {
            return ApiResult.fail("上传失败: " + e.getMessage());
        }
    }
}
