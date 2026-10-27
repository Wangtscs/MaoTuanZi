package com.maotuanzi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@ApiModel("创建宠物请求")
public class PetCreateRequest {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 64)
    @ApiModelProperty(value = "宠物昵称")
    private String nickname;

    @NotNull(message = "请选择品类")
    @ApiModelProperty(value = "1=猫 2=犬 3=其他")
    private Integer species;

    @ApiModelProperty("品种 ID")
    private Integer breedId;

    @ApiModelProperty("品种名")
    private String breedName;

    @ApiModelProperty("0=未知 1=公 2=母")
    private Integer gender;

    @ApiModelProperty("生日")
    private LocalDate birthday;

    @ApiModelProperty("体重 kg")
    private BigDecimal weightKg;

    @ApiModelProperty("是否绝育")
    private Integer isNeutered;

    @ApiModelProperty("过敏史 JSON")
    private String allergies;

    @ApiModelProperty("主食品牌")
    private String stapleFood;

    @ApiModelProperty("头像 URL")
    private String avatarUrl;

    @ApiModelProperty("备注")
    private String remark;
}