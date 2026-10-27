package com.maotuanzi.service.impl;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.maotuanzi.service.AiProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WanxAiProvider implements AiProvider {

    @Value("${maotuanzi.ai.wanx.api-key:}") private String apiKey;
    @Value("${maotuanzi.ai.wanx.model:wanx-pro}") private String model;

    private static final Map<String, String> STYLE_PROMPTS = Map.of(
        "oil", "将宠物照片转化为经典油画风格，笔触细腻，色彩浓郁，保留宠物真实特征",
        "disn", "将宠物照片迪士尼卡通风格，圆润可爱，大眼睛，色彩明快，保留宠物特征",
        "wcol", "将宠物照片转化为水彩画风格，清新淡雅，笔触轻盈，保留宠物神韵",
        "cybr", "将宠物照片赛博朋克风格，霓虹灯光，未来科技感，保留宠物特征",
        "cn", "将宠物照片转化为中国风水墨画风格，意境深远，笔触古朴，保留宠物特征",
        "anim", "将宠物照片转化为动漫风格，明亮生动，线条清晰，保留宠物特征"
    );

    @Override
    public Map<String, String> generate(String imageUrl, String styleId, String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("未配置阿里云百炼API Key，请设置环境变量 AI_WANX_API_KEY");
        }

        String stylePrompt = STYLE_PROMPTS.getOrDefault(styleId,
            "将宠物照片转化为艺术风格，保留宠物真实特征");
        if (prompt != null && !prompt.isEmpty()) {
            stylePrompt = stylePrompt + "。" + prompt;
        }

        try {
            ImageSynthesis imageSynthesis = new ImageSynthesis(apiKey, model);
            ImageSynthesisParam param = ImageSynthesisParam.builder()
                .prompt(stylePrompt)
                .n(1)
                .size("1024*1024")
                .refImage(imageUrl)
                .build();

            ImageSynthesisResult result = imageSynthesis.call(param);
            List<Map<String, String>> results = result.getOutput().getResults();
            if (results != null && !results.isEmpty()) {
                Map<String, String> first = results.get(0);
                String outputUrl = first.get("url");
                if (outputUrl == null && !first.isEmpty()) {
                    outputUrl = first.values().iterator().next();
                }
                if (outputUrl != null && !outputUrl.isEmpty()) {
                    log.info("万相生成成功: style={}, output={}", styleId, outputUrl);
                    Map<String, String> r = new HashMap<>();
                    r.put("outputUrl", outputUrl);
                    r.put("taskId", result.getRequestId());
                    return r;
                }
            }
            throw new RuntimeException("万相API返回结果为空");
        } catch (ApiException e) {
            log.error("万相API调用失败 message={}", e.getMessage());
            throw new RuntimeException("AI生成失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("AI生成异常", e);
            throw new RuntimeException("AI生成失败: " + e.getMessage());
        }
    }

    @Override
    public String getProviderName() {
        return "wanx";
    }
}
