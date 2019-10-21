package com.easyci.ci.entity;


public class ResponseResult {
    private Boolean status;
    private Integer errorCode;
    private String errorDesc;
    private Object object;


    public ResponseResult() {
    }

    public ResponseResult(Boolean status, Integer errorCode, String errorDesc, Object object) {
        this.status = status;
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
        this.object = object;

    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

    public void setErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
    }

    public Object getList() {
        return object;
    }

    public void setList(Object object) {
        this.object = object;
    }
}
