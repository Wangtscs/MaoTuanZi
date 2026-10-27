package com.maotuanzi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 退款申请（订单 ID 取自路径参数，body 可省略，reason 选填）。
 */
@Data
@ApiModel("退款申请")
public class RefundRequest {

    @ApiModelProperty("退款原因（选填）")
    private String reason;
}
