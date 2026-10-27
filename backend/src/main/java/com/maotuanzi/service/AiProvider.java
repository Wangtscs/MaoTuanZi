package com.maotuanzi.service;

import java.util.Map;

public interface AiProvider {
    Map<String, String> generate(String imageUrl, String styleId, String prompt);
    String getProviderName();
}
