package com.maotuanzi.controller.api;

import com.maotuanzi.controller.BaseController;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

@Api(tags = "工具")
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Slf4j
public class QrCodeController extends BaseController {

    @ApiOperation("生成二维码")
    @GetMapping(value = "/qrcode", produces = "image/png")
    public byte[] qrcode(@RequestParam String content,
                         @RequestParam(defaultValue = "300") Integer size) {
        try {
            // 支持长文本（自动选择合适版本）
            BitMatrix matrix = new MultiFormatWriter()
                .encode(content, BarcodeFormat.QR_CODE, size, size);

            int width = matrix.getWidth();
            int height = matrix.getHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("二维码生成失败", e);
            throw new RuntimeException("二维码生成失败: " + e.getMessage());
        }
    }
}
