package com.igot.cb.util;

import lombok.Data;

@Data
public class ApiRespParam {
    private String resMsgId;
    private String msgId;
    private String err;
    private String status;
    private String errMsg;

    public ApiRespParam() {
    }

    public ApiRespParam(String id) {
        resMsgId = id;
        msgId = id;
    }
}
