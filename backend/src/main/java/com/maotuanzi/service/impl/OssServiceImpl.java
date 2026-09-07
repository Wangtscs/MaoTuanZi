package com.maotuanzi.service.impl;

import com.aliyun.oss.*;
import com.maotuanzi.service.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.UUID;

@Slf4j
@Service
public class OssServiceImpl implements OssService {

    @Value("${maotuanzi.oss.endpoint}") private String endpoint;
    @Value("${maotuanzi.oss.bucket}") private String bucket;
    @Value("${maotuanzi.oss.access-key-id}") private String accessKeyId;
    @Value("${maotuanzi.oss.access-key-secret}") private String accessKeySecret;
    @Value("${maotuanzi.oss.cdn-domain:}") private String cdnDomain;

    private OSS getClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    private String getBaseUrl() {
        return (cdnDomain != null && !cdnDomain.isEmpty())
            ? cdnDomain
            : String.format("https://%s.%s", bucket, endpoint.replace("https://",""));
    }

    @Override
    public String uploadUserFile(byte[] data, String filename) {
        String ext = "";
        int dotIdx = filename.lastIndexOf(".");
        if (dotIdx >= 0) ext = filename.substring(dotIdx);
        String key = "uploads/users/" + UUID.randomUUID().toString().replace("-","") + ext;
        return upload(key, data);
    }

    @Override
    public String uploadWorkFile(byte[] data, String filename) {
        String ext = "";
        int dotIdx = filename.lastIndexOf(".");
        if (dotIdx >= 0) ext = filename.substring(dotIdx);
        String key = "uploads/works/" + UUID.randomUUID().toString().replace("-","") + ext;
        return upload(key, data);
    }

    private String upload(String key, byte[] data) {
        OSS client = getClient();
        try {
            client.putObject(bucket, key, new ByteArrayInputStream(data));
            log.info("OSS上传成功: {}", key);
            return getBaseUrl() + "/" + key;
        } catch (Exception e) {
            log.error("OSS上传失败 key={}", key, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        } finally {
            client.shutdown();
        }
    }

    @Override
    public String signDownloadUrl(String objectKey) {
        OSS client = getClient();
        try {
            java.util.Date expireDate = new java.util.Date(System.currentTimeMillis() + 3600 * 1000L);
            return client.generatePresignedUrl(bucket, objectKey, expireDate).toString();
        } catch (Exception e) {
            log.error("签名URL生成失败 key={}", objectKey, e);
            return getBaseUrl() + "/" + objectKey;
        } finally {
            client.shutdown();
        }
    }
}
