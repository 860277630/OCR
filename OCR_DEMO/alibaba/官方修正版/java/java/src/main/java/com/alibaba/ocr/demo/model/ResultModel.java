package com.alibaba.ocr.demo.model;

import java.util.List;
import java.util.UUID;

public class ResultModel {
    private List<RectVal> ret;
    private Boolean success;
    private UUID request_id;

    public List<RectVal> getRet() {
        return ret;
    }

    public void setRet(List<RectVal> ret) {
        this.ret = ret;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public UUID getRequest_id() {
        return request_id;
    }

    public void setRequest_id(UUID request_id) {
        this.request_id = request_id;
    }

    @Override
    public String toString() {
        return "ResultModel{" +
                "ret=" + ret +
                ", success=" + success +
                ", request_id=" + request_id +
                '}';
    }
}
