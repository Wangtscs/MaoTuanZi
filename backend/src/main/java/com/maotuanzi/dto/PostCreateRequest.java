package com.maotuanzi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@ApiModel("广场发帖请求")
public class PostCreateRequest {
    @NotBlank
    private String content;
    private List<String> imageUrls;
    private String topic;
    private Long workId;
    private Long petId;
    private Integer is3d;
}