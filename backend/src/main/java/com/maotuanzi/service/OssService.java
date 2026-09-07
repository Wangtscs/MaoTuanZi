package com.maotuanzi.service;

public interface OssService {
    String uploadUserFile(byte[] data, String filename);
    String uploadWorkFile(byte[] data, String filename);
    String signDownloadUrl(String objectKey);
}
